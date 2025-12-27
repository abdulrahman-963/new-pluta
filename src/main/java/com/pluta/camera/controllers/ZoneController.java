package com.pluta.camera.controllers;


import com.pluta.camera.dtos.ZoneDTO;
import com.pluta.camera.services.ZoneService;
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

@RestController
@RequestMapping("/v1/zones")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Zone Management", description = "APIs for managing zones")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ZoneController {

    private final ZoneService zoneService;

    @GetMapping("/{id}")
    @Operation(summary = "Get zone by ID")
    public ResponseEntity<ZoneDTO> getZoneById(@PathVariable Long id) {
        log.debug("REST request to get Zone : {}", id);
        ZoneDTO zone = zoneService.findByIdAndTenantIdAndBranchId(id);
        return ResponseEntity.ok(zone);
    }

   @GetMapping
    @Operation(summary = "Get all zones with pagination")
    public ResponseEntity<Page<ZoneDTO>> getAllZones(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        log.debug("REST request to get all Zones");
        Page<ZoneDTO> zones = zoneService.findAllByTenantIdAndBranchId( pageable);
        return ResponseEntity.ok(zones);
    }


    @GetMapping("/code/{code}")
    @Operation(summary = "Get zone by branch ID and code")
    public ResponseEntity<ZoneDTO> getZoneByCode(
            @PathVariable Long branchId,
            @PathVariable String code
    ) {
        log.debug("REST request to get Zone by code : {}",  code);
        ZoneDTO zone = zoneService.findByCode( code);
        return ResponseEntity.ok(zone);
    }


    @PostMapping
    @Operation(summary = "Create a new zone")
    public ResponseEntity<ZoneDTO> createZone(@Valid @RequestBody ZoneDTO createDTO) {
        log.debug("REST request to create Zone : {}", createDTO);
        ZoneDTO createdZone = zoneService.create(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdZone);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing zone")
    public ResponseEntity<ZoneDTO> updateZone(
            @PathVariable Long id,
            @Valid @RequestBody ZoneDTO updateDTO
    ) {
        log.debug("REST request to update Zone : {}, {}", id, updateDTO);
        ZoneDTO updatedZone = zoneService.update(id, updateDTO);
        return ResponseEntity.ok(updatedZone);
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a zone")
    public ResponseEntity<Void> deleteZone(@PathVariable Long id) {
        log.debug("REST request to delete Zone : {}", id);
        zoneService.delete(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if zone exists")
    public ResponseEntity<Boolean> zoneExists(@PathVariable Long id) {
        log.debug("REST request to check if Zone exists : {}", id);
        boolean exists = zoneService.existsById(id);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/count")
    @Operation(summary = "Count zones for a branch")
    public ResponseEntity<Long> countZonesByBranchId() {
        log.debug("REST request to count Zones for branch ");
        long count = zoneService.countByTenantIdAndBranchId();
        return ResponseEntity.ok(count);
    }
}