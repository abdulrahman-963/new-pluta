package com.pluta.camera.services;

import com.pluta.camera.dtos.*;
import com.pluta.camera.entities.ImageAnalysisResult;
import com.pluta.camera.enums.AnalysisStatus;
import com.pluta.camera.repositories.ImageAnalysisResultRepository;
import com.pluta.camera.services.mappers.ImageAnalysisPythonMapper;
import com.pluta.camera.services.mappers.ImageAnalysisResultMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ImageAnalysisService {


    private static final Logger logger = LoggerFactory.getLogger(ImageAnalysisService.class);

    private final ImageAnalysisResultRepository analysisRepository;

    private final PythonScriptExecutor_old pythonExecutor;

    private final FileStorageService fileStorageService;

    private final ImageAnalysisPythonMapper imageAnalysisPythonMapper;

    private final ImageAnalysisResultMapper imageAnalysisResultMapper;

    @Value("${analysis.python.script-path}")
    private String pythonScriptPath;

    public ImageAnalysisResponse analyzeImage(ImageAnalysisRequest request){
        return null;
    }

    public void analyzeImage(List<ImageAnalysisResult> images,  Double confidenceThreshold){
        images.forEach(image -> analyzeImageProcessing( image, confidenceThreshold));
    }


    public void analyzeImageProcessing(ImageAnalysisResult analysisResult, Double confidenceThreshold) {
        try {

            analysisResult.setConfidenceThreshold(confidenceThreshold);
            analysisResult.setStatus(AnalysisStatus.PROCESSING);
            analysisResult = analysisRepository.save(analysisResult);

            // Execute Python script
            PythonAnalysisResult pythonResult = pythonExecutor.executeAnalysis(
                    analysisResult.getOriginalPath(),
                    confidenceThreshold
            );

            // Process results
            imageAnalysisPythonMapper.updateEntityFromDTO(pythonResult,analysisResult);

            analysisResult.setStatus(AnalysisStatus.COMPLETED);
            analysisRepository.save(analysisResult);

        } catch (Exception e) {
            logger.error("Error analyzing image: {}", e.getMessage(), e);
            analysisResult.setStatus(AnalysisStatus.FAILED);
            analysisRepository.save(analysisResult);
        }
    }

    public List<AnalysisResultDTO> getAllAnalysisResults(Long videoId, Pageable pageable) {
        return imageAnalysisResultMapper.toDTOList(analysisRepository.findByVideoId(videoId,pageable).getContent());
    }

    public Optional<ImageAnalysisResult> getAnalysisResult(Long id) {
        return analysisRepository.findById(id);
    }

    public List<AnalysisResultDTO> getAnalysisResultsByStatus(AnalysisStatus status) {
        return imageAnalysisResultMapper.toDTOList(analysisRepository.findByStatusOrderByAnalysisDateDesc(status));
    }

    private ImageAnalysisResponse createSuccessResponse(ImageAnalysisResult result) {
        ImageAnalysisResponse response = new ImageAnalysisResponse();
        response.setAnalysisId(result.getId());
        response.setFilename(result.getFilename());
        response.setStatus("SUCCESS");
        response.setMessage("Image analysis completed successfully");

        // Convert to DTO
        AnalysisResultDTO resultDTO = convertToDTO(result);

        response.setResult(resultDTO);

        return response;
    }

    private ImageAnalysisResponse createErrorResponse(String errorMessage) {
        ImageAnalysisResponse response = new ImageAnalysisResponse();
        response.setStatus("ERROR");
        response.setMessage(errorMessage);
        return response;
    }

    private AnalysisResultDTO convertToDTO(ImageAnalysisResult result) {
        AnalysisResultDTO dto = new AnalysisResultDTO();
        dto.setFilename(result.getFilename());
        dto.setResolution(result.getResolution());
        dto.setTablesDetected(result.getTablesDetected());
        dto.setChairsDetected(result.getChairsDetected());
        dto.setBenchesDetected(result.getBenchesDetected());
        dto.setCouchesDetected(result.getCouchesDetected());
        dto.setPersonsDetected(result.getPersonsDetected());
        dto.setAnnotatedImagePath(result.getAnnotatedImagePath());
        dto.setAnalysisDate(result.getAnalysisDate());

        return dto;
    }
}
