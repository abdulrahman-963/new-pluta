package com.pluta.camera.dtos;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchDTO extends BaseDTO {

    private Long id;

    @NotNull(message = "Tenant ID is required")
    private Long tenantId;

    private String tenantCode;

    @NotBlank(message = "Code is required")
    @Size(max = 255, message = "Code must not exceed 255 characters")
    private String code;

    @NotBlank(message = "English Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String englishName;

    @NotBlank(message = "Arabic Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String arabicName;

    @NotBlank(message = "Country is required")
    @Size(max = 50, message = "Country must not exceed 50 characters")
    private String country;

    @NotBlank(message = "City is required")
    @Size(max = 50, message = "City must not exceed 50 characters")
    private String city;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @Size(max = 50, message = "Timezone must not exceed 50 characters")
    private String timezone;

    @Min(value = 1, message = "Max capacity must be at least 1")
    private Integer maxCapacity;
}