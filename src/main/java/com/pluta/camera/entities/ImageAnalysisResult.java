package com.pluta.camera.entities;

import com.pluta.camera.enums.AnalysisStatus;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Entity
@Table(name = "IMAGE_ANALYSIS_RESULTS")
@Getter
@Setter
@DynamicUpdate
@DynamicInsert
public class ImageAnalysisResult {

    public ImageAnalysisResult() {}


    public ImageAnalysisResult(String filename, String originalPath) {
        this.filename = filename;
        this.originalPath = originalPath;
        this.analysisDate = LocalDateTime.now();
        this.status = AnalysisStatus.PENDING;
    }

    // Constructor with Video reference
    public ImageAnalysisResult(String filename, String originalPath, Video video) {
        this.filename = filename;
        this.originalPath = originalPath;
        this.analysisDate = LocalDateTime.now();
        this.status = AnalysisStatus.PENDING;
        this.video = video;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "IMAGE_ANALYSIS_RESULTS_GEN")
    @SequenceGenerator(name = "IMAGE_ANALYSIS_RESULTS_GEN", sequenceName = "IMAGE_ANALYSIS_RESULTS_SEQ", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private String originalPath;

    private String annotatedImagePath;

    private String resolution;

    private int tablesDetected;

    private int chairsDetected;

    private int benchesDetected;

    private int couchesDetected;

    private int personsDetected;

    private int totalDetected;

    private int occupiedChairs;

    private int unoccupiedChairs;

    private int occupiedBenches;

    private int unoccupiedBenches;

    private int occupiedCouches;

    private int unoccupiedCouches;

    private int personsSitting;

    @Column(nullable = false)
    private Double confidenceThreshold;

    @Column(nullable = false)
    private LocalDateTime analysisDate;

    @Enumerated(EnumType.STRING)
    private AnalysisStatus status;

    private String errorMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;


}