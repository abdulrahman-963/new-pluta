package com.pluta.camera.entities;


import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Table;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "table_entity")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicUpdate
@DynamicInsert
public class TableEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "table_seq")
    @SequenceGenerator(name = "table_seq", sequenceName = "table_seq", allocationSize = 1)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camera_id", nullable = false, foreignKey = @ForeignKey(name = "fk_table_camera"))
    private Camera camera;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false, foreignKey = @ForeignKey(name = "fk_table_zone"))
    private Zone zone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false, foreignKey = @ForeignKey(name = "fk_table_branch"))
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_table_tenant"))
    private Tenant tenant;

    @Column(name = "table_number", nullable = false)
    private Integer tableNumber;

    @Column(name = "label", length = 100)
    private String label;

    @OneToMany(mappedBy = "table", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TableCoordinates> coordinates = new ArrayList<>();

    // Helper methods for managing coordinates
    public void addCoordinate(TableCoordinates coordinate) {
        coordinates.add(coordinate);
        coordinate.setTable(this);
    }

    public void removeCoordinate(TableCoordinates coordinate) {
        coordinates.remove(coordinate);
        coordinate.setTable(null);
    }

    public void clearCoordinates() {
        coordinates.forEach(c -> c.setTable(null));
        coordinates.clear();
    }
}