package com.pluta.camera.dtos;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageAnalysisResponse {
    private Long analysisId;
    private String filename;
    private String status;
    private String message;
    private AnalysisResultDTO result;
}
