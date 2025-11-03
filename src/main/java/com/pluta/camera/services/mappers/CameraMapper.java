package com.pluta.camera.services.mappers;


import com.pluta.camera.dtos.CameraDTO;
import com.pluta.camera.entities.Camera;
import org.mapstruct.*;


@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CameraMapper extends GenericMapper<Camera, CameraDTO>{

    @Mapping(source = "zone.id", target = "zoneId")
    @Mapping(source = "zone.code", target = "zoneCode")
    @Mapping(source = "branch.id", target = "branchId")
    @Mapping(source = "branch.code", target = "branchCode")
    @Mapping(source = "tenant.id", target = "tenantId")
    CameraDTO toDTO(Camera entity);


}