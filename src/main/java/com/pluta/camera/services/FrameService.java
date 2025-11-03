package com.pluta.camera.services;

import com.pluta.camera.dtos.FrameAnalysisResultDTO;
import com.pluta.camera.dtos.TableCoordinatesDTO;
import com.pluta.camera.dtos.TableDTO;
import com.pluta.camera.entities.*;
import com.pluta.camera.repositories.FrameRepository;
import com.pluta.camera.repositories.TableRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class FrameService {

    @Value("${upload-dir:temp}")
    private String tempDir;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private final PythonScriptExecutor pythonExecutor;
    private final TableRepository tableRepository;
    private final TableMapper tableMapper;
    private final FrameMapper frameMapper;
    private final FrameRepository frameRepository;


    public void frameAnalysis(File file, Double confidenceThreshold,
                              Double zoneConfidenceThreshold, Video video, StreamEntity stream) throws IOException,
            InterruptedException {
        List<FrameAnalysisResultDTO> results =  analyze( file,  confidenceThreshold,
                 zoneConfidenceThreshold, video.getCamera().getId(), video.getZone().getId(), video.getBranch().getId(),
                video.getTenant().getId());

        List<Frame> frames = frameMapper.toEntityList(results);

        frames.forEach(f -> {f.setVideo(video); f.setStream(stream);
            f.setBranch(video.getBranch()); f.setTenant(video.getTenant()); f.setConfidenceThreshold(confidenceThreshold);});

        frameRepository.saveAll(frames);
    }


    public List<FrameAnalysisResultDTO> analyze(File file, Double confidenceThreshold,
                                                Double zoneConfidenceThreshold,Long cameraId,
                                                Long zoneId, Long branchId, Long tenantId) throws IOException,
            InterruptedException {

        List<TableEntity> tables =  tableRepository.findByTenantIdAndBranchIdAndZoneIdAndCameraId(tenantId,branchId,
                zoneId,cameraId);

        List<TableDTO> tableDTOS = tableMapper.toDTOList(tables);

        Map<Long, List<String>> coo = tableDTOS.stream()
                .flatMap(t -> t.getCoordinates().stream())
                .collect(Collectors.groupingBy(
                        TableCoordinatesDTO::getTableId,
                        Collectors.flatMapping(
                                c -> Stream.of(String.valueOf(c.getX()), String.valueOf(c.getY())),
                                Collectors.toList()
                        )
                ));


        String paths = file.getAbsolutePath();

        List<FrameAnalysisResultDTO> frames = new ArrayList<>();

        for (Map.Entry<Long, List<String>> entry : coo.entrySet()) {
            Long key = entry.getKey();
            List<String> value = entry.getValue();
            frames.add(pythonExecutor.executeAnalysis(paths, confidenceThreshold,
                    zoneConfidenceThreshold, cameraId, key, value));
        }

        return frames;

    }


    public String saveImageToTemp(MultipartFile file) throws IOException {
        // Validate file
        validateFile(file);

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

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
