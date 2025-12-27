package com.pluta.camera.services.mappers;

import com.pluta.camera.dtos.WidgetGroupDTO;
import com.pluta.camera.entities.WidgetGroup;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface WidgetGroupMapper extends GenericMapper<WidgetGroup, WidgetGroupDTO>{
}
