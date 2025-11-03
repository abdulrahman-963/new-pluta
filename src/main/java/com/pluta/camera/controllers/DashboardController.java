package com.pluta.camera.controllers;

import com.pluta.camera.dtos.dashboard.DashboardDTO;
import com.pluta.camera.dtos.dashboard.VideoSummaryDTO;
import com.pluta.camera.dtos.dashboard.AnalysisSummaryDTO;
import com.pluta.camera.dtos.dashboard.ProcessingStatusSummaryDTO;
import com.pluta.camera.dtos.dashboard.OccupancyTrendsDTO;
import com.pluta.camera.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardDTO> getDashboardData(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(dashboardService.getDashboardData(startDate, endDate));
    }

    @GetMapping("/videos/summary")
    public ResponseEntity<VideoSummaryDTO> getVideosSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(dashboardService.getVideosSummary(startDate, endDate));
    }

    @GetMapping("/analysis/summary")
    public ResponseEntity<AnalysisSummaryDTO> getAnalysisSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(dashboardService.getAnalysisSummary(startDate, endDate));
    }

    @GetMapping("/processing/status")
    public ResponseEntity<ProcessingStatusSummaryDTO> getProcessingStatus() {
        return ResponseEntity.ok(dashboardService.getProcessingStatus());
    }

    @GetMapping("/occupancy/trends")
    public ResponseEntity<List<OccupancyTrendsDTO>> getOccupancyTrends(
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(dashboardService.getOccupancyTrends(days));
    }

   /* @GetMapping("/videos/recent")
    public ResponseEntity<List<VideoSummaryDTO.RecentVideoDTO>> getRecentVideos(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(dashboardService.getRecentVideos(limit));
    }*/
}