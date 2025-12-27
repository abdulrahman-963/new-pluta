package com.pluta.camera.services;

import com.pluta.camera.context.TenantContext;
import com.pluta.camera.dtos.TableDTO;
import com.pluta.camera.entities.*;
import com.pluta.camera.exceptions.ResourceNotFoundException;
import com.pluta.camera.repositories.*;
import com.pluta.camera.repositories.generics.GenericRepository;
import com.pluta.camera.services.generics.TenantBranchContextService;
import com.pluta.camera.services.mappers.GenericMapper;
import com.pluta.camera.services.mappers.TableCoordinatesMapper;
import com.pluta.camera.services.mappers.TableMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TableService extends TenantBranchContextService<TableEntity, TableDTO> {

    private final TableRepository tableRepository;
    private final TableCoordinatesRepository coordinatesRepository;
    private final CameraRepository cameraRepository;
    private final ZoneRepository zoneRepository;
    private final BranchRepository branchRepository;
    private final TenantRepository tenantRepository;
    private final TableMapper tableMapper;
    private final TableCoordinatesMapper coordinatesMapper;


    @Override
    public GenericRepository<TableEntity> getGenericRepository() {
        return this.tableRepository;
    }

    @Override
    public GenericMapper<TableEntity, TableDTO> getGenericMapper() {
        return this.tableMapper;
    }

    public List<TableDTO> findByTenantIdAndBranchIdAndZoneIdAndCameraId(Long zoneId, Long cameraId) {
        log.debug("Finding tables by camera id: {}", cameraId);
        List<TableEntity> tables = tableRepository.findByTenantIdAndBranchIdAndZoneIdAndCameraId(TenantContext.getTenantId(), TenantContext.getBranchId(), zoneId, cameraId);
        return tableMapper.toDTOList(tables);
    }

    public List<TableDTO> findByTenantIdAndBranchIdAndZoneId(Long zoneId) {
        log.debug("Finding tables by zone id: {}", zoneId);
        List<TableEntity> tables = tableRepository.findByTenantIdAndBranchIdAndZoneId(TenantContext.getTenantId(), TenantContext.getBranchId(),zoneId);
        return tableMapper.toDTOList(tables);
    }

    @Transactional
    public TableDTO create(TableDTO createDTO) {
        log.debug("Creating new table for tenant: {}, branch: {}, zone: {}, camera: {}",
                TenantContext.getTenantId(), TenantContext.getBranchId(), createDTO.getZoneId(), createDTO.getCameraId());

        // Validate tenant exists
        Tenant tenant = tenantRepository.findById(TenantContext.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + TenantContext.getTenantId()));

        // Validate branch exists and belongs to tenant
        Branch branch = branchRepository.findByTenantIdAndId(TenantContext.getTenantId(), TenantContext.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Branch not found with id: %d for tenant: %s",
                                TenantContext.getBranchId(), TenantContext.getTenantId())));

        // Validate zone exists and belongs to branch
        Zone zone = zoneRepository.findByTenantIdAndBranchIdAndId(TenantContext.getTenantId(), TenantContext.getBranchId(), createDTO.getZoneId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Zone not found with id: %d for branch: %d",
                                createDTO.getZoneId(), TenantContext.getBranchId())));

        // Validate camera exists and belongs to zone
        Camera camera = cameraRepository.findByTenantIdAndBranchIdAndId(TenantContext.getTenantId(), TenantContext.getBranchId(), createDTO.getCameraId())
                .orElseThrow(() -> new ResourceNotFoundException("Camera not found with id: " + createDTO.getCameraId()));

        // Verify camera belongs to the correct zone, branch, and tenant
        if (!camera.getZone().getId().equals(createDTO.getZoneId()) ||
                !camera.getBranch().getId().equals(TenantContext.getBranchId()) ||
                !camera.getTenant().getId().equals(TenantContext.getTenantId())) {
            throw new IllegalArgumentException(
                    "Camera does not belong to the specified zone, branch, and tenant");
        }

        // Check for duplicate table number
        if (tableRepository.existsByTableNumberAndCameraIdAndZoneIdAndBranchIdAndTenantId(
                createDTO.getTableNumber(), createDTO.getCameraId(),
                createDTO.getZoneId(), TenantContext.getBranchId(), TenantContext.getTenantId())) {
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

        TableEntity table = tableRepository.findByTenantIdAndBranchIdAndId(TenantContext.getTenantId(), TenantContext.getBranchId(),id)
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


        TableEntity table = tableRepository.findByTenantIdAndBranchIdAndId(TenantContext.getTenantId(), TenantContext.getBranchId(),id)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found with id: " + id));


        tableRepository.delete(table);
        log.info("Deleted table with id: {}", id);
    }

    public long countByTenantIdAndBranchIdAndZoneIdAndCameraId(Long zoneId, Long cameraId) {
        return tableRepository.countByTenantIdAndBranchIdAndZoneIdAndCameraId(TenantContext.getTenantId(), TenantContext.getBranchId(), zoneId, cameraId);
    }

    public long countByTenantIdAndBranchIdAndZoneId(Long zoneId) {
        return tableRepository.countByTenantIdAndBranchIdAndZoneId(TenantContext.getTenantId(), TenantContext.getBranchId(), zoneId);
    }

    public long countByTenantIdAndBranchId() {
        return tableRepository.countByTenantIdAndBranchId(TenantContext.getTenantId(), TenantContext.getBranchId());
    }



}
