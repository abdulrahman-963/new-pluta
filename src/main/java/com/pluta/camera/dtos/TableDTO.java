package com.pluta.camera.dtos;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableDTO extends BaseDTO{

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

    private String tenantName;

    @NotNull(message = "Table number is required")
    private Integer tableNumber;

    @Size(max = 100, message = "Label must not exceed 100 characters")
    private String label;

    @Valid
    @NotEmpty(message = "Coordinates are required")
    private List<TableCoordinatesDTO> coordinates;

}
