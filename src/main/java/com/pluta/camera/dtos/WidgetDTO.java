package com.pluta.camera.dtos;


import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WidgetDTO {
    private Long id;
    private String title;
    @Column(nullable = false)
    private String queryName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String hqlQuery;
}