package com.pluta.camera.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OccupancyTrendsDTO {
    private LocalDate date;
    private Double averageOccupancy;
    private Double peakOccupancy;
    private Integer totalVisitors;
    private String busiestHour;
}