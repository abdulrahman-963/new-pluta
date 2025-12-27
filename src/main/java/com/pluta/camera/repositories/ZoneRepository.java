package com.pluta.camera.repositories;


import com.pluta.camera.entities.Zone;
import com.pluta.camera.repositories.generics.GenericRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ZoneRepository extends GenericRepository<Zone> {

    Optional<Zone> findByTenantIdAndBranchIdAndCode(Long tenantId, Long branchId, String code);

    boolean existsByTenantIdAndBranchIdAndCode(Long tenantId, Long branchId, String code);

    long countByTenantIdAndBranchId(Long tenantId,Long branchId);
}