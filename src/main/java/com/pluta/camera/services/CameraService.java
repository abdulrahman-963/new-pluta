package com.pluta.camera.services;


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
import com.pluta.camera.services.generics.ReadOnlyService;
import com.pluta.camera.services.mappers.CameraMapper;
import com.pluta.camera.services.mappers.GenericMapper;
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
public class CameraService extends ReadOnlyService<Camera,CameraDTO> {

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

  /*  public CameraDTO findById(Long id) {
        log.debug("Finding camera by id: {}", id);
        Camera camera = cameraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Camera not found with id: " + id));
        return cameraMapper.toDTO(camera);
    }

    public List<CameraDTO> findAll() {
        log.debug("Finding all cameras");
        List<Camera> cameras = cameraRepository.findAll();
        return cameraMapper.toDTOList(cameras);
    }

    public Page<CameraDTO> findAll(Pageable pageable) {
        log.debug("Finding all cameras with pagination: {}", pageable);
        Page<Camera> cameras = cameraRepository.findAll(pageable);
        return cameras.map(cameraMapper::toDTO);
    }*/

    public List<CameraDTO> findByZoneId(Long zoneId) {
        log.debug("Finding cameras by zone id: {}", zoneId);
        List<Camera> cameras = cameraRepository.findByZoneId(zoneId);
        return cameraMapper.toDTOList(cameras);
    }
/*
    public List<CameraDTO> findByBranchId(Long branchId) {
        log.debug("Finding cameras by branch id: {}", branchId);
        List<Camera> cameras = cameraRepository.findByBranchId(branchId);
        return cameraMapper.toDTOList(cameras);
    }

    public List<CameraDTO> findByTenantId(Long tenantId) {
        log.debug("Finding cameras by tenant id: {}", tenantId);
        List<Camera> cameras = cameraRepository.findByTenantId(tenantId);
        return cameraMapper.toDTOList(cameras);
    }

    public List<CameraDTO> findByTenantIdAndBranchId(Long tenantId, Long branchId) {
        log.debug("Finding cameras by tenant id: {} and branch id: {}", tenantId, branchId);
        List<Camera> cameras = cameraRepository.findByTenantIdAndBranchId(tenantId, branchId);
        return cameraMapper.toDTOList(cameras);
    }*/

    public List<CameraDTO> findByTenantIdAndBranchIdAndZoneId(Long tenantId, Long branchId, Long zoneId) {
        log.debug("Finding cameras by tenant id: {}, branch id: {} and zone id: {}", tenantId, branchId, zoneId);
        List<Camera> cameras = cameraRepository.findByTenantIdAndBranchIdAndZoneId(tenantId, branchId, zoneId);
        return cameraMapper.toDTOList(cameras);
    }

    public List<CameraDTO> findByStatus(CameraStatus status) {
        log.debug("Finding cameras by status: {}", status);
        List<Camera> cameras = cameraRepository.findByStatus(status);
        return cameraMapper.toDTOList(cameras);
    }

    public List<CameraDTO> findByBranchIdAndStatus(Long branchId, CameraStatus status) {
        log.debug("Finding cameras by branch id: {} and status: {}", branchId, status);
        List<Camera> cameras = cameraRepository.findByBranchIdAndStatus(branchId, status);
        return cameraMapper.toDTOList(cameras);
    }

    @Transactional
    public CameraDTO create(CameraDTO createDTO) {
        log.debug("Creating new camera for tenant: {}, branch: {}, zone: {}",
                createDTO.getTenantId(), createDTO.getBranchId(), createDTO.getZoneId());

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

        // Check for duplicate code
        if (cameraRepository.existsByCodeAndZoneIdAndBranchIdAndTenantId(
                createDTO.getCode(), createDTO.getZoneId(),
                createDTO.getBranchId(), createDTO.getTenantId())) {
            throw new IllegalArgumentException(
                    String.format("Camera with code '%s' already exists for tenant: %s, branch: %d, zone: %d",
                            createDTO.getCode(), createDTO.getTenantId(),
                            createDTO.getBranchId(), createDTO.getZoneId()));
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

        Camera camera = cameraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Camera not found with id: " + id));

        // Check for duplicate code if code is being updated
        if (updateDTO.getCode() != null && !updateDTO.getCode().equals(camera.getCode())) {
            if (cameraRepository.existsByCodeAndZoneIdAndBranchIdAndTenantId(
                    updateDTO.getCode(), camera.getZone().getId(),
                    camera.getBranch().getId(), camera.getTenant().getId())) {
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

        if (!cameraRepository.existsById(id)) {
            throw new ResourceNotFoundException("Camera not found with id: " + id);
        }

        cameraRepository.deleteById(id);
        log.info("Deleted camera with id: {}", id);
    }

    public boolean existsById(Long id) {
        return cameraRepository.existsById(id);
    }

    public long countByZoneId(Long zoneId) {
        return cameraRepository.countByZoneId(zoneId);
    }

/*
    public long countByBranchId(Long branchId) {
        return cameraRepository.countByBranchId(branchId);
    }

    public long countByTenantId(Long tenantId) {
        return cameraRepository.countByTenantId(tenantId);
    }*/
}
