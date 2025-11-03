package com.pluta.camera.entities;


import com.pluta.camera.enums.TenantStatus;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.UUID;

@Entity
@Table(name = "tenant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicUpdate
@DynamicInsert
public class Tenant extends BaseEntity {

    @Id
    @GeneratedValue(generator = "tenant_gen", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "tenant_gen", sequenceName = "tenant_seq", allocationSize = 1)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "plan", length = 50)
    private String plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private TenantStatus status;


}