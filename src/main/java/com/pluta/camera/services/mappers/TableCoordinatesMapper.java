package com.pluta.camera.services.mappers;

import com.pluta.camera.dtos.TableCoordinatesDTO;
import com.pluta.camera.entities.TableCoordinates;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TableCoordinatesMapper extends GenericMapper<TableCoordinates, TableCoordinatesDTO>{

    @Mapping(source = "table.id", target = "tableId")
    TableCoordinatesDTO toDTO(TableCoordinates entity);
}
