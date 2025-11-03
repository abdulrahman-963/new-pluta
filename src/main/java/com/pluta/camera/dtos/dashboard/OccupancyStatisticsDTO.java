package com.pluta.camera.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OccupancyStatisticsDTO {
    private Double currentOccupancyRate;
    private Double averageOccupancyRate;
    private Double peakOccupancyRate;
    private String peakOccupancyTime;
    private OccupancyDetailsDTO occupancyDetails;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OccupancyDetailsDTO {
        private Integer totalSeats;
        private Integer occupiedSeats;
        private Integer availableSeats;
        private SeatingBreakdownDTO seatingBreakdown;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatingBreakdownDTO {
        private Integer chairsOccupied;
        private Integer chairsAvailable;
        private Integer benchesOccupied;
        private Integer benchesAvailable;
        private Integer couchesOccupied;
        private Integer couchesAvailable;
    }
}
