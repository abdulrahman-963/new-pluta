package com.pluta.camera.repositories;

import com.pluta.camera.entities.TableCoordinates;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TableCoordinatesRepository extends JpaRepository<TableCoordinates, Long> {

    List<TableCoordinates> findByTableId(Long tableId);

    void deleteByTableId(Long tableId);
}