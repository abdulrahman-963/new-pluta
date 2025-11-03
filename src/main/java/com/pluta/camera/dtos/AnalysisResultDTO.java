package com.pluta.camera.dtos;

import com.pluta.camera.enums.AnalysisStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AnalysisResultDTO {
    private String filename;
    private String resolution;
    private Integer tablesDetected;
    private Integer chairsDetected;
    private Integer benchesDetected;
    private Integer couchesDetected;
    private Integer personsDetected;
    private Integer occupiedChairs;
    private Integer unoccupiedChairs;
    private Integer occupiedBenches;
    private Integer unoccupiedBenches;
    private Integer occupiedCouches;
    private Integer unoccupiedCouches;
    private Integer personsSitting;
    private Integer totalDetected;
    private String annotatedImagePath;
    private LocalDateTime analysisDate;
    private AnalysisStatus status;
}
