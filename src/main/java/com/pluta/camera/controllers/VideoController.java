package com.pluta.camera.controllers;

import com.pluta.camera.context.TenantContext;
import com.pluta.camera.dtos.VideoResponseDto;
import com.pluta.camera.services.VideoProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@RestController
@RequestMapping("/v1/video")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class VideoController {

    public static final String SUCCESS = "success";
    public static final String MESSAGE = "message";
    private final VideoProcessingService videoProcessingService;

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload video based on specific zone and camera")
    @PreAuthorize("hasRole('video-upload')")
    public ResponseEntity<Map<String, Object>> uploadVideo(@RequestParam("file") MultipartFile file,
                                                           @RequestParam("zoneId") Long zoneId,
                                                           @RequestParam("cameraId") Long cameraId) throws IOException {
        Map<String, Object> response = new HashMap<>();

            // Validate file
            if (file.isEmpty()) {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "File is empty");
                return ResponseEntity.badRequest().body(response);
            }

            // Check if file is a video
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                response.put(SUCCESS, false);
                response.put(MESSAGE, "File must be a video");
                return ResponseEntity.badRequest().body(response);
            }

            // Save video details and start async processing
            Long videoId = videoProcessingService.uploadAndProcessVideo(file, TenantContext.getTenantId(),TenantContext.getTenantId(),zoneId,cameraId);

            response.put(SUCCESS, true);
            response.put(MESSAGE, "Video uploaded successfully and processing started");
            response.put("videoId", videoId);

            return ResponseEntity.ok(response);

    }

    @GetMapping
    public ResponseEntity<List<VideoResponseDto>> getVideoDetails(@PageableDefault Pageable page){
        List<VideoResponseDto> videoDetails = videoProcessingService.getVideos(page);
        return ResponseEntity.ok(videoDetails);
    }

    @GetMapping("/by-tenant-branch")
    @Operation(summary = "Get videos by tenant ID and branch ID")
    public ResponseEntity<List<VideoResponseDto>> getVideos(@PageableDefault Pageable page) {
        List<VideoResponseDto> videos = videoProcessingService.getVideosByTenantAndBranch(page,TenantContext.getTenantId(),
                TenantContext.getBranchId());
        return ResponseEntity.ok(videos);
    }

    @GetMapping("/{videoId}")
    public ResponseEntity<VideoResponseDto> getVideoDetails(@PathVariable Long videoId) {
        try {
            VideoResponseDto videoDetails = videoProcessingService.getVideoDetails(videoId);
            return ResponseEntity.ok(videoDetails);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{videoId}/status")
    public ResponseEntity<Map<String, String>> getVideoStatus(@PathVariable Long videoId) {
        try {
            String status = videoProcessingService.getVideoStatus(videoId);
            Map<String, String> response = new HashMap<>();
            response.put("status", status);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}