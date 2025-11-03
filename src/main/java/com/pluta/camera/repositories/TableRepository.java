package com.pluta.camera.repositories;


import com.pluta.camera.entities.TableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TableRepository extends JpaRepository<TableEntity, Long>, JpaSpecificationExecutor<TableEntity> {

    List<TableEntity> findByCameraId(Long cameraId);

    List<TableEntity> findByZoneId(Long zoneId);

    List<TableEntity> findByBranchId(Long branchId);

    List<TableEntity> findByTenantId(Long tenantId);

    List<TableEntity> findByTenantIdAndBranchId(Long tenantId, Long branchId);

    List<TableEntity> findByTenantIdAndBranchIdAndZoneId(Long tenantId, Long branchId, Long zoneId);

    List<TableEntity> findByTenantIdAndBranchIdAndZoneIdAndCameraId(Long tenantId, Long branchId, Long zoneId, Long cameraId);

    Optional<TableEntity> findByTableNumberAndCameraIdAndZoneIdAndBranchIdAndTenantId(
            Integer tableNumber, Long cameraId, Long zoneId, Long branchId, Long tenantId);

    boolean existsByTableNumberAndCameraIdAndZoneIdAndBranchIdAndTenantId(
            Integer tableNumber, Long cameraId, Long zoneId, Long branchId, Long tenantId);

    long countByCameraId(Long cameraId);

    long countByZoneId(Long zoneId);

    long countByBranchId(Long branchId);

    long countByTenantId(Long tenantId);

    void deleteByCameraId(Long cameraId);

    void deleteByZoneId(Long zoneId);

    void deleteByBranchId(Long branchId);

    void deleteByTenantId(Long tenantId);
}