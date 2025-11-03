package com.pluta.camera.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PythonAnalysisResult {
    private String resolution;
    private int tablesDetected;
    private int chairsDetected;
    private int benchesDetected;
    private int couchesDetected;
    private int personsDetected;
    private int totalDetected;
    private int occupiedChairs;
    private int unoccupiedChairs;
    private int occupiedBenches;
    private int unoccupiedBenches;
    private int occupiedCouches;
    private int unoccupiedCouches;
    private int personsSitting;

    private String annotatedImagePath;
    private String status;
    //private List<TableChairRelationship> tableChairRelationships;
    //private Map<String, Object> debugInfo;
}
