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

    @Mapping(target = "coordinates", ignore = true)
    TableEntity toEntity(TableDTO dto);
}
