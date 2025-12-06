package com.pluta.camera.entities;


import com.pluta.camera.enums.CameraStatus;
import jakarta.persistence.*;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;

@Entity
@Table(name = "camera")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicUpdate
@DynamicInsert
public class Camera extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "camera_seq")
    @SequenceGenerator(name = "camera_seq", sequenceName = "camera_seq", allocationSize = 1)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false, foreignKey = @ForeignKey(name = "fk_camera_zone"))
    private Zone zone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false, foreignKey = @ForeignKey(name = "fk_camera_branch"))
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_camera_tenant"))
    private Tenant tenant;

    @Column(name = "code", nullable = false, length = 200)
    private String code;

    @Column(name = "name", length = 200)
    private String name;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "rtsp_url")
    private String rtspUrl;

    @Column(name = "mount_position", length = 100)
    private String mountPosition;

    @Column(name = "resolution", length = 20)
    private String resolution;

    @Column(name = "fps")
    private Integer fps;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private CameraStatus status;

}