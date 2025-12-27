package com.pluta.camera.controllers;


import com.pluta.camera.dtos.TableDTO;
import com.pluta.camera.services.TableService;
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
@RequestMapping("/api/v1/tables")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Table Management", description = "APIs for managing tables with coordinates")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class TableController {

    private final TableService tableService;

    @GetMapping("/{id}")
    @Operation(summary = "Get table by ID")
    public ResponseEntity<TableDTO> getTableById(@PathVariable Long id) {
        log.debug("REST request to get Table : {}", id);
        TableDTO table = tableService.findByIdAndTenantIdAndBranchId(id);
        return ResponseEntity.ok(table);
    }


    @GetMapping
    @Operation(summary = "Get all tables with pagination")
    public ResponseEntity<Page<TableDTO>> getAllTables(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        log.debug("REST request to get all Tables");
        Page<TableDTO> tables = tableService.findAllByTenantIdAndBranchId(pageable);
        return ResponseEntity.ok(tables);
    }

    @GetMapping("/zone/{zoneId}/camera/{cameraId}")
    @Operation(summary = "Get all tables for a camera")
    public ResponseEntity<List<TableDTO>> getTablesByCameraId(@PathVariable Long zoneId, @PathVariable Long cameraId) {
        log.debug("REST request to get Tables for camera : {}", cameraId);
        List<TableDTO> tables = tableService.findByTenantIdAndBranchIdAndZoneIdAndCameraId(zoneId, cameraId);
        return ResponseEntity.ok(tables);
    }

    @GetMapping("/zone/{zoneId}")
    @Operation(summary = "Get all tables for a zone")
    public ResponseEntity<List<TableDTO>> getTablesByZoneId(@PathVariable Long zoneId) {
        log.debug("REST request to get Tables for zone : {}", zoneId);
        List<TableDTO> tables = tableService.findByTenantIdAndBranchIdAndZoneId(zoneId);
        return ResponseEntity.ok(tables);
    }


    @PostMapping
    @Operation(summary = "Create a new table with coordinates")
    public ResponseEntity<TableDTO> createTable(@Valid @RequestBody TableDTO createDTO) {
        log.debug("REST request to create Table : {}", createDTO);
        TableDTO createdTable = tableService.create(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTable);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing table")
    public ResponseEntity<TableDTO> updateTable(
            @PathVariable Long id,
            @Valid @RequestBody TableDTO updateDTO
    ) {
        log.debug("REST request to update Table : {}, {}", id, updateDTO);
        TableDTO updatedTable = tableService.update(id, updateDTO);
        return ResponseEntity.ok(updatedTable);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a table")
    public ResponseEntity<Void> deleteTable(@PathVariable Long id) {
        log.debug("REST request to delete Table : {}", id);
        tableService.delete(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/zone/{zoneId}/camera/{cameraId}/count")
    @Operation(summary = "Count tables for a camera")
    public ResponseEntity<Long> countTablesByCameraId(@PathVariable Long zoneId, @PathVariable Long cameraId) {
        log.debug("REST request to count Tables for camera : {}", cameraId);
        long count = tableService.countByTenantIdAndBranchIdAndZoneIdAndCameraId(zoneId, cameraId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/zone/{zoneId}/count")
    @Operation(summary = "Count tables for a zone")
    public ResponseEntity<Long> countTablesByZoneId(@PathVariable Long zoneId) {
        log.debug("REST request to count Tables for zone : {}", zoneId);
        long count = tableService.countByTenantIdAndBranchIdAndZoneId(zoneId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count-by-branch")
    @Operation(summary = "Count tables for a branch")
    public ResponseEntity<Long> countTablesByBranchId(@PathVariable Long branchId) {
        log.debug("REST request to count Tables for branch : {}", branchId);
        long count = tableService.countByTenantIdAndBranchId();
        return ResponseEntity.ok(count);
    }


}