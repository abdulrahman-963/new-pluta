package com.pluta.camera.entities;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "widget")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicUpdate
@DynamicInsert
public class Widget extends BaseEntity{


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "widget_seq")
    @SequenceGenerator(name = "widget_seq", sequenceName = "widget_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "widget_group_id", nullable = false, foreignKey = @ForeignKey(name = "fk_widgets_widget_group"))
    @JsonBackReference
    private WidgetGroup widgetGroup;

    @Column(nullable = false)
    private String title;

    @Column(name = "query_name", nullable = false)
    private String queryName;

    @Column(name = "hql_query",  nullable = false, columnDefinition = "TEXT")
    private String hqlQuery;

}
