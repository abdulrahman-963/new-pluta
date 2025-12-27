package com.pluta.camera.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "widget_group")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicUpdate
@DynamicInsert
public class WidgetGroup extends BaseEntity{


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "widget_group_seq")
    @SequenceGenerator(name = "widget_group_seq", sequenceName = "widget_group_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_public")
    private Boolean isPublic;

    @OneToMany(mappedBy = "widgetGroup", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Widget> widgets = new ArrayList<>();

    // Helper methods
   public void addWidget(Widget widget) {
             widgets.add(widget);
               widget.setWidgetGroup(this);
   }
   public void removeWidget(Widget widget) {
               widgets.remove(widget);
               widget.setWidgetGroup(null);
   }
}