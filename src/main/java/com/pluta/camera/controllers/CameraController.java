package com.pluta.camera.controllers;


import com.pluta.camera.dtos.CameraDTO;
import com.pluta.camera.enums.CameraStatus;
import com.pluta.camera.services.CameraService;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cameras")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Camera Management", description = "APIs for managing cameras")
public class CameraController {

    private final CameraService cameraService;

    @GetMapping("/{id}")
    @Operation(summary = "Get camera by ID")
    public ResponseEntity<CameraDTO> getCameraById(@PathVariable Long id) {
        log.debug("REST request to get Camera : {}", id);
        CameraDTO camera = cameraService.findById(id);
        return ResponseEntity.ok(camera);
    }

    @GetMapping
    @Operation(summary = "Get all cameras with pagination")
    public ResponseEntity<Page<CameraDTO>> getAllCameras(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        log.debug("REST request to get all Cameras");
        Page<CameraDTO> cameras = cameraService.findAll(pageable);
        return ResponseEntity.ok(cameras);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all cameras without pagination")
    public ResponseEntity<List<CameraDTO>> getAllCamerasNoPaging() {
        log.debug("REST request to get all Cameras without pagination");
        List<CameraDTO> cameras = cameraService.findAll();
        return ResponseEntity.ok(cameras);
    }

    @GetMapping("/zone/{zoneId}")
    @Operation(summary = "Get all cameras for a zone")
    public ResponseEntity<List<CameraDTO>> getCamerasByZoneId(@PathVariable Long zoneId) {
        log.debug("REST request to get Cameras for zone : {}", zoneId);
        List<CameraDTO> cameras = cameraService.findByZoneId(zoneId);
        return ResponseEntity.ok(cameras);
    }

    @GetMapping("/branch/{branchId}")
    @Operation(summary = "Get all cameras for a branch")
    public ResponseEntity<List<CameraDTO>> getCamerasByBranchId(@PathVariable Long branchId) {
        log.debug("REST request to get Cameras for branch : {}", branchId);
        List<CameraDTO> cameras = cameraService.findByBranchId(branchId);
        return ResponseEntity.ok(cameras);
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "Get all cameras for a tenant")
    public ResponseEntity<List<CameraDTO>> getCamerasByTenantId(@PathVariable Long tenantId) {
        log.debug("REST request to get Cameras for tenant : {}", tenantId);
        List<CameraDTO> cameras = cameraService.findByTenantId(tenantId);
        return ResponseEntity.ok(cameras);
    }

    @GetMapping("/tenant/{tenantId}/branch/{branchId}")
    @Operation(summary = "Get cameras by tenant and branch")
    public ResponseEntity<List<CameraDTO>> getCamerasByTenantIdAndBranchId(
            @PathVariable Long tenantId,
            @PathVariable Long branchId
    ) {
        log.debug("REST request to get Cameras for tenant: {} and branch: {}", tenantId, branchId);
        List<CameraDTO> cameras = cameraService.findByTenantIdAndBranchId(tenantId, branchId);
        return ResponseEntity.ok(cameras);
    }

    @GetMapping("/tenant/{tenantId}/branch/{branchId}/zone/{zoneId}")
    @Operation(summary = "Get cameras by tenant, branch and zone")
    public ResponseEntity<List<CameraDTO>> getCamerasByTenantBranchAndZone(
            @PathVariable Long tenantId,
            @PathVariable Long branchId,
            @PathVariable Long zoneId
    ) {
        log.debug("REST request to get Cameras for tenant: {}, branch: {} and zone: {}",
                tenantId, branchId, zoneId);
        List<CameraDTO> cameras = cameraService.findByTenantIdAndBranchIdAndZoneId(tenantId, branchId, zoneId);
        return ResponseEntity.ok(cameras);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get cameras by status")
    public ResponseEntity<List<CameraDTO>> getCamerasByStatus(@PathVariable CameraStatus status) {
        log.debug("REST request to get Cameras by status : {}", status);
        List<CameraDTO> cameras = cameraService.findByStatus(status);
        return ResponseEntity.ok(cameras);
    }

    @GetMapping("/branch/{branchId}/status/{status}")
    @Operation(summary = "Get cameras by branch and status")
    public ResponseEntity<List<CameraDTO>> getCamerasByBranchIdAndStatus(
            @PathVariable Long branchId,
            @PathVariable CameraStatus status
    ) {
        log.debug("REST request to get Cameras for branch: {} with status: {}", branchId, status);
        List<CameraDTO> cameras = cameraService.findByBranchIdAndStatus(branchId, status);
        return ResponseEntity.ok(cameras);
    }

    @PostMapping
    @Operation(summary = "Create a new camera")
    public ResponseEntity<CameraDTO> createCamera(@Valid @RequestBody CameraDTO createDTO) {
        log.debug("REST request to create Camera : {}", createDTO);
        CameraDTO createdCamera = cameraService.create(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCamera);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing camera")
    public ResponseEntity<CameraDTO> updateCamera(
            @PathVariable Long id,
            @Valid @RequestBody CameraDTO updateDTO
    ) {
        log.debug("REST request to update Camera : {}, {}", id, updateDTO);
        CameraDTO updatedCamera = cameraService.update(id, updateDTO);
        return ResponseEntity.ok(updatedCamera);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a camera")
    public ResponseEntity<Void> deleteCamera(@PathVariable Long id) {
        log.debug("REST request to delete Camera : {}", id);
        cameraService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if camera exists")
    public ResponseEntity<Boolean> cameraExists(@PathVariable Long id) {
        log.debug("REST request to check if Camera exists : {}", id);
        boolean exists = cameraService.existsById(id);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/zone/{zoneId}/count")
    @Operation(summary = "Count cameras for a zone")
    public ResponseEntity<Long> countCamerasByZoneId(@PathVariable Long zoneId) {
        log.debug("REST request to count Cameras for zone : {}", zoneId);
        long count = cameraService.countByZoneId(zoneId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/branch/{branchId}/count")
    @Operation(summary = "Count cameras for a branch")
    public ResponseEntity<Long> countCamerasByBranchId(@PathVariable Long branchId) {
        log.debug("REST request to count Cameras for branch : {}", branchId);
        long count = cameraService.countByBranchId(branchId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/tenant/{tenantId}/count")
    @Operation(summary = "Count cameras for a tenant")
    public ResponseEntity<Long> countCamerasByTenantId(@PathVariable Long tenantId) {
        log.debug("REST request to count Cameras for tenant : {}", tenantId);
        long count = cameraService.countByTenantId(tenantId);
        return ResponseEntity.ok(count);
    }
}
