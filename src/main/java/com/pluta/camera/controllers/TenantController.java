package com.pluta.camera.controllers;


import com.pluta.camera.dtos.TenantDTO;
import com.pluta.camera.enums.TenantStatus;
import com.pluta.camera.services.TenantService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tenant Management", description = "APIs for managing tenants")
public class TenantController {

    private final TenantService tenantService;

    @GetMapping("/{id}")
    @Operation(summary = "Get tenant by ID")
    public ResponseEntity<TenantDTO> getTenantById(@PathVariable Long id) {
        log.debug("REST request to get Tenant : {}", id);
        TenantDTO tenant = tenantService.findById(id);
        return ResponseEntity.ok(tenant);
    }

    @GetMapping
    @Operation(summary = "Get all tenants with pagination")
    public ResponseEntity<Page<TenantDTO>> getAllTenants(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        log.debug("REST request to get all Tenants");
        Page<TenantDTO> tenants = tenantService.findAll(pageable);
        return ResponseEntity.ok(tenants);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all tenants without pagination")
    public ResponseEntity<List<TenantDTO>> getAllTenantsNoPaging() {
        log.debug("REST request to get all Tenants without pagination");
        List<TenantDTO> tenants = tenantService.findAll();
        return ResponseEntity.ok(tenants);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get tenants by status")
    public ResponseEntity<List<TenantDTO>> getTenantsByStatus(
            @PathVariable TenantStatus status
    ) {
        log.debug("REST request to get Tenants by status : {}", status);
        List<TenantDTO> tenants = tenantService.findByStatus(status);
        return ResponseEntity.ok(tenants);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get tenants by status")
    public ResponseEntity<TenantDTO> getTenantsByCode(
            @PathVariable String code
    ) {
        log.debug("REST request to get Tenants by code : {}", code);
        TenantDTO tenant = tenantService.findByCode(code);
        return ResponseEntity.ok(tenant);
    }

    @PostMapping
    @Operation(summary = "Create a new tenant")
    public ResponseEntity<TenantDTO> createTenant(@Valid @RequestBody TenantDTO createDTO) {
        log.debug("REST request to create Tenant : {}", createDTO);
        TenantDTO createdTenant = tenantService.create(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTenant);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing tenant")
    public ResponseEntity<TenantDTO> updateTenant(
            @PathVariable Long id,
            @Valid @RequestBody TenantDTO updateDTO
    ) {
        log.debug("REST request to update Tenant : {}, {}", id, updateDTO);
        TenantDTO updatedTenant = tenantService.update(id, updateDTO);
        return ResponseEntity.ok(updatedTenant);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a tenant")
    public ResponseEntity<Void> deleteTenant(@PathVariable Long id) {
        log.debug("REST request to delete Tenant : {}", id);
        tenantService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if tenant exists")
    public ResponseEntity<Boolean> tenantExists(@PathVariable Long id) {
        log.debug("REST request to check if Tenant exists : {}", id);
        boolean exists = tenantService.existsById(id);
        return ResponseEntity.ok(exists);
    }
}