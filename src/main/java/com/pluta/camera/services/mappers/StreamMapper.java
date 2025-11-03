package com.pluta.camera.services.mappers;

import com.pluta.camera.dtos.StreamDTO;
import com.pluta.camera.entities.StreamEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface StreamMapper extends GenericMapper<StreamEntity, StreamDTO>{

    @Mapping(source = "camera.id", target = "cameraId")
    @Mapping(source = "camera.code", target = "cameraCode")
    @Mapping(source = "zone.id", target = "zoneId")
    @Mapping(source = "zone.code", target = "zoneCode")
    @Mapping(source = "branch.id", target = "branchId")
    @Mapping(source = "branch.code", target = "branchCode")
    @Mapping(source = "tenant.id", target = "tenantId")
    @Mapping(source = "tenant.code", target = "tenantCode")
    StreamDTO toDTO(StreamEntity entity);
}
