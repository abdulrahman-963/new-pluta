package com.pluta.camera.services.mappers;

import com.pluta.camera.dtos.TableDTO;
import com.pluta.camera.entities.TableEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        uses = {TableCoordinatesMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TableMapper extends GenericMapper<TableEntity, TableDTO>{

    @Mapping(source = "camera.id", target = "cameraId")
    @Mapping(source = "camera.code", target = "cameraCode")
    @Mapping(source = "zone.id", target = "zoneId")
    @Mapping(source = "zone.code", target = "zoneCode")
    @Mapping(source = "branch.id", target = "branchId")
    @Mapping(source = "branch.code", target = "branchCode")
    @Mapping(source = "tenant.id", target = "tenantId")
    @Mapping(source = "tenant.englishName", target = "tenantName")
    TableDTO toDTO(TableEntity entity);
}
