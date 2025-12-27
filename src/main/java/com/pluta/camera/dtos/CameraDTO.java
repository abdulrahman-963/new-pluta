package com.pluta.camera.dtos;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.pluta.camera.enums.CameraStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CameraDTO extends BaseDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotNull(message = "Zone ID is required")
    private Long zoneId;

    @NotBlank(message = "Code is required")
    @Size(max = 200, message = "Code must not exceed 200 characters")
    private String code;

    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @Size(max = 100, message = "Model must not exceed 100 characters")
    private String model;

    private String rtspUrl;

    @Size(max = 100, message = "Mount position must not exceed 100 characters")
    private String mountPosition;

    @Size(max = 20, message = "Resolution must not exceed 20 characters")
    private String resolution;

    @Min(value = 1, message = "FPS must be at least 1")
    @Max(value = 120, message = "FPS must not exceed 120")
    private Integer fps;

    private CameraStatus status;
}
