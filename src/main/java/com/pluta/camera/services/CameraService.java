package com.pluta.camera.services;


import com.pluta.camera.context.TenantContext;
import com.pluta.camera.dtos.CameraDTO;
import com.pluta.camera.entities.Branch;
import com.pluta.camera.entities.Camera;
import com.pluta.camera.entities.Tenant;
import com.pluta.camera.entities.Zone;
import com.pluta.camera.enums.CameraStatus;
import com.pluta.camera.exceptions.ResourceNotFoundException;
import com.pluta.camera.repositories.BranchRepository;
import com.pluta.camera.repositories.CameraRepository;
import com.pluta.camera.repositories.TenantRepository;
import com.pluta.camera.repositories.ZoneRepository;
import com.pluta.camera.repositories.generics.GenericRepository;
import com.pluta.camera.services.generics.TenantBranchContextService;
import com.pluta.camera.services.mappers.CameraMapper;
import com.pluta.camera.services.mappers.GenericMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CameraService extends TenantBranchContextService<Camera,CameraDTO> {

    private final CameraRepository cameraRepository;
    private final ZoneRepository zoneRepository;
    private final BranchRepository branchRepository;
    private final TenantRepository tenantRepository;
    private final CameraMapper cameraMapper;


    @Override
    public GenericRepository<Camera> getGenericRepository() {
        return this.cameraRepository;
    }

    @Override
    public GenericMapper<Camera, CameraDTO> getGenericMapper() {
        return this.cameraMapper;
    }


    public List<CameraDTO> findByZoneId(Long zoneId) {
        log.debug("Finding cameras by zone id: {}", zoneId);
        List<Camera> cameras = cameraRepository.findByTenantIdAndBranchIdAndZoneId(TenantContext.getTenantId(), TenantContext.getBranchId(), zoneId);
        return cameraMapper.toDTOList(cameras);
    }

    @Transactional
    public CameraDTO create(CameraDTO createDTO) {
        log.debug("Creating new camera for tenant: {}, branch: {}, zone: {}",
                TenantContext.getTenantId(), TenantContext.getBranchId(), createDTO.getZoneId());

        // Validate tenant exists
        Tenant tenant = tenantRepository.findById(TenantContext.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + TenantContext.getTenantId()));

        // Validate branch exists and belongs to tenant
        Branch branch = branchRepository.findByTenantIdAndId(TenantContext.getTenantId(), TenantContext.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Branch not found with id: %d for tenant: %s",
                                TenantContext.getBranchId(), TenantContext.getTenantId())));

        // Validate zone exists and belongs to branch
        Zone zone = zoneRepository.findByTenantIdAndBranchIdAndId(TenantContext.getTenantId(),TenantContext.getBranchId(), createDTO.getZoneId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Zone not found with id: %d for branch: %d",
                                createDTO.getZoneId(), TenantContext.getBranchId())));

        // Check for duplicate code
        if (cameraRepository.existsByCodeAndZoneIdAndBranchIdAndTenantId(
                createDTO.getCode(), createDTO.getZoneId(),
                TenantContext.getBranchId(), TenantContext.getTenantId())) {
            throw new IllegalArgumentException(
                    String.format("Camera with code '%s' already exists for tenant: %s, branch: %d, zone: %d",
                            createDTO.getCode(), TenantContext.getTenantId(),
                            TenantContext.getBranchId(), createDTO.getZoneId()));
        }

        Camera camera = cameraMapper.toEntity(createDTO);
        camera.setTenant(tenant);
        camera.setBranch(branch);
        camera.setZone(zone);

        // Set default status if not provided
        if (camera.getStatus() == null) {
            camera.setStatus(CameraStatus.ACTIVE);
        }

        Camera savedCamera = cameraRepository.save(camera);
        log.info("Created camera with id: {} for zone: {}", savedCamera.getId(), zone.getId());

        return cameraMapper.toDTO(savedCamera);
    }

    @Transactional
    public CameraDTO update(Long id, CameraDTO updateDTO) {
        log.debug("Updating camera with id: {}", id);

        Camera camera = cameraRepository.findByTenantIdAndBranchIdAndId(TenantContext.getTenantId(),TenantContext.getBranchId(), id)
                .orElseThrow(() -> new ResourceNotFoundException("Camera not found with id: " + id));

        // Check for duplicate code if code is being updated
        if (updateDTO.getCode() != null && !updateDTO.getCode().equals(camera.getCode())) {
            if (cameraRepository.existsByCodeAndZoneIdAndBranchIdAndTenantId(
                    updateDTO.getCode(), camera.getZone().getId(),
                    TenantContext.getBranchId(), TenantContext.getTenantId())) {
                throw new IllegalArgumentException(
                        String.format("Camera with code '%s' already exists for this tenant/branch/zone combination",
                                updateDTO.getCode()));
            }
        }

        cameraMapper.updateEntityFromDTO(updateDTO, camera);
        Camera updatedCamera = cameraRepository.save(camera);
        log.info("Updated camera with id: {}", id);

        return cameraMapper.toDTO(updatedCamera);
    }

    @Transactional
    public void delete(Long id) {
        log.debug("Deleting camera with id: {}", id);

        Camera camera = cameraRepository.findByTenantIdAndBranchIdAndId(TenantContext.getTenantId(),TenantContext.getBranchId(), id)
                .orElseThrow(() -> new ResourceNotFoundException("Camera not found with id: " + id));

        cameraRepository.delete(camera);
        log.info("Deleted camera with id: {}", id);
    }

    public long countByZoneId(Long zoneId) {
        return cameraRepository.countByTenantIdAndBranchIdAndZoneId(TenantContext.getTenantId(),TenantContext.getBranchId(),zoneId);
    }


    public long countByBranchId(){
        return cameraRepository.countByTenantIdAndBranchId(TenantContext.getTenantId(),TenantContext.getBranchId());
    }

}
