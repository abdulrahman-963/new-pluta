package com.pluta.camera.services;


import com.pluta.camera.dtos.StreamDTO;
import com.pluta.camera.entities.*;
import com.pluta.camera.exceptions.ResourceNotFoundException;
import com.pluta.camera.repositories.*;
import com.pluta.camera.services.mappers.StreamMapper;
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
public class StreamService {

    private final StreamRepository streamRepository;
    private final CameraRepository cameraRepository;
    private final ZoneRepository zoneRepository;
    private final BranchRepository branchRepository;
    private final TenantRepository tenantRepository;
    private final StreamMapper streamMapper;

    public StreamDTO findById(Long id) {
        log.debug("Finding stream by id: {}", id);
        StreamEntity stream = streamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stream not found with id: " + id));
        return streamMapper.toDTO(stream);
    }

    public StreamDTO findByCameraId(Long cameraId) {
        log.debug("Finding stream by camera id: {}", cameraId);
        StreamEntity stream = streamRepository.findByCameraId(cameraId)
                .orElseThrow(() -> new ResourceNotFoundException("Stream not found for camera id: " + cameraId));
        return streamMapper.toDTO(stream);
    }

    public List<StreamDTO> findAll() {
        log.debug("Finding all streams");
        List<StreamEntity> streams = streamRepository.findAll();
        return streamMapper.toDTOList(streams);
    }

    public Page<StreamDTO> findAll(Pageable pageable) {
        log.debug("Finding all streams with pagination: {}", pageable);
        Page<StreamEntity> streams = streamRepository.findAll(pageable);
        return streams.map(streamMapper::toDTO);
    }

    public List<StreamDTO> findByZoneId(Long zoneId) {
        log.debug("Finding streams by zone id: {}", zoneId);
        List<StreamEntity> streams = streamRepository.findByZoneId(zoneId);
        return streamMapper.toDTOList(streams);
    }

    public List<StreamDTO> findByBranchId(Long branchId) {
        log.debug("Finding streams by branch id: {}", branchId);
        List<StreamEntity> streams = streamRepository.findByBranchId(branchId);
        return streamMapper.toDTOList(streams);
    }

    public List<StreamDTO> findByTenantId(Long tenantId) {
        log.debug("Finding streams by tenant id: {}", tenantId);
        List<StreamEntity> streams = streamRepository.findByTenantId(tenantId);
        return streamMapper.toDTOList(streams);
    }

    public List<StreamDTO> findByTenantIdAndBranchId(Long tenantId, Long branchId) {
        log.debug("Finding streams by tenant id: {} and branch id: {}", tenantId, branchId);
        List<StreamEntity> streams = streamRepository.findByTenantIdAndBranchId(tenantId, branchId);
        return streamMapper.toDTOList(streams);
    }

    public List<StreamDTO> findByTenantIdAndBranchIdAndZoneId(Long tenantId, Long branchId, Long zoneId) {
        log.debug("Finding streams by tenant id: {}, branch id: {} and zone id: {}", tenantId, branchId, zoneId);
        List<StreamEntity> streams = streamRepository.findByTenantIdAndBranchIdAndZoneId(tenantId, branchId, zoneId);
        return streamMapper.toDTOList(streams);
    }

    public List<StreamDTO> findByActive(Boolean active) {
        log.debug("Finding streams by active status: {}", active);
        List<StreamEntity> streams = streamRepository.findByActive(active);
        return streamMapper.toDTOList(streams);
    }

    public List<StreamDTO> findByTenantIdAndActive(Long tenantId, Boolean active) {
        log.debug("Finding streams by tenant id: {} and active status: {}", tenantId, active);
        List<StreamEntity> streams = streamRepository.findByTenantIdAndActive(tenantId, active);
        return streamMapper.toDTOList(streams);
    }

    @Transactional
    public StreamDTO create(StreamDTO createDTO) {
        log.debug("Creating new stream for tenant: {}, branch: {}, zone: {}, camera: {}",
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
        Zone zone = zoneRepository.findByTenantIdAndBranchIdAndId(createDTO.getTenantId(), createDTO.getBranchId(), createDTO.getZoneId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Zone not found with id: %d for branch: %d",
                                createDTO.getZoneId(), createDTO.getBranchId())));

