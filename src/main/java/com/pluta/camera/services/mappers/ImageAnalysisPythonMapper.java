package com.pluta.camera.services.mappers;

import com.pluta.camera.dtos.PythonAnalysisResult;
import com.pluta.camera.entities.ImageAnalysisResult;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ImageAnalysisPythonMapper extends GenericMapper<ImageAnalysisResult, PythonAnalysisResult>{
}
