package com.pluta.camera.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisSummaryDTO {
    private Long totalAnalyses;
    private Long successfulAnalyses;
    private Long failedAnalyses;
    private Long pendingAnalyses;
    private Double averageConfidenceThreshold;
    private ObjectDetectionSummaryDTO objectDetectionSummary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ObjectDetectionSummaryDTO {
        private Long totalTablesDetected;
        private Long totalChairsDetected;
        private Long totalBenchesDetected;
        private Long totalCouchesDetected;
        private Long totalPersonsDetected;
        private Double averageOccupancyRate;
    }
}