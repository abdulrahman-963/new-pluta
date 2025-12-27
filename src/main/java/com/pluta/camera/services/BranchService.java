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


    public List<BranchDTO> getAllBranchesByUser() {
        List<Branch> branches = branchRepository.findByTenantIdAndIdIn(TenantContext.getTenantId(),tokenService.getCurrentUserBranches());
        return branchMapper.toDTOList(branches);
    }


    public BranchDTO findById(Long id) {
        log.debug("Finding branch by id: {}", id);
        Branch branch = branchRepository.findByTenantIdAndId(TenantContext.getTenantId(), id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + id));
        return branchMapper.toDTO(branch);
    }


    public Page<BranchDTO> findAll(Pageable pageable) {
        log.debug("Finding all branches with pagination: {}", pageable);
        Page<Branch> branches = branchRepository.findByTenantId(TenantContext.getTenantId(),pageable);
        return branches.map(branchMapper::toDTO);
    }

    public BranchDTO findByCode( String code) {
        log.debug("Finding branch by tenant id: {} and code: {}", TenantContext.getTenantId(), code);
        Branch branch = branchRepository.findByTenantIdAndCode(TenantContext.getTenantId(), code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Branch not found with code: %s for tenant: %s", code, TenantContext.getTenantId())));
        return branchMapper.toDTO(branch);
    }


    @Transactional
    public BranchDTO create(BranchDTO createDTO) {
        log.debug("Creating new branch for tenant: {}", TenantContext.getTenantId());

        // Validate tenant exists
        Tenant tenant = tenantRepository.findById(TenantContext.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + TenantContext.getTenantId()));

        // Check for duplicate code within tenant
        if (branchRepository.existsByTenantIdAndCode(TenantContext.getTenantId(), createDTO.getCode())) {
            throw new IllegalArgumentException(
                    String.format("Branch with code '%s' already exists for tenant: %s",
                            createDTO.getCode(), TenantContext.getTenantId()));
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

        Branch branch = branchRepository.findByTenantIdAndId(TenantContext.getTenantId(),id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + id));

        // Check for duplicate code if code is being updated
        if (updateDTO.getCode() != null && !updateDTO.getCode().equals(branch.getCode())) {
            if (branchRepository.existsByTenantIdAndCode(TenantContext.getTenantId(), updateDTO.getCode())) {
                throw new IllegalArgumentException(
                        String.format("Branch with code '%s' already exists for tenant: %s",
                                updateDTO.getCode(), TenantContext.getTenantId()));
            }
        }

        branchMapper.updateEntityFromDTO(updateDTO, branch);
        Branch updatedBranch = branchRepository.save(branch);
        log.info("Updated branch with id: {}", id);

        return branchMapper.toDTO(updatedBranch);
    }



    @Transactional
    public void deleteById(Long id) {
        log.debug("Deleting branch with id: {} for tenant: {}", id, TenantContext.getTenantId());

        Branch branch = branchRepository.findByTenantIdAndId(TenantContext.getTenantId(), id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Branch not found with id: %d for tenant: %s", id, TenantContext.getTenantId())));

        branchRepository.delete(branch);
        log.info("Deleted branch with id: {} for tenant: {}", id, TenantContext.getTenantId());
    }


    public boolean existsByTenantIdAndCode( String code) {
        return branchRepository.existsByTenantIdAndCode(TenantContext.getTenantId(), code);
    }

    public long countByTenantId() {
        return branchRepository.countByTenantId(TenantContext.getTenantId());
    }


    public List<BranchDTO> findByCountry( String country) {
        log.debug("Finding branches by tenant id: {} and country: {}", TenantContext.getTenantId(), country);
        List<Branch> branches = branchRepository.findByTenantIdAndCountry(TenantContext.getTenantId(), country);
        return branchMapper.toDTOList(branches);
    }

    public List<BranchDTO> findByCity( String city) {
        log.debug("Finding branches by tenant id: {} and city: {}", TenantContext.getTenantId(), city);
        List<Branch> branches = branchRepository.findByTenantIdAndCity(TenantContext.getTenantId(), city);
        return branchMapper.toDTOList(branches);
    }

}