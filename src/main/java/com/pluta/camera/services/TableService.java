package com.pluta.camera.services;

import com.pluta.camera.dtos.TableDTO;
import com.pluta.camera.entities.*;
import com.pluta.camera.exceptions.ResourceNotFoundException;
import com.pluta.camera.repositories.*;
import com.pluta.camera.services.mappers.TableCoordinatesMapper;
import com.pluta.camera.services.mappers.TableMapper;
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
public class TableService {

    private final TableRepository tableRepository;
    private final TableCoordinatesRepository coordinatesRepository;
    private final CameraRepository cameraRepository;
    private final ZoneRepository zoneRepository;
    private final BranchRepository branchRepository;
    private final TenantRepository tenantRepository;
    private final TableMapper tableMapper;
    private final TableCoordinatesMapper coordinatesMapper;

    public TableDTO findById(Long id) {
        log.debug("Finding table by id: {}", id);
        TableEntity table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found with id: " + id));
        return tableMapper.toDTO(table);
    }

    public List<TableDTO> findAll() {
        log.debug("Finding all tables");
        List<TableEntity> tables = tableRepository.findAll();
        return tableMapper.toDTOList(tables);
    }

    public Page<TableDTO> findAll(Pageable pageable) {
        log.debug("Finding all tables with pagination: {}", pageable);
        Page<TableEntity> tables = tableRepository.findAll(pageable);
        return tables.map(tableMapper::toDTO);
    }

    public List<TableDTO> findByCameraId(Long cameraId) {
        log.debug("Finding tables by camera id: {}", cameraId);
        List<TableEntity> tables = tableRepository.findByCameraId(cameraId);
        return tableMapper.toDTOList(tables);
    }

    public List<TableDTO> findByZoneId(Long zoneId) {
        log.debug("Finding tables by zone id: {}", zoneId);
        List<TableEntity> tables = tableRepository.findByZoneId(zoneId);
        return tableMapper.toDTOList(tables);
    }

    public List<TableDTO> findByBranchId(Long branchId) {
        log.debug("Finding tables by branch id: {}", branchId);
        List<TableEntity> tables = tableRepository.findByBranchId(branchId);
        return tableMapper.toDTOList(tables);
    }

    public List<TableDTO> findByTenantId(Long tenantId) {
        log.debug("Finding tables by tenant id: {}", tenantId);
        List<TableEntity> tables = tableRepository.findByTenantId(tenantId);
        return tableMapper.toDTOList(tables);
    }

    public List<TableDTO> findByTenantIdAndBranchId(Long tenantId, Long branchId) {
        log.debug("Finding tables by tenant id: {} and branch id: {}", tenantId, branchId);
        List<TableEntity> tables = tableRepository.findByTenantIdAndBranchId(tenantId, branchId);
        return tableMapper.toDTOList(tables);
    }

    public List<TableDTO> findByTenantIdAndBranchIdAndZoneId(Long tenantId, Long branchId, Long zoneId) {
        log.debug("Finding tables by tenant id: {}, branch id: {} and zone id: {}", tenantId, branchId, zoneId);
        List<TableEntity> tables = tableRepository.findByTenantIdAndBranchIdAndZoneId(tenantId, branchId, zoneId);
        return tableMapper.toDTOList(tables);
    }

    @Transactional
    public TableDTO create(TableDTO createDTO) {
        log.debug("Creating new table for tenant: {}, branch: {}, zone: {}, camera: {}",
                createDTO.getTenantId(), createDTO.getBranchId(), createDTO.getZoneId(), createDTO.getCameraId());

        // Validate tenant exists
        Tenant tenant = tenantRepository.findById(createDTO.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + createDTO.getTenantId()));

