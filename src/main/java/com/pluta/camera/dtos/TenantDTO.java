package com.pluta.camera.dtos;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.pluta.camera.enums.TenantStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantDTO extends BaseDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank(message = "Code is required")
    @Size(max = 50, message = "code must not exceed 50 characters")
    private String code;

    @NotBlank(message = "Arabic Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String arabicName;

    @NotBlank(message = "English Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String englishName;

    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String contactEmail;

    @Size(max = 50, message = "Plan must not exceed 50 characters")
    private String plan;

    private TenantStatus status;
}