package com.pluta.camera.controllers;


import com.pluta.camera.dtos.StreamDTO;
import com.pluta.camera.services.StreamService;
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

//@RestController
@RequestMapping("/api/v1/streams")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Stream Management", description = "APIs for managing camera streams")
@PreAuthorize("hasRole('ADMIN')")
public class StreamController {

    private final StreamService streamService;

    @GetMapping("/{id}")
    @Operation(summary = "Get stream by ID")
    public ResponseEntity<StreamDTO> getStreamById(@PathVariable Long id) {
        log.debug("REST request to get Stream : {}", id);
        StreamDTO stream = streamService.findById(id);
        return ResponseEntity.ok(stream);
    }

    @GetMapping("/camera/{cameraId}")
    @Operation(summary = "Get stream by camera ID")
    public ResponseEntity<StreamDTO> getStreamByCameraId(@PathVariable Long cameraId) {
        log.debug("REST request to get Stream for camera : {}", cameraId);
        StreamDTO stream = streamService.findByCameraId(cameraId);
        return ResponseEntity.ok(stream);
    }

  /*  @GetMapping
    @Operation(summary = "Get all streams with pagination")
    public ResponseEntity<Page<StreamDTO>> getAllStreams(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        log.debug("REST request to get all Streams");
        Page<StreamDTO> streams = streamService.findAll(pageable);
        return ResponseEntity.ok(streams);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all streams without pagination")
    public ResponseEntity<List<StreamDTO>> getAllStreamsNoPaging() {
        log.debug("REST request to get all Streams without pagination");
        List<StreamDTO> streams = streamService.findAll();
        return ResponseEntity.ok(streams);
    }*/

    @GetMapping("/zone/{zoneId}")
    @Operation(summary = "Get all streams for a zone")
    public ResponseEntity<List<StreamDTO>> getStreamsByZoneId(@PathVariable Long zoneId) {
        log.debug("REST request to get Streams for zone : {}", zoneId);
        List<StreamDTO> streams = streamService.findByZoneId(zoneId);
        return ResponseEntity.ok(streams);
    }

    @GetMapping("/branch/{branchId}")
    @Operation(summary = "Get all streams for a branch")
    public ResponseEntity<List<StreamDTO>> getStreamsByBranchId(@PathVariable Long branchId) {
        log.debug("REST request to get Streams for branch : {}", branchId);
        List<StreamDTO> streams = streamService.findByBranchId(branchId);
        return ResponseEntity.ok(streams);
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "Get all streams for a tenant")
    public ResponseEntity<List<StreamDTO>> getStreamsByTenantId(@PathVariable Long tenantId) {
        log.debug("REST request to get Streams for tenant : {}", tenantId);
        List<StreamDTO> streams = streamService.findByTenantId(tenantId);
        return ResponseEntity.ok(streams);
    }

    @GetMapping("/tenant/{tenantId}/branch/{branchId}")
    @Operation(summary = "Get streams by tenant and branch")
    public ResponseEntity<List<StreamDTO>> getStreamsByTenantIdAndBranchId(
            @PathVariable Long tenantId,
            @PathVariable Long branchId
    ) {
        log.debug("REST request to get Streams for tenant: {} and branch: {}", tenantId, branchId);
        List<StreamDTO> streams = streamService.findByTenantIdAndBranchId(tenantId, branchId);
        return ResponseEntity.ok(streams);
    }

