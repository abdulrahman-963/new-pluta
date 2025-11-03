package com.pluta.camera.services.mappers;

import com.pluta.camera.dtos.ZoneDTO;
import com.pluta.camera.entities.Zone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ZoneMapper extends GenericMapper<Zone, ZoneDTO>{

    @Mapping(source = "branch.id", target = "branchId")
    @Mapping(source = "branch.code", target = "branchCode")
    @Mapping(source = "tenant.id", target = "tenantId")
    @Mapping(source = "tenant.code", target = "tenantCode")
    ZoneDTO toDTO(Zone entity);

}
