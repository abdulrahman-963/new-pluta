package com.pluta.camera.repositories.generics;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

@NoRepositoryBean
public interface GenericRepository <E> extends JpaRepository<E, Long>, JpaSpecificationExecutor<E> {


    Optional<E> findByTenantIdAndBranchIdAndId(Long tenantId, Long branchId,Long id);

    Page<E> findByTenantIdAndBranchId(Long tenantId, Long branchId, Pageable pageable);

    long countByTenantIdAndBranchId(Long tenantId, Long branchId);
}
