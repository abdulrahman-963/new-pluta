package com.pluta.camera.controllers;

import com.pluta.camera.dtos.FrameAnalysisResultDTO;
import com.pluta.camera.dtos.ImageAnalysisRequest;
import com.pluta.camera.dtos.ImageAnalysisResponse;
import com.pluta.camera.dtos.PythonAnalysisResult;
import com.pluta.camera.services.FrameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/frames")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class FrameController {

    private final FrameService frameService;


    @PostMapping(path ="/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<FrameAnalysisResultDTO>> analyzeImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "confidenceThreshold", defaultValue = "0.2") Double confidenceThreshold,
            @RequestParam(value = "zoneConfidenceThreshold", defaultValue = "0.7")  Double zoneConfidenceThreshold,
            @RequestParam(value = "cameraId") Long cameraId ,
            @RequestParam(value = "zoneId") Long zoneId ,
            @RequestParam(value = "branchId") Long branchId ,
            @RequestParam(value = "tenantId") Long tenantId ) throws Exception {


            return ResponseEntity.ok(frameService.analyze(image.getResource().getFile(), confidenceThreshold,zoneConfidenceThreshold,cameraId,zoneId,branchId,tenantId));

    }
}
