package com.pluta.camera.repositories.generics;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface GenericRepository <E> extends JpaRepository<E, Long>, JpaSpecificationExecutor<E> {


    Optional<E> findByIdAndTenantIdAndBranchId(Long id, Long tenantId, Long branchId);

    Page<E> findByTenantIdAndBranchId(Long tenantId, Long branchId, Pageable pageable);

    List<E> findByTenantIdAndBranchId(Long tenantId, Long branchId);

    long countByTenantIdAndBranchId(Long tenantId, Long branchId);
}
