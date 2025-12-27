package com.pluta.camera.controllers;


import com.pluta.camera.dtos.CameraDTO;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/cameras")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Camera Management", description = "APIs for managing cameras")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class CameraController {

    private final CameraService cameraService;

    @GetMapping("/{id}")
    @Operation(summary = "Get camera by ID")
    public ResponseEntity<CameraDTO> getCameraById(@PathVariable Long id) {
        log.debug("REST request to get Camera : {}", id);
        CameraDTO camera = cameraService.findByIdAndTenantIdAndBranchId(id);
        return ResponseEntity.ok(camera);
    }

    @GetMapping
    @Operation(summary = "Get all cameras with pagination")
    public ResponseEntity<Page<CameraDTO>> getAllCameras(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        log.debug("REST request to get all Cameras");
        Page<CameraDTO> cameras = cameraService.findAllByTenantIdAndBranchId(pageable);
        return ResponseEntity.ok(cameras);
    }

    @GetMapping("/zone/{zoneId}")
    @Operation(summary = "Get all cameras for a zone")
    public ResponseEntity<List<CameraDTO>> getCamerasByZoneId(@PathVariable Long zoneId) {
        log.debug("REST request to get Cameras for zone : {}", zoneId);
        List<CameraDTO> cameras = cameraService.findByZoneId(zoneId);
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


    @GetMapping("/zone/{zoneId}/count")
    @Operation(summary = "Count cameras for a zone")
    public ResponseEntity<Long> countCamerasByZoneId(@PathVariable Long zoneId) {
        log.debug("REST request to count Cameras for zone : {}", zoneId);
        long count = cameraService.countByZoneId(zoneId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count")
    @Operation(summary = "Count cameras for a branch")
    public ResponseEntity<Long> countCamerasByBranchId() {
        log.debug("REST request to count Cameras for branch ");
        long count = cameraService.countByTenantIdAndBranchId();
        return ResponseEntity.ok(count);
    }

}
