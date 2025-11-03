package com.pluta.camera.controllers;

import com.pluta.camera.dtos.VideoResponseDto;
import com.pluta.camera.services.VideoProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/video")
@RequiredArgsConstructor
public class VideoController {

    public static final String SUCCESS = "success";
    public static final String MESSAGE = "message";
    private final VideoProcessingService videoProcessingService;

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadVideo(@RequestParam("file") MultipartFile file,
                                                           @RequestParam("tenantId") Long tenantId,
                                                           @RequestParam("branchId") Long branchId,
                                                           @RequestParam("zoneId") Long zoneId,
                                                           @RequestParam("cameraId") Long cameraId) {
        Map<String, Object> response = new HashMap<>();

        try {
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
            Long videoId = videoProcessingService.uploadAndProcessVideo(file,tenantId,branchId,zoneId,cameraId);

            response.put(SUCCESS, true);
            response.put(MESSAGE, "Video uploaded successfully and processing started");
            response.put("videoId", videoId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put(SUCCESS, false);
            response.put(MESSAGE, "Error uploading video: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping
    public ResponseEntity<List<VideoResponseDto>> getVideoDetails(@PageableDefault Pageable page){
        List<VideoResponseDto> videoDetails = videoProcessingService.getVideos(page);
        return ResponseEntity.ok(videoDetails);

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