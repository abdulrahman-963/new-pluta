package com.pluta.camera.repositories;


import com.pluta.camera.entities.WidgetGroup;
import com.pluta.camera.repositories.generics.GenericRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface WidgetGroupRepository extends JpaRepository<WidgetGroup, Long>, JpaSpecificationExecutor<WidgetGroup> {

    @Query("SELECT DISTINCT qg FROM WidgetGroup qg LEFT JOIN FETCH qg.widgets WHERE qg.id = :id")
    Optional<WidgetGroup> findByIdWithWidgets(@Param("id") Long id);

}

