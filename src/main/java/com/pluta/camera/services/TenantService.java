package com.pluta.camera.services;


import com.pluta.camera.dtos.TenantDTO;
import com.pluta.camera.entities.Tenant;
import com.pluta.camera.enums.TenantStatus;
import com.pluta.camera.exceptions.ResourceNotFoundException;
import com.pluta.camera.repositories.TenantRepository;
import com.pluta.camera.services.mappers.TenantMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantService {

    private final TenantRepository tenantRepository;
    private final TenantMapper tenantMapper;

    public TenantDTO findById(Long id) {
        log.debug("Finding tenant by id: {}", id);
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));
        return tenantMapper.toDTO(tenant);
    }


    public Page<TenantDTO> findAll(Pageable pageable) {
        log.debug("Finding all tenants with pagination: {}", pageable);
        Page<Tenant> tenants = tenantRepository.findAll(pageable);
        return tenants.map(tenantMapper::toDTO);
    }

    public List<TenantDTO> findByStatus(TenantStatus status) {
        log.debug("Finding tenants by status: {}", status);
        List<Tenant> tenants = tenantRepository.findByStatus(status);
        return tenantMapper.toDTOList(tenants);
    }

    public TenantDTO findByCode(String code) {
        log.debug("Finding tenants by code: {}", code);
        Tenant tenant = tenantRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with code: " + code));
        return tenantMapper.toDTO(tenant);
    }

    @Transactional
    public TenantDTO create(TenantDTO createDTO) {
        log.debug("Creating new tenant: {}", createDTO.getEnglishName());

        if (tenantRepository.existsByEnglishName(createDTO.getEnglishName())) {
            throw new IllegalArgumentException("Tenant with name already exists: " + createDTO.getEnglishName());
        }

        Tenant tenant = tenantMapper.toEntity(createDTO);
        Tenant savedTenant = tenantRepository.save(tenant);
        log.info("Created tenant with id: {}", savedTenant.getId());

        return tenantMapper.toDTO(savedTenant);
    }

    @Transactional
    public TenantDTO update(Long id, TenantDTO updateDTO) {
        log.debug("Updating tenant with id: {}", id);

        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + id));

        tenantMapper.updateEntityFromDTO(updateDTO, tenant);
        Tenant updatedTenant = tenantRepository.save(tenant);
        log.info("Updated tenant with id: {}", id);

        return tenantMapper.toDTO(updatedTenant);
    }

    @Transactional
    public void delete(Long id) {
        log.debug("Deleting tenant with id: {}", id);

        if (!tenantRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tenant not found with id: " + id);
        }

        tenantRepository.deleteById(id);
        log.info("Deleted tenant with id: {}", id);
    }

    public boolean existsById(Long id) {
        return tenantRepository.existsById(id);
    }


}