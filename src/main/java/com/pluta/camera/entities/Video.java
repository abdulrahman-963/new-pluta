package com.pluta.camera.entities;


import com.pluta.camera.enums.ProcessingStatus;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;


@Entity
@Table(name = "VIDEO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicUpdate
@DynamicInsert
public class Video  extends BaseEntity{

    public Video(String originalFileName, String fileName, Long fileSize, String contentType, String filePath
                ,Tenant tenant, Branch branch,Zone zone, Camera camera) {
        this.tenant = tenant;
        this.branch = branch;
        this.zone = zone;
        this.camera = camera;
        this.originalFileName = originalFileName;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.filePath = filePath;
        this.status = ProcessingStatus.UPLOADED;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "VIDEO_GEN")
    @SequenceGenerator(name = "VIDEO_GEN", sequenceName = "VIDEO_SEQ", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camera_id", nullable = false, foreignKey = @ForeignKey(name = "fk_video_camera"))
    private Camera camera;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false, foreignKey = @ForeignKey(name = "fk_video_zone"))
    private Zone zone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false, foreignKey = @ForeignKey(name = "fk_video_branch"))
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_video_tenant"))
    private Tenant tenant;

    @Column(name = "original_File_Name" , nullable = false)
    private String originalFileName;

    @Column(name = "file_Name" , nullable = false)
    private String fileName;

    @Column(name = "file_Size", nullable = false)
    private Long fileSize;

    @Column(name = "content_Type", nullable = false)
    private String contentType;

    @Column(name = "file_Path", nullable = false)
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessingStatus status;

    @Column(name = "duration")
    private Double duration;

    @Column(name = "frames_Extracted")
    private Integer framesExtracted;

    @Column(name = "processing_Started_At")
    private LocalDateTime processingStartedAt;

    @Column(name = "processing_Completed_At")
    private LocalDateTime processingCompletedAt;

    @Column(name = "error_Message")
    private String errorMessage;


}