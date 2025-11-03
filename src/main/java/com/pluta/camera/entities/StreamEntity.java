package com.pluta.camera.entities;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "stream")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicUpdate
@DynamicInsert
public class StreamEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stream_seq")
    @SequenceGenerator(name = "stream_seq", sequenceName = "stream_seq", allocationSize = 1)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camera_id", nullable = false, foreignKey = @ForeignKey(name = "fk_stream_camera"))
    private Camera camera;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false, foreignKey = @ForeignKey(name = "fk_stream_zone"))
    private Zone zone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false, foreignKey = @ForeignKey(name = "fk_stream_branch"))
    private Branch branch;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_stream_tenant"))
    private Tenant tenant;

    @Column(name = "url", columnDefinition = "TEXT")
    private String url;

    @Column(name = "api_key_secret_id", columnDefinition = "TEXT")
    private String apiKeySecretId;

    @Column(name = "username", length = 50)
    private String username;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "model_version", length = 50)
    private String modelVersion;

    @Column(name = "sampling_interval_s")
    @Builder.Default
    private Integer samplingIntervalSeconds = 5;

    @Column(name = "active")
    @Builder.Default
    private Boolean active = true;
}
