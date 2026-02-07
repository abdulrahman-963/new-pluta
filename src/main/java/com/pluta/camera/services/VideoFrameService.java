package com.pluta.camera.services;

import com.pluta.camera.clients.StreamAnalysisClient;
import com.pluta.camera.context.TenantContext;
import com.pluta.camera.dtos.FrameAnalysisResultDTO;
import com.pluta.camera.dtos.StreamAnalysisRequest;
import com.pluta.camera.dtos.TableCoordinatesDTO;
import com.pluta.camera.dtos.TableDTO;
import com.pluta.camera.entities.Frame;
import com.pluta.camera.entities.StreamEntity;
import com.pluta.camera.entities.TableEntity;
import com.pluta.camera.entities.Video;
import com.pluta.camera.exceptions.ResourceNotFoundException;
import com.pluta.camera.repositories.FrameRepository;
import com.pluta.camera.repositories.TableRepository;
import com.pluta.camera.services.interfaces.IFrameService;
import com.pluta.camera.services.mappers.FrameMapper;
import com.pluta.camera.services.mappers.TableMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service("videoFrameService")
@Transactional
@RequiredArgsConstructor
@Slf4j
public class VideoFrameService implements IFrameService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private final PythonScriptExecutor pythonExecutor;
    private final TableRepository tableRepository;
    private final TableMapper tableMapper;
    private final FrameMapper frameMapper;
    private final FrameRepository frameRepository;
    private final StreamAnalysisClient streamAnalysisClient;
    @Value("${upload-dir:temp}")
    private String tempDir;

    public void frameAnalysis(File file, Double confidenceThreshold,
                              Double zoneConfidenceThreshold, Video video, StreamEntity stream,
                              Double frameTimeSecond) throws IOException,
            InterruptedException {

        List<FrameAnalysisResultDTO> results = analyze(file, stream.getUrl(), confidenceThreshold,
                zoneConfidenceThreshold, video.getCamera().getId(), video.getZone().getId(), video.getBranch().getId(),
                video.getTenant().getId());


        List<Frame> frames = frameMapper.toEntityList(results);

        // Fetch all tables for the frames and create a lookup map
        List<Long> tableIds = results.stream()
                .map(FrameAnalysisResultDTO::getTableId)
                .filter(java.util.Objects::nonNull)
                .map(Integer::longValue)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, TableEntity> tableMap = tableRepository.findAllById(tableIds).stream()
                .collect(Collectors.toMap(TableEntity::getId, t -> t));

        // Set all relationships for each frame
        for (int i = 0; i < frames.size(); i++) {
            Frame frame = frames.get(i);
            FrameAnalysisResultDTO resultDTO = results.get(i);

            frame.setVideo(video);
            frame.setStream(stream);
            frame.setBranch(video.getBranch());
            frame.setTenant(video.getTenant());
            frame.setConfidenceThreshold(confidenceThreshold);
            frame.setFrameOffsetSeconds(frameTimeSecond);

            // Set the table reference
            if (resultDTO.getTableId() != null) {
                TableEntity table = tableMap.get(resultDTO.getTableId().longValue());
                if (table != null) {
                    frame.setTable(table);
                } else {
                    log.warn("Table with ID {} not found for frame", resultDTO.getTableId());
                }
            }
        }

        frameRepository.saveAll(frames);
    }


    private List<FrameAnalysisResultDTO> analyze(File file, String url, Double confidenceThreshold,
                                                Double zoneConfidenceThreshold, Long cameraId,
                                                Long zoneId, Long branchId, Long tenantId) throws IOException,
            InterruptedException {

        List<TableEntity> tables = tableRepository.findByTenantIdAndBranchIdAndZoneIdAndCameraId(tenantId, branchId,
                zoneId, cameraId);

        List<TableDTO> tableDTOS = tableMapper.toDTOList(tables);

        Map<Long, List<String>> coo = tableDTOS.stream()
                .flatMap(t -> t.getCoordinates().stream())
                .sorted(Comparator.comparing(TableCoordinatesDTO::getId))
                .collect(Collectors.groupingBy(
                        TableCoordinatesDTO::getTableId,
                        Collectors.flatMapping( c -> Stream.of(String.valueOf(c.getX()), String.valueOf(c.getY())),
                                Collectors.toList() )
                ));


        String paths = file.getAbsolutePath();

        List<FrameAnalysisResultDTO> frames = new ArrayList<>();

        for (Map.Entry<Long, List<String>> entry : coo.entrySet()) {
            Long key = entry.getKey();
            List<String> value = entry.getValue();

            FrameAnalysisResultDTO frameDto = pythonExecutor.executeAnalysis(paths, confidenceThreshold,
                    zoneConfidenceThreshold, cameraId, key, value);
          frames.add(frameDto);
        }

        return frames;

    }


    public String saveImageToTemp(MultipartFile file) throws IOException {
        // Validate file
        validateFile(file);

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID() + fileExtension;

        // Create temp directory path
        Path tempDirPath = Paths.get(tempDir);
        Files.createDirectories(tempDirPath);

        // Create full file path
        Path filePath = tempDirPath.resolve(uniqueFilename);

        // Save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        String fullPath = filePath.toAbsolutePath().toString();
        log.info("Image saved successfully to: {}", fullPath);

        return fullPath;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or null");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "File size exceeds maximum limit of " + (MAX_FILE_SIZE / (1024 * 1024)) + "MB"
            );
        }

        String contentType = file.getContentType();
        if (contentType == null || !isValidImageType(contentType)) {
            throw new IllegalArgumentException(
                    "Invalid file type. Only images are allowed (jpg, jpeg, png, gif, bmp)"
            );
        }
    }

    private boolean isValidImageType(String contentType) {
        return contentType.equals("image/jpeg") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/gif") ||
                contentType.equals("image/bmp") ||
                contentType.equals("image/webp");
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex);
        }
        return "";
    }

    public void deleteTempFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
            log.info("Temporary file deleted: {}", filePath);
        } catch (IOException e) {
            log.error("Failed to delete temporary file: {}", filePath, e);
        }
    }

}
