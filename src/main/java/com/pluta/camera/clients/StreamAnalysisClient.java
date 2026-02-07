package com.pluta.camera.clients;

import com.pluta.camera.configs.FeignConfig;
import com.pluta.camera.dtos.FrameAnalysisResultDTO;
import com.pluta.camera.dtos.StreamAnalysisRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "stream-analysis-client",
        url = "${stream.analysis.api.url}",
        configuration = FeignConfig.class
)
public interface StreamAnalysisClient {

    /**
     * Calls the stream analysis API with the provided request
     * @param request The stream analysis request containing stream URL, camera ID, etc.
     * @return ResponseEntity with the API response
     */
    @PostMapping
    ResponseEntity<FrameAnalysisResultDTO> analyzeStream(@RequestBody StreamAnalysisRequest request);
}