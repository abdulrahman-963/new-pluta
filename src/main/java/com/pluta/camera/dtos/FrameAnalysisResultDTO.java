package com.pluta.camera.dtos;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class FrameAnalysisResultDTO {

    private Integer cameraId;
    private Integer tableId;
    private Double frameOffsetSeconds;
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
    private String status;
}