        // Validate branch exists and belongs to tenant
        Branch branch = branchRepository.findByTenantIdAndId(createDTO.getTenantId(), createDTO.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Branch not found with id: %d for tenant: %s",
                                createDTO.getBranchId(), createDTO.getTenantId())));

        // Validate zone exists and belongs to branch
        Zone zone = zoneRepository.findByBranchIdAndId(createDTO.getBranchId(), createDTO.getZoneId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Zone not found with id: %d for branch: %d",
                                createDTO.getZoneId(), createDTO.getBranchId())));

        // Validate camera exists and belongs to zone
        Camera camera = cameraRepository.findById(createDTO.getCameraId())
                .orElseThrow(() -> new ResourceNotFoundException("Camera not found with id: " + createDTO.getCameraId()));

        // Verify camera belongs to the correct zone, branch, and tenant
        if (!camera.getZone().getId().equals(createDTO.getZoneId()) ||
                !camera.getBranch().getId().equals(createDTO.getBranchId()) ||
                !camera.getTenant().getId().equals(createDTO.getTenantId())) {
            throw new IllegalArgumentException(
                    "Camera does not belong to the specified zone, branch, and tenant");
        }

        // Check for duplicate table number
        if (tableRepository.existsByTableNumberAndCameraIdAndZoneIdAndBranchIdAndTenantId(
                createDTO.getTableNumber(), createDTO.getCameraId(),
                createDTO.getZoneId(), createDTO.getBranchId(), createDTO.getTenantId())) {
            throw new IllegalArgumentException(
                    String.format("Table with number %d already exists for this camera/zone/branch/tenant combination",
                            createDTO.getTableNumber()));
        }

        // Create table entity
        TableEntity table = tableMapper.toEntity(createDTO);
        table.setTenant(tenant);
        table.setBranch(branch);
        table.setZone(zone);
        table.setCamera(camera);

        // Add coordinates
        List<TableCoordinates> coordinatesList = coordinatesMapper.toEntityList(createDTO.getCoordinates());
        coordinatesList.forEach(table::addCoordinate);

        TableEntity savedTable = tableRepository.save(table);
        log.info("Created table with id: {} for camera: {}", savedTable.getId(), camera.getId());

        return tableMapper.toDTO(savedTable);
    }

    @Transactional
    public TableDTO update(Long id, TableDTO updateDTO) {
        log.debug("Updating table with id: {}", id);

        TableEntity table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found with id: " + id));

        // Check for duplicate table number if it's being updated
        if (updateDTO.getTableNumber() != null && !updateDTO.getTableNumber().equals(table.getTableNumber())) {
            if (tableRepository.existsByTableNumberAndCameraIdAndZoneIdAndBranchIdAndTenantId(
                    updateDTO.getTableNumber(), table.getCamera().getId(),
                    table.getZone().getId(), table.getBranch().getId(), table.getTenant().getId())) {
                throw new IllegalArgumentException(
                        String.format("Table with number %d already exists for this camera/zone/branch/tenant combination",
                                updateDTO.getTableNumber()));
            }
        }

        // Update basic fields
        tableMapper.updateEntityFromDTO(updateDTO, table);

        // Update coordinates if provided
        if (updateDTO.getCoordinates() != null && !updateDTO.getCoordinates().isEmpty()) {
            table.clearCoordinates();
            List<TableCoordinates> newCoordinates = coordinatesMapper.toEntityList(updateDTO.getCoordinates());
            newCoordinates.forEach(table::addCoordinate);
        }

        TableEntity updatedTable = tableRepository.save(table);
        log.info("Updated table with id: {}", id);

        return tableMapper.toDTO(updatedTable);
    }

    @Transactional
    public void delete(Long id) {
        log.debug("Deleting table with id: {}", id);

        if (!tableRepository.existsById(id)) {
            throw new ResourceNotFoundException("Table not found with id: " + id);
        }

        tableRepository.deleteById(id);
        log.info("Deleted table with id: {}", id);
    }

    public boolean existsById(Long id) {
        return tableRepository.existsById(id);
    }

    public long countByCameraId(Long cameraId) {
        return tableRepository.countByCameraId(cameraId);
    }

    public long countByZoneId(Long zoneId) {
        return tableRepository.countByZoneId(zoneId);
    }

    public long countByBranchId(Long branchId) {
        return tableRepository.countByBranchId(branchId);
    }

    public long countByTenantId(Long tenantId) {
        return tableRepository.countByTenantId(tenantId);
    }
}
