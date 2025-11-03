package com.pluta.camera.enums;

import lombok.Getter;

@Getter
public enum AnalysisStatus {
    PENDING("Analysis is pending"),
    PROCESSING("Analysis is in progress"),
    COMPLETED("Analysis completed successfully"),
    FAILED("Analysis failed");

    private final String description;

    AnalysisStatus(String description) {
        this.description = description;
    }

}
