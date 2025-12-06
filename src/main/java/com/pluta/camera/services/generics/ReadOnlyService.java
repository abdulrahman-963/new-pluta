package com.pluta.camera.services.generics;

import com.pluta.camera.context.TenantContext;
import com.pluta.camera.exceptions.ResourceNotFoundException;
import com.pluta.camera.repositories.generics.GenericRepository;
import com.pluta.camera.services.mappers.GenericMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Slf4j
public abstract class ReadOnlyService<E,D> {


    public abstract GenericRepository<E> getGenericRepository();
    public abstract GenericMapper<E,D> getGenericMapper();

    public D findById(Long id) {
        log.debug("Finding {} by id: {}",this.getClass().getName(), id);
        E entity = getGenericRepository().findByIdAndTenantIdAndBranchId(TenantContext.getTenantId(),TenantContext.getBranchId(),id)
                .orElseThrow(() -> new ResourceNotFoundException(this.getClass().getName()+" not found with id: " + id));
        return getGenericMapper().toDTO(entity);
    }

    public Page<D> findAllByTenantIdAndBranchId(Pageable pageable) {
        log.debug("Finding all {}} with pagination: {}",this.getClass().getName(), pageable);
        Page<E> entities = getGenericRepository().findByTenantIdAndBranchId(TenantContext.getTenantId(), TenantContext.getBranchId(), pageable);
        return entities.map(getGenericMapper()::toDTO);
    }


    public long countByTenantIdAndBranchId() {
        return getGenericRepository().countByTenantIdAndBranchId(TenantContext.getTenantId(), TenantContext.getBranchId());
    }

}
