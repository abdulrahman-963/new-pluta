package com.pluta.camera.services;



import com.pluta.camera.context.TenantContext;
import com.pluta.camera.dtos.ZoneDTO;
import com.pluta.camera.entities.Branch;
import com.pluta.camera.entities.Tenant;
import com.pluta.camera.entities.Zone;
import com.pluta.camera.exceptions.ResourceNotFoundException;
import com.pluta.camera.repositories.BranchRepository;
import com.pluta.camera.repositories.TenantRepository;
import com.pluta.camera.repositories.ZoneRepository;
import com.pluta.camera.repositories.generics.GenericRepository;
import com.pluta.camera.services.generics.TenantBranchContextService;
import com.pluta.camera.services.mappers.GenericMapper;
import com.pluta.camera.services.mappers.ZoneMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ZoneService extends TenantBranchContextService<Zone, ZoneDTO> {

    private final ZoneRepository zoneRepository;
    private final BranchRepository branchRepository;
    private final TenantRepository tenantRepository;
    private final ZoneMapper zoneMapper;

    @Override
    public GenericRepository<Zone> getGenericRepository() {
        return this.zoneRepository;
    }

    @Override
    public GenericMapper<Zone, ZoneDTO> getGenericMapper() {
        return this.zoneMapper;
    }

    public ZoneDTO findByCode(String code) {
        log.debug("Finding zone by code: {}",  code);
        Zone zone = zoneRepository.findByTenantIdAndBranchIdAndCode(TenantContext.getTenantId(), TenantContext.getBranchId(), code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Zone not found with code: %s for branch: %d", code, TenantContext.getBranchId())));
        return zoneMapper.toDTO(zone);
    }

    @Transactional
    public ZoneDTO create(ZoneDTO createDTO) {
        log.debug("Creating new zone for branch: {}", TenantContext.getBranchId());

        // Validate tenant exists
        Tenant tenant = tenantRepository.findById(TenantContext.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + TenantContext.getTenantId()));


        // Validate branch exists
        Branch branch = branchRepository.findById(TenantContext.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + TenantContext.getBranchId()));

        // Check for duplicate code within branch
        if (zoneRepository.existsByTenantIdAndBranchIdAndCode(TenantContext.getTenantId(), TenantContext.getBranchId()
                , createDTO.getCode())) {
            throw new IllegalArgumentException(
                    String.format("Zone with code '%s' already exists for branch: %d",
                            createDTO.getCode(),TenantContext.getBranchId()));
        }

        Zone zone = zoneMapper.toEntity(createDTO);
        zone.setBranch(branch);
        zone.setTenant(tenant);
        Zone savedZone = zoneRepository.save(zone);
        log.info("Created zone with id: {} for branch: {}", savedZone.getId(), branch.getId());

        return zoneMapper.toDTO(savedZone);
    }

    @Transactional
    public ZoneDTO update(Long id, ZoneDTO updateDTO) {
        log.debug("Updating zone with id: {}", id);

        Zone zone = zoneRepository.findByTenantIdAndBranchIdAndId(TenantContext.getTenantId(),
                        TenantContext.getBranchId(), id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + id));

        // Check for duplicate code if code is being updated
        if (updateDTO.getCode() != null && !updateDTO.getCode().equals(zone.getCode())) {
            if (zoneRepository.existsByTenantIdAndBranchIdAndCode(TenantContext.getTenantId(),TenantContext.getBranchId(), updateDTO.getCode())) {
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
    public void delete(Long id) {
        log.debug("Deleting zone with id: {}", id);

        Zone zone = zoneRepository.findByTenantIdAndBranchIdAndId(TenantContext.getTenantId(), TenantContext.getBranchId(), id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + id));

        zoneRepository.delete(zone);
        log.info("Deleted zone with id: {}", id);
    }



    public boolean existsById(Long id) {
        return zoneRepository.existsById(id);
    }


    public long countByTenantIdAndBranchId() {
        return zoneRepository.countByTenantIdAndBranchId(TenantContext.getTenantId(), TenantContext.getBranchId());
    }
}
