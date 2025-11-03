package com.pluta.camera.services.mappers;

import com.pluta.camera.dtos.FrameAnalysisResultDTO;
import com.pluta.camera.entities.Frame;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface FrameMapper extends GenericMapper<Frame, FrameAnalysisResultDTO>{

}
