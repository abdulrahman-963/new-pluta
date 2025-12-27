package com.pluta.camera.controllers;

import com.pluta.camera.services.QueryExecutionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/queries")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Query Management", description = "APIs for execution query")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
public class QueryExecutionController {

    private final QueryExecutionService queryExecutionService;

    @PostMapping("/{queryName}/execute")
    public ResponseEntity<List<Map<String , Object>>> execute(
            @PathVariable String queryName,
            @RequestBody Map<String, Object> params) {
        return ResponseEntity.ok( queryExecutionService.executeQuery(queryName, params));
    }
}
