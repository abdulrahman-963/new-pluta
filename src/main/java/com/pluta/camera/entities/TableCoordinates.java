package com.pluta.camera.entities;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "table_coordinates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicUpdate
@DynamicInsert
public class TableCoordinates {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "table_coordinates_seq")
    @SequenceGenerator(name = "table_coordinates_seq", sequenceName = "table_coordinates_seq", allocationSize = 1)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false, foreignKey = @ForeignKey(name = "fk_table_coordinates_table"))
    private TableEntity table;

    @Column(name = "x", nullable = false)
    private Integer x;

    @Column(name = "y", nullable = false)
    private Integer y;
}