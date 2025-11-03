package com.pluta.camera.dtos;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZoneDTO extends BaseDTO {

    private Long id;

    @NotNull(message = "Branch ID is required")
    private Long branchId;

    private String branchCode;

    @NotNull(message = "Tenant ID is required")
    private Long tenantId;

    private String tenantCode;

    @NotBlank(message = "Code is required")
    @Size(max = 255, message = "Code must not exceed 255 characters")
    private String code;

    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;
}
