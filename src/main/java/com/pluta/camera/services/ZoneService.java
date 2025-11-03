package com.pluta.camera.services;



import com.pluta.camera.dtos.ZoneDTO;
import com.pluta.camera.entities.Branch;
import com.pluta.camera.entities.Zone;
import com.pluta.camera.exceptions.ResourceNotFoundException;
import com.pluta.camera.repositories.BranchRepository;
import com.pluta.camera.repositories.ZoneRepository;
import com.pluta.camera.services.mappers.ZoneMapper;
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
public class ZoneService {

    private final ZoneRepository zoneRepository;
    private final BranchRepository branchRepository;
    private final ZoneMapper zoneMapper;

    public ZoneDTO findById(Long id) {
        log.debug("Finding zone by id: {}", id);
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + id));
        return zoneMapper.toDTO(zone);
    }

    public ZoneDTO findByBranchIdAndId(Long branchId, Long id) {
        log.debug("Finding zone by branch id: {} and zone id: {}", branchId, id);
        Zone zone = zoneRepository.findByBranchIdAndId(branchId, id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Zone not found with id: %d for branch: %d", id, branchId)));
        return zoneMapper.toDTO(zone);
    }

    public List<ZoneDTO> findAll() {
        log.debug("Finding all zones");
        List<Zone> zones = zoneRepository.findAll();
        return zoneMapper.toDTOList(zones);
    }

    public Page<ZoneDTO> findAll(Pageable pageable) {
        log.debug("Finding all zones with pagination: {}", pageable);
        Page<Zone> zones = zoneRepository.findAll(pageable);
        return zones.map(zoneMapper::toDTO);
    }

    public List<ZoneDTO> findByBranchId(Long branchId) {
        log.debug("Finding zones by branch id: {}", branchId);
        List<Zone> zones = zoneRepository.findByBranchId(branchId);
        return zoneMapper.toDTOList(zones);
    }

    public ZoneDTO findByBranchIdAndCode(Long branchId, String code) {
        log.debug("Finding zone by branch id: {} and code: {}", branchId, code);
        Zone zone = zoneRepository.findByBranchIdAndCode(branchId, code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Zone not found with code: %s for branch: %d", code, branchId)));
        return zoneMapper.toDTO(zone);
    }

    public List<ZoneDTO> findByCode(String code) {
        log.debug("Finding zones by code: {}", code);
        List<Zone> zones = zoneRepository.findByCode(code);
        return zoneMapper.toDTOList(zones);
    }

    @Transactional
    public ZoneDTO create(ZoneDTO createDTO) {
        log.debug("Creating new zone for branch: {}", createDTO.getBranchId());

        // Validate branch exists
        Branch branch = branchRepository.findById(createDTO.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + createDTO.getBranchId()));

        // Check for duplicate code within branch
        if (zoneRepository.existsByBranchIdAndCode(createDTO.getBranchId(), createDTO.getCode())) {
            throw new IllegalArgumentException(
                    String.format("Zone with code '%s' already exists for branch: %d",
                            createDTO.getCode(), createDTO.getBranchId()));
        }

        Zone zone = zoneMapper.toEntity(createDTO);
        zone.setBranch(branch);

        Zone savedZone = zoneRepository.save(zone);
        log.info("Created zone with id: {} for branch: {}", savedZone.getId(), branch.getId());

        return zoneMapper.toDTO(savedZone);
    }

    @Transactional
    public ZoneDTO update(Long id, ZoneDTO updateDTO) {
        log.debug("Updating zone with id: {}", id);

        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + id));

        // Check for duplicate code if code is being updated
        if (updateDTO.getCode() != null && !updateDTO.getCode().equals(zone.getCode())) {
            if (zoneRepository.existsByBranchIdAndCode(zone.getBranch().getId(), updateDTO.getCode())) {
                throw new IllegalArgumentException(
                        String.format("Zone with code '%s' already exists for branch: %d",
                                updateDTO.getCode(), zone.getBranch().getId()));
            }
        }

        zoneMapper.updateEntityFromDTO(updateDTO, zone);
        Zone updatedZone = zoneRepository.save(zone);
        log.info("Updated zone with id: {}", id);

        return zoneMapper.toDTO(updatedZone);
    }

    @Transactional
    public ZoneDTO updateByBranchIdAndId(Long branchId, Long id, ZoneDTO updateDTO) {
        log.debug("Updating zone with id: {} for branch: {}", id, branchId);

        Zone zone = zoneRepository.findByBranchIdAndId(branchId, id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Zone not found with id: %d for branch: %d", id, branchId)));

        // Check for duplicate code if code is being updated
        if (updateDTO.getCode() != null && !updateDTO.getCode().equals(zone.getCode())) {
            if (zoneRepository.existsByBranchIdAndCode(branchId, updateDTO.getCode())) {
                throw new IllegalArgumentException(
                        String.format("Zone with code '%s' already exists for branch: %d",
                                updateDTO.getCode(), branchId));
            }
        }

        zoneMapper.updateEntityFromDTO(updateDTO, zone);
        Zone updatedZone = zoneRepository.save(zone);
        log.info("Updated zone with id: {} for branch: {}", id, branchId);

        return zoneMapper.toDTO(updatedZone);
    }

    @Transactional
    public void delete(Long id) {
        log.debug("Deleting zone with id: {}", id);

        if (!zoneRepository.existsById(id)) {
            throw new ResourceNotFoundException("Zone not found with id: " + id);
        }

        zoneRepository.deleteById(id);
        log.info("Deleted zone with id: {}", id);
    }

    @Transactional
    public void deleteByBranchIdAndId(Long branchId, Long id) {
        log.debug("Deleting zone with id: {} for branch: {}", id, branchId);

        Zone zone = zoneRepository.findByBranchIdAndId(branchId, id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Zone not found with id: %d for branch: %d", id, branchId)));

        zoneRepository.delete(zone);
        log.info("Deleted zone with id: {} for branch: {}", id, branchId);
    }

    public boolean existsById(Long id) {
        return zoneRepository.existsById(id);
    }

    public boolean existsByBranchIdAndCode(Long branchId, String code) {
        return zoneRepository.existsByBranchIdAndCode(branchId, code);
    }

    public long countByBranchId(Long branchId) {
        return zoneRepository.countByBranchId(branchId);
    }
}
