package com.pluta.camera.repositories;


import com.pluta.camera.entities.TableEntity;
import com.pluta.camera.repositories.generics.GenericRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TableRepository extends GenericRepository<TableEntity> {

    List<TableEntity> findByTenantIdAndBranchIdAndZoneId(Long tenantId, Long branchId, Long zoneId);

    List<TableEntity> findByTenantIdAndBranchIdAndZoneIdAndCameraId(Long tenantId, Long branchId, Long zoneId, Long cameraId);

    boolean existsByTableNumberAndCameraIdAndZoneIdAndBranchIdAndTenantId(
            Integer tableNumber, Long cameraId, Long zoneId, Long branchId, Long tenantId);

    long countByTenantIdAndBranchIdAndZoneIdAndCameraId(Long tenantId, Long branchId, Long zoneId, Long cameraId);

    long countByTenantIdAndBranchIdAndZoneId(Long tenantId, Long branchId, Long zoneId);

    long countByTenantIdAndBranchId(Long tenantId, Long branchId);
}