package com.pluta.camera.repositories;


import com.pluta.camera.entities.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long>, JpaSpecificationExecutor<Branch> {


    List<Branch> findByTenantIdAndIdIn(Long tenantId, List<Long> ids);

    Optional<Branch> findByTenantIdAndCode(Long tenantId, String code);

    Optional<Branch> findByTenantIdAndId(Long tenantId, Long id);

    List<Branch> findByTenantIdAndCountry(Long tenantId, String country);

    List<Branch> findByTenantIdAndCity(Long tenantId, String city);

    boolean existsByTenantIdAndCode(Long tenantId, String code);

    long countByTenantId(Long tenantId);

    Page<Branch> findByTenantId(Long tenantId, Pageable pageable);

}