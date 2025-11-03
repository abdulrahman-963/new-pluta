package com.pluta.camera.dtos;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class VideoDto {

    private Long id;
    private String originalFileName;
    private Long fileSize;
    private String contentType;
    private String status;
    private Double duration;
    private Integer framesExtracted;
    private List<String> framePaths;
    private LocalDateTime uploadedAt;
    private LocalDateTime processingStartedAt;
    private LocalDateTime processingCompletedAt;
    private Integer processingTime ;
    private String errorMessage;



}
