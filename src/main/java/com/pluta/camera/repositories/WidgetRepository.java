package com.pluta.camera.repositories;


import com.pluta.camera.entities.Widget;
import com.pluta.camera.entities.WidgetGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WidgetRepository extends JpaRepository<Widget, Long>, JpaSpecificationExecutor<Widget> {

    Optional<Widget> findByQueryName(String queryName);

    Optional<Widget> findByIdAndWidgetGroupId(Long id, Long widgetGroupId);

    List<Widget> findByWidgetGroupId(Long widgetGroupId);

    @Query("SELECT COUNT(w) FROM Widget w WHERE w.widgetGroup.id = :widgetGroupId")
    long countByWidgetGroupId(@Param("widgetGroupId") Long widgetGroupId);
}