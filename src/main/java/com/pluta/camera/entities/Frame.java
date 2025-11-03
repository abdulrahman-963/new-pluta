package com.pluta.camera.entities;

import com.pluta.camera.enums.AnalysisStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "frame")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicUpdate
@DynamicInsert
public class Frame extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "frame_seq")
    @SequenceGenerator(name = "frame_seq", sequenceName = "frame_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "annotated_image_path")
    private String annotatedImagePath;

    @Column(name = "resolution")
    private String resolution;

    @Column(name = "tables_detected")
    private int tablesDetected;

    @Column(name = "chairs_detected")
    private int chairsDetected;

    @Column(name = "benches_detected")
    private int benchesDetected;

    @Column(name = "couches_detected")
    private int couchesDetected;

    @Column(name = "persons_detected")
    private int personsDetected;

    @Column(name = "total_detected")
    private int totalDetected;

    @Column(name = "occupied_chairs")
    private int occupiedChairs;

    @Column(name = "unoccupied_chairs")
    private int unoccupiedChairs;

    @Column(name = "occupied_benches")
    private int occupiedBenches;

    @Column(name = "unoccupied_benches")
    private int unoccupiedBenches;

    @Column(name = "occupied_couches")
    private int occupiedCouches;

    @Column(name = "unoccupied_couches")
    private int unoccupiedCouches;

    @Column(name = "persons_sitting")
    private int personsSitting;

    @Column(name = "confidence_threshold", nullable = false)
    private Double confidenceThreshold;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AnalysisStatus status;

    @Column(name = "error_message")
    private String errorMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", foreignKey = @ForeignKey(name = "fk_frame_video"))
    private Video video;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stream_id", foreignKey = @ForeignKey(name = "fk_frame_stream"))
    private StreamEntity stream;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_frame_tenant"))
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false, foreignKey = @ForeignKey(name = "fk_frame_branch"))
    private Branch branch;
}
