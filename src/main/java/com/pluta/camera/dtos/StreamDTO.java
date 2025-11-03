package com.pluta.camera.dtos;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StreamDTO extends BaseDTO {

    private Long id;

    @NotNull(message = "Camera ID is required")
    private Long cameraId;
    private String cameraCode;

    @NotNull(message = "Zone ID is required")
    private Long zoneId;

    private String zoneCode;

    @NotNull(message = "Branch ID is required")
    private Long branchId;

    private String branchCode;

    @NotNull(message = "Tenant ID is required")
    private Long tenantId;

    private String tenantCode;

    @NotBlank(message = "URL is required")
    private String url;

    private String apiKeySecretId;

    @Size(max = 50, message = "Username must not exceed 50 characters")
    private String username;

    // Password excluded from DTO for security - never returned to client
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Size(max = 50, message = "Model version must not exceed 50 characters")
    private String modelVersion;

    @Min(value = 1, message = "Sampling interval must be at least 1 second")
    @Max(value = 3600, message = "Sampling interval must not exceed 3600 seconds (1 hour)")
    private Integer samplingIntervalSeconds;

    private Boolean active;
}