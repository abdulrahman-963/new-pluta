package com.pluta.camera.entities;


import com.pluta.camera.enums.ProcessingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "VIDEO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicUpdate
@DynamicInsert
public class Video_old extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "VIDEO_GEN")
    @SequenceGenerator(name = "VIDEO_GEN", sequenceName = "VIDEO_SEQ", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessingStatus status;

    private Double duration;

    private Integer framesExtracted;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    private LocalDateTime processingStartedAt;

    private LocalDateTime processingCompletedAt;

    private String errorMessage;

    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ImageAnalysisResult> imageAnalysisResults;


    // Helper methods for managing the relationship
    public void addImageAnalysisResult(ImageAnalysisResult imageAnalysisResult) {
        if (this.imageAnalysisResults == null) {
            this.imageAnalysisResults = new ArrayList<>();
        }
        this.imageAnalysisResults.add(imageAnalysisResult);
       // imageAnalysisResult.setVideo(this);
    }

    public void removeImageAnalysisResult(ImageAnalysisResult imageAnalysisResult) {
        if (this.imageAnalysisResults != null) {
            this.imageAnalysisResults.remove(imageAnalysisResult);
            imageAnalysisResult.setVideo(null);
        }
    }

    public void clearImageAnalysisResults() {
        if (this.imageAnalysisResults != null) {
            for (ImageAnalysisResult result : this.imageAnalysisResults) {
                result.setVideo(null);
            }
            this.imageAnalysisResults.clear();
        }
    }

}