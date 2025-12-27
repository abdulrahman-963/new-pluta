package com.pluta.camera.controllers;


import com.pluta.camera.context.TenantContext;
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
//@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
public class BranchController {

    private final BranchService branchService;

    @GetMapping("/all-by-user")
    @Operation(summary = "Get branches by user")
    public ResponseEntity<List<BranchDTO>> getAllBranchesByUser() {

        log.debug("REST request to get Branches by IDs ");
        List<BranchDTO> branches = branchService.getAllBranchesByUser();
        return ResponseEntity.ok(branches);
    }


    @GetMapping("/{id}")
    @Operation(summary = "Get branch by ID")
    //@PreAuthorize("hasRole('SUPER-ADMIN')")
    public ResponseEntity<BranchDTO> getBranchById(@PathVariable Long id) {
        log.debug("REST request to get Branch : {}", id);
        BranchDTO branch = branchService.findById(id);
        return ResponseEntity.ok(branch);
    }

    @GetMapping
    @Operation(summary = "Get all branches for a tenant")
    //@PreAuthorize("hasRole('SUPER-ADMIN')")
    public ResponseEntity<Page<BranchDTO>> getAllBranches( @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        log.debug("REST request to get Branches for tenant : {}", TenantContext.getTenantId());
        Page<BranchDTO> branches = branchService.findAll(pageable);
        return ResponseEntity.ok(branches);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get branch by tenant ID and code")
    @PreAuthorize("hasRole('SUPER-ADMIN')")
    public ResponseEntity<BranchDTO> getBranchByCode(
            @PathVariable String code
    ) {
        log.debug("REST request to get Branch by tenant : {} and code : {}", TenantContext.getTenantId(), code);
        BranchDTO branch = branchService.findByCode(code);
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

    @PostMapping
    @Operation(summary = "Create a new branch")
    //@PreAuthorize("hasRole('SUPER-ADMIN')")
    public ResponseEntity<BranchDTO> createBranch(@Valid @RequestBody BranchDTO createDTO) {
        log.debug("REST request to create Branch : {}", createDTO);
        BranchDTO createdBranch = branchService.create(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBranch);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing branch")
    //@PreAuthorize("hasRole('SUPER-ADMIN')")
    public ResponseEntity<BranchDTO> updateBranch(
            @PathVariable Long id,
            @Valid @RequestBody BranchDTO updateDTO
    ) {
        log.debug("REST request to update Branch : {}, {}", id, updateDTO);
        BranchDTO updatedBranch = branchService.update(id, updateDTO);
        return ResponseEntity.ok(updatedBranch);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a branch")
    //@PreAuthorize("hasRole('SUPER-ADMIN')")
    public ResponseEntity<Void> deleteBranch(@PathVariable Long id) {
        log.debug("REST request to delete Branch : {}", id);
        branchService.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/count")
    @Operation(summary = "Count branches for a tenant")
    @PreAuthorize("hasRole('SUPER-ADMIN')")
    public ResponseEntity<Long> countBranchesByTenantId() {
        log.debug("REST request to count Branches for tenant : {}", TenantContext.getTenantId());
        long count = branchService.countByTenantId();
        return ResponseEntity.ok(count);
    }
}