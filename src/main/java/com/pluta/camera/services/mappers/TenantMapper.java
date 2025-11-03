package com.pluta.camera.services.mappers;


import com.pluta.camera.dtos.TenantDTO;
import com.pluta.camera.entities.Tenant;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TenantMapper extends GenericMapper<Tenant, TenantDTO>{

    @Mapping(target = "id", ignore = true)
    Tenant toEntity(TenantDTO dto);

    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(TenantDTO dto, @MappingTarget Tenant entity);
}