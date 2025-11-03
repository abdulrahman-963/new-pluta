package com.pluta.camera.controllers;

import com.pluta.camera.dtos.AnalysisResultDTO;
import com.pluta.camera.dtos.ImageAnalysisRequest;
import com.pluta.camera.dtos.ImageAnalysisResponse;
import com.pluta.camera.entities.ImageAnalysisResult;
import com.pluta.camera.enums.AnalysisStatus;
import com.pluta.camera.services.ImageAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/image-analysis")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ImageAnalysisController {

    private final ImageAnalysisService analysisService;

    @PostMapping(path ="/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageAnalysisResponse> analyzeImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "confidenceThreshold", defaultValue = "0.2") Double confidenceThreshold) {

        if (image.isEmpty()) {
            ImageAnalysisResponse response = new ImageAnalysisResponse();
            response.setStatus("ERROR");
            response.setMessage("Please select an image file to upload");
            return ResponseEntity.badRequest().body(response);
        }

        ImageAnalysisRequest request = new ImageAnalysisRequest();
        request.setImage(image);
        request.setConfidenceThreshold(confidenceThreshold);

        ImageAnalysisResponse response = analysisService.analyzeImage(request);

        if ("SUCCESS".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/video/{videoId}/results")
    public ResponseEntity<List<AnalysisResultDTO>> getAllResults(@PathVariable Long videoId,@PageableDefault Pageable pageable) {
        List<AnalysisResultDTO> results = analysisService.getAllAnalysisResults(videoId,pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/results/{id}")
    public ResponseEntity<ImageAnalysisResult> getResult(@PathVariable Long id) {
        Optional<ImageAnalysisResult> result = analysisService.getAnalysisResult(id);
        return result.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/results/status/{status}")
    public ResponseEntity<List<AnalysisResultDTO>> getResultsByStatus(@PathVariable String status) {
        try {
            AnalysisStatus analysisStatus = AnalysisStatus.valueOf(status.toUpperCase());
            List<AnalysisResultDTO> results = analysisService.getAnalysisResultsByStatus(analysisStatus);
            return ResponseEntity.ok(results);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}