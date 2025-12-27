package com.pluta.camera.repositories;


import com.pluta.camera.entities.Camera;
import com.pluta.camera.enums.CameraStatus;
import com.pluta.camera.repositories.generics.GenericRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CameraRepository extends GenericRepository<Camera> {

    List<Camera> findByTenantIdAndBranchIdAndZoneId(Long tenantId, Long branchId, Long zoneId);

    boolean existsByCodeAndZoneIdAndBranchIdAndTenantId(String code, Long zoneId, Long branchId, Long tenantId);

    long countByTenantIdAndBranchIdAndZoneId(Long tenantId, Long branchId, Long zoneId);

}
