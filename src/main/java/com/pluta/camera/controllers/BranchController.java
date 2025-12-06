package com.pluta.camera.controllers;


import com.pluta.camera.dtos.BranchDTO;
import com.pluta.camera.services.BranchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/branches")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Branch Management", description = "APIs for managing branches")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
public class BranchController {

    private final BranchService branchService;

    @GetMapping("/all-by-user")
    @Operation(summary = "Get branches by user")
    public ResponseEntity<List<BranchDTO>> getAllBranches() {

        log.debug("REST request to get Branches by IDs ");
        List<BranchDTO> branches = branchService.getAllBranches();
        return ResponseEntity.ok(branches);
    }

    /*
    @GetMapping("/{id}")
    @Operation(summary = "Get branch by ID")
    @PreAuthorize("hasRole('SUPER-ADMIN')")
    public ResponseEntity<BranchDTO> getBranchById(@PathVariable Long id) {
        log.debug("REST request to get Branch : {}", id);
        BranchDTO branch = branchService.findById(id);
        return ResponseEntity.ok(branch);
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "Get all branches for a tenant")
    @PreAuthorize("hasRole('SUPER-ADMIN')")
    public ResponseEntity<List<BranchDTO>> getBranchesByTenantId(@PathVariable Long tenantId) {
        log.debug("REST request to get Branches for tenant : {}", tenantId);
        List<BranchDTO> branches = branchService.findByTenantId(tenantId);
        return ResponseEntity.ok(branches);
    }

    @GetMapping("/tenant/{tenantId}/code/{code}")
    @Operation(summary = "Get branch by tenant ID and code")
    @PreAuthorize("hasRole('SUPER-ADMIN')")
    public ResponseEntity<BranchDTO> getBranchByTenantIdAndCode(
            @PathVariable Long tenantId,
            @PathVariable String code
    ) {
        log.debug("REST request to get Branch by tenant : {} and code : {}", tenantId, code);
        BranchDTO branch = branchService.findByTenantIdAndCode(tenantId, code);
        return ResponseEntity.ok(branch);
    }

    @GetMapping("/tenant/{tenantId}/branch/{id}")
    @Operation(summary = "Get branch by tenant ID and branch ID")
    @PreAuthorize("hasRole('SUPER-ADMIN')")
    public ResponseEntity<BranchDTO> getBranchByTenantIdAndId(
            @PathVariable Long tenantId,
            @PathVariable Long id
    ) {
        log.debug("REST request to get Branch by tenant : {} and id : {}", tenantId, id);
        BranchDTO branch = branchService.findByTenantIdAndId(tenantId, id);
        return ResponseEntity.ok(branch);
    }

    @GetMapping("/country/{country}")
    @Operation(summary = "Get branches by country")
    @PreAuthorize("hasRole('SUPER-ADMIN')")
    public ResponseEntity<List<BranchDTO>> getBranchesByCountry(@PathVariable String country) {
        log.debug("REST request to get Branches by country : {}", country);
        List<BranchDTO> branches = branchService.findByCountry(country);
        return ResponseEntity.ok(branches);
    }

    @GetMapping("/city/{city}")
    @Operation(summary = "Get branches by city")
    @PreAuthorize("hasRole('SUPER-ADMIN')")
    public ResponseEntity<List<BranchDTO>> getBranchesByCity(@PathVariable String city) {
        log.debug("REST request to get Branches by city : {}", city);
        List<BranchDTO> branches = branchService.findByCity(city);
        return ResponseEntity.ok(branches);
    }

    @GetMapping("/tenant/{tenantId}/country/{country}")
    @Operation(summary = "Get branches by tenant ID and country")
    @PreAuthorize("hasRole('SUPER-ADMIN')")
    public ResponseEntity<List<BranchDTO>> getBranchesByTenantIdAndCountry(
            @PathVariable Long tenantId,
            @PathVariable String country
    ) {
        log.debug("REST request to get Branches by tenant : {} and country : {}", tenantId, country);
        List<BranchDTO> branches = branchService.findByTenantIdAndCountry(tenantId, country);
        return ResponseEntity.ok(branches);
    }

    @GetMapping("/tenant/{tenantId}/city/{city}")
    @Operation(summary = "Get branches by tenant ID and city")
    @PreAuthorize("hasRole('SUPER-ADMIN')")
    public ResponseEntity<List<BranchDTO>> getBranchesByTenantIdAndCity(
            @PathVariable Long tenantId,
            @PathVariable String city
    ) {
        log.debug("REST request to get Branches by tenant : {} and city : {}", tenantId, city);
        List<BranchDTO> branches = branchService.findByTenantIdAndCity(tenantId, city);
        return ResponseEntity.ok(branches);
    }

    @PostMapping
    @Operation(summary = "Create a new branch")
    @PreAuthorize("hasRole('SUPER-ADMIN')")
    public ResponseEntity<BranchDTO> createBranch(@Valid @RequestBody BranchDTO createDTO) {
        log.debug("REST request to create Branch : {}", createDTO);
        BranchDTO createdBranch = branchService.create(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBranch);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing branch")
    @PreAuthorize("hasRole('SUPER-ADMIN')")
    public ResponseEntity<BranchDTO> updateBranch(
            @PathVariable Long id,
            @Valid @RequestBody BranchDTO updateDTO
    ) {
        log.debug("REST request to update Branch : {}, {}", id, updateDTO);
        BranchDTO updatedBranch = branchService.update(id, updateDTO);
        return ResponseEntity.ok(updatedBranch);
    }

    @PutMapping("/tenant/{tenantId}/branch/{id}")
    @Operation(summary = "Update branch by tenant ID and branch ID")
    @PreAuthorize("hasRole('SUPER-ADMIN')")
    public ResponseEntity<BranchDTO> updateBranchByTenantIdAndId(
            @PathVariable Long tenantId,
            @PathVariable Long id,
            @Valid @RequestBody BranchDTO updateDTO
    ) {
        log.debug("REST request to update Branch by tenant : {} and id : {}, {}", tenantId, id, updateDTO);
        BranchDTO updatedBranch = branchService.updateByTenantIdAndId(tenantId, id, updateDTO);
        return ResponseEntity.ok(updatedBranch);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a branch")
    @PreAuthorize("hasRole('SUPER-ADMIN')")
    public ResponseEntity<Void> deleteBranch(@PathVariable Long id) {
        log.debug("REST request to delete Branch : {}", id);
        branchService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/tenant/{tenantId}/branch/{id}")
    @Operation(summary = "Delete branch by tenant ID and branch ID")
    @PreAuthorize("hasRole('SUPER-ADMIN')")
    public ResponseEntity<Void> deleteBranchByTenantIdAndId(
            @PathVariable Long tenantId,
            @PathVariable Long id
    ) {
        log.debug("REST request to delete Branch by tenant : {} and id : {}", tenantId, id);
        branchService.deleteByTenantIdAndId(tenantId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if branch exists")
    @PreAuthorize("hasRole('SUPER-ADMIN')")
    public ResponseEntity<Boolean> branchExists(@PathVariable Long id) {
        log.debug("REST request to check if Branch exists : {}", id);
        boolean exists = branchService.existsById(id);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/tenant/{tenantId}/count")
    @Operation(summary = "Count branches for a tenant")
    @PreAuthorize("hasRole('SUPER-ADMIN')")
    public ResponseEntity<Long> countBranchesByTenantId(@PathVariable Long tenantId) {
        log.debug("REST request to count Branches for tenant : {}", tenantId);
        long count = branchService.countByTenantId(tenantId);
        return ResponseEntity.ok(count);
    }*/
}