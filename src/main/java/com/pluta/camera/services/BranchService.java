package com.pluta.camera.services;



import com.pluta.camera.context.TenantContext;
import com.pluta.camera.dtos.BranchDTO;
import com.pluta.camera.entities.Branch;
import com.pluta.camera.entities.Tenant;
import com.pluta.camera.exceptions.ResourceNotFoundException;
import com.pluta.camera.repositories.BranchRepository;
import com.pluta.camera.repositories.TenantRepository;
import com.pluta.camera.services.mappers.BranchMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BranchService {

    private final BranchRepository branchRepository;
    private final TenantRepository tenantRepository;
    private final BranchMapper branchMapper;
    private final TokenService tokenService;


    public List<BranchDTO> getAllBranches() {
        List<Branch> branches = branchRepository.findByTenantIdAndIdIn(TenantContext.getTenantId(),tokenService.getCurrentUserBranches());
        return branchMapper.toDTOList(branches);
    }

/*
    public BranchDTO findById(Long id) {
        log.debug("Finding branch by id: {}", id);
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + id));
        return branchMapper.toDTO(branch);
    }

    public BranchDTO findByTenantIdAndId(Long tenantId, Long id) {
        log.debug("Finding branch by tenant id: {} and branch id: {}", tenantId, id);
        Branch branch = branchRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Branch not found with id: %d for tenant: %s", id, tenantId)));
        return branchMapper.toDTO(branch);
    }

    public List<BranchDTO> findAll() {
        log.debug("Finding all branches");
        List<Branch> branches = branchRepository.findAll();
        return branchMapper.toDTOList(branches);
    }

    public Page<BranchDTO> findAll(Pageable pageable) {
        log.debug("Finding all branches with pagination: {}", pageable);
        Page<Branch> branches = branchRepository.findAll(pageable);
        return branches.map(branchMapper::toDTO);
    }

    public List<BranchDTO> findByTenantId(Long tenantId) {
        log.debug("Finding branches by tenant id: {}", tenantId);
        List<Branch> branches = branchRepository.findByTenantId(tenantId);
        return branchMapper.toDTOList(branches);
    }

    public BranchDTO findByTenantIdAndCode(Long tenantId, String code) {
        log.debug("Finding branch by tenant id: {} and code: {}", tenantId, code);
        Branch branch = branchRepository.findByTenantIdAndCode(tenantId, code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Branch not found with code: %s for tenant: %s", code, tenantId)));
        return branchMapper.toDTO(branch);
    }

    public List<BranchDTO> findByCountry(String country) {
        log.debug("Finding branches by country: {}", country);
        List<Branch> branches = branchRepository.findByCountry(country);
        return branchMapper.toDTOList(branches);
    }

    public List<BranchDTO> findByCity(String city) {
        log.debug("Finding branches by city: {}", city);
        List<Branch> branches = branchRepository.findByCity(city);
        return branchMapper.toDTOList(branches);
    }

    public List<BranchDTO> findByTenantIdAndCountry(Long tenantId, String country) {
        log.debug("Finding branches by tenant id: {} and country: {}", tenantId, country);
        List<Branch> branches = branchRepository.findByTenantIdAndCountry(tenantId, country);
        return branchMapper.toDTOList(branches);
    }

    public List<BranchDTO> findByTenantIdAndCity(Long tenantId, String city) {
        log.debug("Finding branches by tenant id: {} and city: {}", tenantId, city);
        List<Branch> branches = branchRepository.findByTenantIdAndCity(tenantId, city);
        return branchMapper.toDTOList(branches);
    }

    @Transactional
    public BranchDTO create(BranchDTO createDTO) {
        log.debug("Creating new branch for tenant: {}", createDTO.getTenantId());

        // Validate tenant exists
        Tenant tenant = tenantRepository.findById(createDTO.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + createDTO.getTenantId()));

        // Check for duplicate code within tenant
        if (branchRepository.existsByTenantIdAndCode(createDTO.getTenantId(), createDTO.getCode())) {
            throw new IllegalArgumentException(
                    String.format("Branch with code '%s' already exists for tenant: %s",
                            createDTO.getCode(), createDTO.getTenantId()));
        }

        Branch branch = branchMapper.toEntity(createDTO);
        branch.setTenant(tenant);

        Branch savedBranch = branchRepository.save(branch);
        log.info("Created branch with id: {} for tenant: {}", savedBranch.getId(), tenant.getId());

        return branchMapper.toDTO(savedBranch);
    }

    @Transactional
    public BranchDTO update(Long id, BranchDTO updateDTO) {
        log.debug("Updating branch with id: {}", id);

        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + id));

        // Check for duplicate code if code is being updated
        if (updateDTO.getCode() != null && !updateDTO.getCode().equals(branch.getCode())) {
            if (branchRepository.existsByTenantIdAndCode(branch.getTenant().getId(), updateDTO.getCode())) {
                throw new IllegalArgumentException(
                        String.format("Branch with code '%s' already exists for tenant: %s",
                                updateDTO.getCode(), branch.getTenant().getId()));
            }
        }

        branchMapper.updateEntityFromDTO(updateDTO, branch);
        Branch updatedBranch = branchRepository.save(branch);
        log.info("Updated branch with id: {}", id);

        return branchMapper.toDTO(updatedBranch);
    }

    @Transactional
    public BranchDTO updateByTenantIdAndId(Long tenantId, Long id, BranchDTO updateDTO) {
        log.debug("Updating branch with id: {} for tenant: {}", id, tenantId);

        Branch branch = branchRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Branch not found with id: %d for tenant: %s", id, tenantId)));

        // Check for duplicate code if code is being updated
        if (updateDTO.getCode() != null && !updateDTO.getCode().equals(branch.getCode())) {
            if (branchRepository.existsByTenantIdAndCode(tenantId, updateDTO.getCode())) {
                throw new IllegalArgumentException(
                        String.format("Branch with code '%s' already exists for tenant: %s",
                                updateDTO.getCode(), tenantId));
            }
        }

        branchMapper.updateEntityFromDTO(updateDTO, branch);
        Branch updatedBranch = branchRepository.save(branch);
        log.info("Updated branch with id: {} for tenant: {}", id, tenantId);

        return branchMapper.toDTO(updatedBranch);
    }

    @Transactional
    public void delete(Long id) {
        log.debug("Deleting branch with id: {}", id);

        if (!branchRepository.existsById(id)) {
            throw new ResourceNotFoundException("Branch not found with id: " + id);
        }

        branchRepository.deleteById(id);
        log.info("Deleted branch with id: {}", id);
    }

    @Transactional
    public void deleteByTenantIdAndId(Long tenantId, Long id) {
        log.debug("Deleting branch with id: {} for tenant: {}", id, tenantId);

        Branch branch = branchRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Branch not found with id: %d for tenant: %s", id, tenantId)));

        branchRepository.delete(branch);
        log.info("Deleted branch with id: {} for tenant: {}", id, tenantId);
    }

    public boolean existsById(Long id) {
        return branchRepository.existsById(id);
    }

    public boolean existsByTenantIdAndCode(Long tenantId, String code) {
        return branchRepository.existsByTenantIdAndCode(tenantId, code);
    }

    public long countByTenantId(Long tenantId) {
        return branchRepository.countByTenantId(tenantId);
    }*/
}