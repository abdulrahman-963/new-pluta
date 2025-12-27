package com.pluta.camera.dtos;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableDTO extends BaseDTO{

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotNull(message = "Camera ID is required")
    private Long cameraId;

    @NotNull(message = "Zone ID is required")
    private Long zoneId;

    @NotNull(message = "Table number is required")
    private Integer tableNumber;

    @Size(max = 100, message = "Label must not exceed 100 characters")
    private String label;

    @Valid
    @NotEmpty(message = "Coordinates are required")
    private List<TableCoordinatesDTO> coordinates;

}
