package com.pluta.camera.services.mappers;

import com.pluta.camera.dtos.WidgetDTO;
import com.pluta.camera.entities.Widget;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface WidgetMapper extends GenericMapper<Widget, WidgetDTO>{
}
