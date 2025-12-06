package com.pluta.camera.entities;


import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.UUID;

@Entity
@Table( name = "branch")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicUpdate
@DynamicInsert
public class Branch extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "branch_seq")
    @SequenceGenerator(name = "branch_seq", sequenceName = "branch_seq", allocationSize = 1)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_branch_tenant"))
    private Tenant tenant;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "english_name", nullable = false)
    private String englishName;

    @Column(name = "arabic_name", nullable = false)
    private String arabicName;

    @Column(name = "country", nullable = false, length = 50)
    private String country;

    @Column(name = "city", nullable = false, length = 50)
    private String city;

    @Column(name = "address")
    private String address;

    @Column(name = "timezone", length = 50)
    private String timezone;

    @Column(name = "max_capacity")
    private Integer maxCapacity;
}