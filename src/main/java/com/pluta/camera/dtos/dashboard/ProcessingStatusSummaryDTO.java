package com.pluta.camera.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingStatusSummaryDTO {
    private Map<String, Long> videoStatusCounts;
    private Map<String, Long> analysisStatusCounts;
    private Long activeProcessing;
    private Long queuedForProcessing;
    private Double processingSuccessRate;
}