package com.pluta.camera.dtos;


import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ImageAnalysisRequest {
    @NotNull(message = "Image file is required")
    private MultipartFile image;

    @DecimalMin(value = "0.1", message = "Confidence threshold must be at least 0.1")
    @DecimalMax(value = "1.0", message = "Confidence threshold must be at most 1.0")
    private Double confidenceThreshold = 0.2;

    // Getters and setters
}