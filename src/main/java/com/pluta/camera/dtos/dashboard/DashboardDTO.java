package com.pluta.camera.dtos.dashboard;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {
    private VideoSummaryDTO videoSummary;
    private AnalysisSummaryDTO analysisSummary;
    private ProcessingStatusSummaryDTO processingStatus;
    private OccupancyStatisticsDTO occupancyStatistics;
    private LocalDateTime lastUpdated;
}