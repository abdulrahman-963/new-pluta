package com.pluta.camera.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableCoordinatesDTO {

    private Long id;

    private Long tableId;

    @NotNull(message = "X coordinate is required")
    private Integer x;

    @NotNull(message = "Y coordinate is required")
    private Integer y;
}