        // Validate camera exists
        Camera camera = cameraRepository.findById(createDTO.getCameraId())
                .orElseThrow(() -> new ResourceNotFoundException("Camera not found with id: " + createDTO.getCameraId()));

        // Verify camera belongs to the correct zone, branch, and tenant
        if (!camera.getZone().getId().equals(createDTO.getZoneId()) ||
                !camera.getBranch().getId().equals(createDTO.getBranchId()) ||
                !camera.getTenant().getId().equals(createDTO.getTenantId())) {
            throw new IllegalArgumentException(
                    "Camera does not belong to the specified zone, branch, and tenant");
        }

        // Check if stream already exists for this camera
        if (streamRepository.existsByCameraId(createDTO.getCameraId())) {
            throw new IllegalArgumentException(
                    String.format("Stream already exists for camera id: %d", createDTO.getCameraId()));
        }

        StreamEntity stream = streamMapper.toEntity(createDTO);
        stream.setTenant(tenant);
        stream.setBranch(branch);
        stream.setZone(zone);
        stream.setCamera(camera);

        // Encrypt password if provided
        if (createDTO.getPassword() != null && !createDTO.getPassword().isEmpty()) {
           // stream.setPassword(passwordEncoder.encode(createDTO.getPassword()));
        }

        // Set defaults
        if (stream.getSamplingIntervalSeconds() == null) {
            stream.setSamplingIntervalSeconds(5);
        }
        if (stream.getActive() == null) {
            stream.setActive(true);
        }

        StreamEntity savedStream = streamRepository.save(stream);
        log.info("Created stream with id: {} for camera: {}", savedStream.getId(), camera.getId());

        return streamMapper.toDTO(savedStream);
    }

    @Transactional
    public StreamDTO update(Long id, StreamDTO updateDTO) {
        log.debug("Updating stream with id: {}", id);

        StreamEntity stream = streamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stream not found with id: " + id));

        // Update password if provided
        if (updateDTO.getPassword() != null && !updateDTO.getPassword().isEmpty()) {
            //stream.setPassword(passwordEncoder.encode(updateDTO.getPassword()));
            // Prevent MapStruct from trying to update password
            updateDTO.setPassword(null);
        }

        streamMapper.updateEntityFromDTO(updateDTO, stream);
        StreamEntity updatedStream = streamRepository.save(stream);
        log.info("Updated stream with id: {}", id);

        return streamMapper.toDTO(updatedStream);
    }

    @Transactional
    public void delete(Long id) {
        log.debug("Deleting stream with id: {}", id);

        if (!streamRepository.existsById(id)) {
            throw new ResourceNotFoundException("Stream not found with id: " + id);
        }

        streamRepository.deleteById(id);
        log.info("Deleted stream with id: {}", id);
    }

    @Transactional
    public StreamDTO activateStream(Long id) {
        log.debug("Activating stream with id: {}", id);

        StreamEntity stream = streamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stream not found with id: " + id));

        stream.setActive(true);
        StreamEntity updatedStream = streamRepository.save(stream);
        log.info("Activated stream with id: {}", id);

        return streamMapper.toDTO(updatedStream);
    }

    @Transactional
    public StreamDTO deactivateStream(Long id) {
        log.debug("Deactivating stream with id: {}", id);

        StreamEntity stream = streamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stream not found with id: " + id));

        stream.setActive(false);
        StreamEntity updatedStream = streamRepository.save(stream);
        log.info("Deactivated stream with id: {}", id);

        return streamMapper.toDTO(updatedStream);
    }

    public boolean existsById(Long id) {
        return streamRepository.existsById(id);
    }

    public boolean existsByCameraId(Long cameraId) {
        return streamRepository.existsByCameraId(cameraId);
    }

    public long countByZoneId(Long zoneId) {
        return streamRepository.countByZoneId(zoneId);
    }

    public long countByBranchId(Long branchId) {
        return streamRepository.countByBranchId(branchId);
    }

    public long countByTenantId(Long tenantId) {
        return streamRepository.countByTenantId(tenantId);
    }

    public long countByActive(Boolean active) {
        return streamRepository.countByActive(active);
    }
}