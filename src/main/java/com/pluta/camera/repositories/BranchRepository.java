package com.pluta.camera.repositories;


import com.pluta.camera.entities.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long>, JpaSpecificationExecutor<Branch> {

    List<Branch> findByTenantId(Long tenantId);

    Optional<Branch> findByTenantIdAndCode(Long tenantId, String code);

    Optional<Branch> findByTenantIdAndId(Long tenantId, Long id);

    List<Branch> findByCountry(String country);

    List<Branch> findByCity(String city);

    List<Branch> findByTenantIdAndCountry(Long tenantId, String country);

    List<Branch> findByTenantIdAndCity(Long tenantId, String city);

    boolean existsByTenantIdAndCode(Long tenantId, String code);

    long countByTenantId(Long tenantId);

    void deleteByTenantId(Long tenantId);
}