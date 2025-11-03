package com.pluta.camera.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoSummaryDTO {
    private Long totalVideos;
    private Long totalStorageSize;
    private Double totalDuration;
    private Long videosProcessedToday;
    private Long videosUploadedToday;
    private Double averageProcessingTime;
    private List<RecentVideoDTO> recentVideos;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentVideoDTO {
        private Long id;
        private String originalFileName;
        private Long fileSize;
        private String status;
        private LocalDateTime uploadedAt;
        private Double duration;
        private Integer framesExtracted;
        private Integer totalDetectedObjects;
    }
}