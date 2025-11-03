package com.pluta.camera.repositories;


import com.pluta.camera.entities.StreamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StreamRepository extends JpaRepository<StreamEntity, Long>, JpaSpecificationExecutor<StreamEntity> {

    Optional<StreamEntity> findByCameraId(Long cameraId);

    List<StreamEntity> findByZoneId(Long zoneId);

    List<StreamEntity> findByBranchId(Long branchId);

    List<StreamEntity> findByTenantId(Long tenantId);

    List<StreamEntity> findByTenantIdAndBranchId(Long tenantId, Long branchId);

    List<StreamEntity> findByTenantIdAndBranchIdAndZoneId(Long tenantId, Long branchId, Long zoneId);

    List<StreamEntity> findByActive(Boolean active);

    List<StreamEntity> findByTenantIdAndActive(Long tenantId, Boolean active);

    boolean existsByCameraId(Long cameraId);

    long countByZoneId(Long zoneId);

    long countByBranchId(Long branchId);

    long countByTenantId(Long tenantId);

    long countByActive(Boolean active);

    void deleteByCameraId(Long cameraId);

    void deleteByZoneId(Long zoneId);

    void deleteByBranchId(Long branchId);

    void deleteByTenantId(Long tenantId);
}