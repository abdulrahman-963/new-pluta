package com.pluta.camera.services;


import com.pluta.camera.dtos.VideoResponseDto;
import com.pluta.camera.entities.*;
import com.pluta.camera.exceptions.ResourceNotFoundException;
import com.pluta.camera.repositories.*;
import com.pluta.camera.services.mappers.VideoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class VideoProcessingService {

    @Value("${video.frames.output.directory:./extracted_frames}")
    private String outputDirectory;

    @Value("${video.temp.directory:./temp}")
    private String tempDirectory;

    @Value("${video.storage.directory:./videos}")
    private String videoStorageDirectory;

    private final VideoRepository videoRepository;
    private final VideoMapper  videoMapper;
    private final AsyncVideoProcessor asyncVideoProcessor;
    private final BranchRepository branchRepository;
    private final TenantRepository tenantRepository;
    private final CameraRepository cameraRepository;
    private final ZoneRepository zoneRepository;

    public Long uploadAndProcessVideo(MultipartFile file, Long tenantId, Long branchId, Long zoneId, Long cameraId) throws IOException {
        // Create directories if they don't exist
        createDirectories();

        // Generate unique filename
        String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        String videoPath = videoStorageDirectory + File.separator + uniqueFileName;

        // Save video file
        try (FileOutputStream fos = new FileOutputStream(videoPath)) {
            fos.write(file.getBytes());
        }

        // Validate tenant exists
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + tenantId));

        // Validate branch exists and belongs to tenant
        Branch branch = branchRepository.findByTenantIdAndId(tenantId, branchId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Branch not found with id: %d for tenant: %s",
                                branchId, tenantId)));

        // Validate zone exists and belongs to branch
        Zone zone = zoneRepository.findByTenantIdAndBranchIdAndId(tenantId, branchId, zoneId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Zone not found with id: %d for branch: %d",
                                zoneId, branchId)));

        // Validate camera exists and belongs to tenant
        Camera camera = cameraRepository.findById(cameraId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Camera not found with id: %d for tenant: %s",
                                cameraId, tenantId)));

        // Save video details to database
        Video video = new Video(
                file.getOriginalFilename(),
                uniqueFileName,
                file.getSize(),
                file.getContentType(),
                videoPath,
                tenant,
                branch,
                zone,
                camera
        );


        Video savedVideo = videoRepository.save(video);

        // Start async processing using separate service
        asyncVideoProcessor.processVideoAsync(savedVideo.getId(), videoPath);

        return savedVideo.getId();
    }

    public List<VideoResponseDto> getVideos(Pageable pageable) {
        return videoMapper.toDTOList(videoRepository.findAll(pageable).getContent());
    }

    public List<VideoResponseDto> getVideosByTenantAndBranch(Pageable page, Long tenantId, Long branchId) {
        List<Video> videos = videoRepository.findByTenantIdAndBranchId(page, tenantId, branchId);
        return videoMapper.toDTOList(videos);
    }

    public VideoResponseDto getVideoDetails(Long videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));
        return videoMapper.toDTO(video);
    }

    public String getVideoStatus(Long videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));
        return video.getStatus().name();
    }

    private void createDirectories() throws IOException {
        Path outputPath = Paths.get(outputDirectory);
        Path tempPath = Paths.get(tempDirectory);
        Path videoPath = Paths.get(videoStorageDirectory);

        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }

        if (!Files.exists(tempPath)) {
            Files.createDirectories(tempPath);
        }

        if (!Files.exists(videoPath)) {
            Files.createDirectories(videoPath);
        }
    }


}