    @GetMapping("/tenant/{tenantId}/branch/{branchId}/zone/{zoneId}")
    @Operation(summary = "Get streams by tenant, branch and zone")
    public ResponseEntity<List<StreamDTO>> getStreamsByTenantBranchAndZone(
            @PathVariable Long tenantId,
            @PathVariable Long branchId,
            @PathVariable Long zoneId
    ) {
        log.debug("REST request to get Streams for tenant: {}, branch: {} and zone: {}",
                tenantId, branchId, zoneId);
        List<StreamDTO> streams = streamService.findByTenantIdAndBranchIdAndZoneId(tenantId, branchId, zoneId);
        return ResponseEntity.ok(streams);
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active streams")
    public ResponseEntity<List<StreamDTO>> getActiveStreams() {
        log.debug("REST request to get all active Streams");
        List<StreamDTO> streams = streamService.findByActive(true);
        return ResponseEntity.ok(streams);
    }

    @GetMapping("/inactive")
    @Operation(summary = "Get all inactive streams")
    public ResponseEntity<List<StreamDTO>> getInactiveStreams() {
        log.debug("REST request to get all inactive Streams");
        List<StreamDTO> streams = streamService.findByActive(false);
        return ResponseEntity.ok(streams);
    }

    @GetMapping("/tenant/{tenantId}/active")
    @Operation(summary = "Get active streams for a tenant")
    public ResponseEntity<List<StreamDTO>> getActiveStreamsByTenant(@PathVariable Long tenantId) {
        log.debug("REST request to get active Streams for tenant : {}", tenantId);
        List<StreamDTO> streams = streamService.findByTenantIdAndActive(tenantId, true);
        return ResponseEntity.ok(streams);
    }

    @PostMapping
    @Operation(summary = "Create a new stream")
    public ResponseEntity<StreamDTO> createStream(@Valid @RequestBody StreamDTO createDTO) {
        log.debug("REST request to create Stream : {}", createDTO);
        StreamDTO createdStream = streamService.create(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStream);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing stream")
    public ResponseEntity<StreamDTO> updateStream(
            @PathVariable Long id,
            @Valid @RequestBody StreamDTO updateDTO
    ) {
        log.debug("REST request to update Stream : {}, {}", id, updateDTO);
        StreamDTO updatedStream = streamService.update(id, updateDTO);
        return ResponseEntity.ok(updatedStream);
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate a stream")
    public ResponseEntity<StreamDTO> activateStream(@PathVariable Long id) {
        log.debug("REST request to activate Stream : {}", id);
        StreamDTO stream = streamService.activateStream(id);
        return ResponseEntity.ok(stream);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a stream")
    public ResponseEntity<StreamDTO> deactivateStream(@PathVariable Long id) {
        log.debug("REST request to deactivate Stream : {}", id);
        StreamDTO stream = streamService.deactivateStream(id);
        return ResponseEntity.ok(stream);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a stream")
    public ResponseEntity<Void> deleteStream(@PathVariable Long id) {
        log.debug("REST request to delete Stream : {}", id);
        streamService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if stream exists")
    public ResponseEntity<Boolean> streamExists(@PathVariable Long id) {
        log.debug("REST request to check if Stream exists : {}", id);
        boolean exists = streamService.existsById(id);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/camera/{cameraId}/exists")
    @Operation(summary = "Check if stream exists for camera")
    public ResponseEntity<Boolean> streamExistsForCamera(@PathVariable Long cameraId) {
        log.debug("REST request to check if Stream exists for camera : {}", cameraId);
        boolean exists = streamService.existsByCameraId(cameraId);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/zone/{zoneId}/count")
    @Operation(summary = "Count streams for a zone")
    public ResponseEntity<Long> countStreamsByZoneId(@PathVariable Long zoneId) {
        log.debug("REST request to count Streams for zone : {}", zoneId);
        long count = streamService.countByZoneId(zoneId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/branch/{branchId}/count")
    @Operation(summary = "Count streams for a branch")
    public ResponseEntity<Long> countStreamsByBranchId(@PathVariable Long branchId) {
        log.debug("REST request to count Streams for branch : {}", branchId);
        long count = streamService.countByBranchId(branchId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/tenant/{tenantId}/count")
    @Operation(summary = "Count streams for a tenant")
    public ResponseEntity<Long> countStreamsByTenantId(@PathVariable Long tenantId) {
        log.debug("REST request to count Streams for tenant : {}", tenantId);
        long count = streamService.countByTenantId(tenantId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/active/count")
    @Operation(summary = "Count active streams")
    public ResponseEntity<Long> countActiveStreams() {
        log.debug("REST request to count active Streams");
        long count = streamService.countByActive(true);
        return ResponseEntity.ok(count);
    }
}