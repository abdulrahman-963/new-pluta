package com.pluta.camera.repositories;


import com.pluta.camera.entities.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long>, JpaSpecificationExecutor<Zone> {

    List<Zone> findByBranchId(Long branchId);

    Optional<Zone> findByBranchIdAndCode(Long branchId, String code);

    Optional<Zone> findByBranchIdAndId(Long branchId, Long id);

    List<Zone> findByCode(String code);

    boolean existsByBranchIdAndCode(Long branchId, String code);

    long countByBranchId(Long branchId);

    void deleteByBranchId(Long branchId);
}