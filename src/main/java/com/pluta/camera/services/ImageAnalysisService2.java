package com.pluta.camera.services;

import com.pluta.camera.dtos.AnalysisResultDTO;
import com.pluta.camera.dtos.ImageAnalysisRequest;
import com.pluta.camera.dtos.ImageAnalysisResponse;
import com.pluta.camera.dtos.PythonAnalysisResult;
import com.pluta.camera.entities.ImageAnalysisResult;
import com.pluta.camera.enums.AnalysisStatus;
import com.pluta.camera.repositories.ImageAnalysisResultRepository;
import com.pluta.camera.services.mappers.ImageAnalysisPythonMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ImageAnalysisService2 {


    private static final Logger logger = LoggerFactory.getLogger(ImageAnalysisService2.class);

    private final ImageAnalysisResultRepository analysisRepository;

    private final PythonScriptExecutor_old pythonExecutor;

    private final FileStorageService fileStorageService;

    private final ImageAnalysisPythonMapper imageAnalysisPythonMapper;

    @Value("${analysis.python.script-path}")
    private String pythonScriptPath;

    public ImageAnalysisResponse analyzeImage(ImageAnalysisRequest request){
        return null;
    }

    public void analyzeImage(List<ImageAnalysisResult> extractedFrames,  Double confidenceThreshold){
        extractedFrames.forEach(p -> analyzeImageProcessing( new File(p.getOriginalPath()), confidenceThreshold));
    }


    public ImageAnalysisResponse analyzeImageProcessing(File image, Double confidenceThreshold) {
        try {
            // Save uploaded file
            String savedFilePath = fileStorageService.saveFile(image);

            // Create analysis record
            ImageAnalysisResult analysisResult = new ImageAnalysisResult(
                    image.getName(),
                    savedFilePath
            );
            analysisResult.setConfidenceThreshold(confidenceThreshold);
            analysisResult.setStatus(AnalysisStatus.PROCESSING);
            analysisResult = analysisRepository.save(analysisResult);

            // Execute Python script
            PythonAnalysisResult pythonResult = pythonExecutor.executeAnalysis(
                    savedFilePath,
                    confidenceThreshold
            );

            // Process results
            imageAnalysisPythonMapper.updateEntityFromDTO(pythonResult,analysisResult);

            analysisResult.setStatus(AnalysisStatus.COMPLETED);
            analysisRepository.save(analysisResult);

            return createSuccessResponse(analysisResult);

        } catch (Exception e) {
            logger.error("Error analyzing image: {}", e.getMessage(), e);
            return createErrorResponse(e.getMessage());
        }
    }

    public List<ImageAnalysisResult> getAllAnalysisResults() {
        return analysisRepository.findAll();
    }

    public Optional<ImageAnalysisResult> getAnalysisResult(Long id) {
        return analysisRepository.findById(id);
    }

    public List<ImageAnalysisResult> getAnalysisResultsByStatus(AnalysisStatus status) {
        return analysisRepository.findByStatusOrderByAnalysisDateDesc(status);
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
