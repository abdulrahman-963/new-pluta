package com.pluta.camera.repositories;

import com.pluta.camera.entities.Tenant;
import com.pluta.camera.enums.TenantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long>, JpaSpecificationExecutor<Tenant> {

    Optional<Tenant> findByContactEmail(String contactEmail);

    List<Tenant> findByStatus(TenantStatus status);


    boolean existsByContactEmail(String contactEmail);

    Optional<Tenant>  findByCode(String code);
}