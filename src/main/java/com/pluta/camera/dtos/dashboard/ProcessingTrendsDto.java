package com.pluta.camera.dtos.dashboard;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ProcessingTrendsDto {
    private List<Map<String, Object>> dailyStats;
    private Map<String, Object> trendAnalysis;
    private Double averageProcessingTimeMinutes;
    private Long totalProcessedInPeriod;
    private Double successRateTrend;
}
