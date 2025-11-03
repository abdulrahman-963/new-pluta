package com.pluta.camera.services;


import com.pluta.camera.dtos.dashboard.*;
import com.pluta.camera.entities.ImageAnalysisResult;
import com.pluta.camera.entities.Video;
import com.pluta.camera.enums.AnalysisStatus;
import com.pluta.camera.enums.ProcessingStatus;
import com.pluta.camera.repositories.ImageAnalysisResultRepository;
import com.pluta.camera.repositories.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final VideoRepository videoRepository;
    private final ImageAnalysisResultRepository imageAnalysisResultRepository;

    public DashboardDTO getDashboardData(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        return DashboardDTO.builder()
                .videoSummary(getVideosSummary(startDate, endDate))
                .analysisSummary(getAnalysisSummary(startDate, endDate))
                .processingStatus(getProcessingStatus())
                .occupancyStatistics(getOccupancyStatistics())
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    public VideoSummaryDTO getVideosSummary(LocalDateTime startDate, LocalDateTime endDate) {
        List<Video> videos = videoRepository.findByUpdatedAtBetween(
                startDate != null ? startDate : LocalDateTime.now().minusDays(30),
                endDate != null ? endDate : LocalDateTime.now()
        );

        LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.now().with(LocalTime.MAX);

        long videosProcessedToday = videos.stream()
                .filter(v -> v.getProcessingCompletedAt() != null)
                .filter(v -> v.getProcessingCompletedAt().isAfter(todayStart) &&
                        v.getProcessingCompletedAt().isBefore(todayEnd))
                .count();

        double avgProcessingTime = videos.stream()
                .filter(v -> v.getProcessingStartedAt() != null && v.getProcessingCompletedAt() != null)
                .mapToDouble(v -> ChronoUnit.SECONDS.between(v.getProcessingStartedAt(), v.getProcessingCompletedAt()))
                .average()
                .orElse(0);

       // List<VideoSummaryDTO.RecentVideoDTO> recentVideos = getRecentVideos(10);

        return VideoSummaryDTO.builder()
                .totalVideos((long) videos.size())
                .totalStorageSize(videos.stream().mapToLong(Video::getFileSize).sum())
                .totalDuration(videos.stream()
                        .filter(v -> v.getDuration() != null)
                        .mapToDouble(Video::getDuration)
                        .sum())
                .videosProcessedToday(videosProcessedToday)
                .averageProcessingTime(avgProcessingTime)
              //  .recentVideos(recentVideos)
                .build();
    }

    public AnalysisSummaryDTO getAnalysisSummary(LocalDateTime startDate, LocalDateTime endDate) {
        List<ImageAnalysisResult> results = imageAnalysisResultRepository.findByAnalysisDateBetween(
                startDate != null ? startDate : LocalDateTime.now().minusDays(30),
                endDate != null ? endDate : LocalDateTime.now()
        );

        long successfulAnalyses = results.stream()
                .filter(r -> r.getStatus() == AnalysisStatus.COMPLETED)
                .count();

        long failedAnalyses = results.stream()
                .filter(r -> r.getStatus() == AnalysisStatus.FAILED)
                .count();

        long pendingAnalyses = results.stream()
                .filter(r -> r.getStatus() == AnalysisStatus.PENDING || r.getStatus() == AnalysisStatus.PROCESSING)
                .count();

        double avgConfidence = results.stream()
                .filter(r -> r.getConfidenceThreshold() != null)
                .mapToDouble(ImageAnalysisResult::getConfidenceThreshold)
                .average()
                .orElse(0.0);

        AnalysisSummaryDTO.ObjectDetectionSummaryDTO objectSummary = calculateObjectDetectionSummary(results);

        return AnalysisSummaryDTO.builder()
                .totalAnalyses((long) results.size())
                .successfulAnalyses(successfulAnalyses)
                .failedAnalyses(failedAnalyses)
                .pendingAnalyses(pendingAnalyses)
                .averageConfidenceThreshold(avgConfidence)
                .objectDetectionSummary(objectSummary)
                .build();
    }

    public ProcessingStatusSummaryDTO getProcessingStatus() {
        Map<String, Long> videoStatusCounts = videoRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        v -> v.getStatus().toString(),
                        Collectors.counting()
                ));

        Map<String, Long> analysisStatusCounts = imageAnalysisResultRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        r -> r.getStatus().toString(),
                        Collectors.counting()
                ));

        long activeProcessing = videoRepository.countByStatus(ProcessingStatus.PROCESSING);
        long queuedForProcessing = videoRepository.countByStatus(ProcessingStatus.UPLOADED);

        long totalProcessed = videoRepository.countByStatus(ProcessingStatus.COMPLETED);
        long totalFailed = videoRepository.countByStatus(ProcessingStatus.FAILED);
        double successRate = totalProcessed + totalFailed > 0
                ? (double) totalProcessed / (totalProcessed + totalFailed) * 100
                : 0;

        return ProcessingStatusSummaryDTO.builder()
                .videoStatusCounts(videoStatusCounts)
                .analysisStatusCounts(analysisStatusCounts)
                .activeProcessing(activeProcessing)
                .queuedForProcessing(queuedForProcessing)
                .processingSuccessRate(successRate)
                .build();
    }

    public List<OccupancyTrendsDTO> getOccupancyTrends(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<ImageAnalysisResult> results = imageAnalysisResultRepository.findByAnalysisDateAfter(startDate);

        Map<LocalDate, List<ImageAnalysisResult>> resultsByDate = results.stream()
                .collect(Collectors.groupingBy(r -> r.getAnalysisDate().toLocalDate()));

        return resultsByDate.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<ImageAnalysisResult> dayResults = entry.getValue();

                    double avgOccupancy = calculateAverageOccupancy(dayResults);
                    double peakOccupancy = calculatePeakOccupancy(dayResults);
                    int totalVisitors = dayResults.stream()
                            .mapToInt(ImageAnalysisResult::getPersonsDetected)
                            .sum();
                    String busiestHour = findBusiestHour(dayResults);

                    return OccupancyTrendsDTO.builder()
                            .date(date)
                            .averageOccupancy(avgOccupancy)
                            .peakOccupancy(peakOccupancy)
                            .totalVisitors(totalVisitors)
                            .busiestHour(busiestHour)
                            .build();
                })
                .sorted(Comparator.comparing(OccupancyTrendsDTO::getDate))
                .collect(Collectors.toList());
    }

 /*   public List<VideoSummaryDTO.RecentVideoDTO> getRecentVideos(int limit) {
        return videoRepository.findTopByOrderByUploadedAtDesc(limit).stream()
                .map(this::mapToRecentVideoDTO)
                .collect(Collectors.toList());
    }*/

    private OccupancyStatisticsDTO getOccupancyStatistics() {
        List<ImageAnalysisResult> recentResults = imageAnalysisResultRepository
                .findTopByOrderByAnalysisDateDesc(100);

        if (recentResults.isEmpty()) {
            return OccupancyStatisticsDTO.builder()
                    .currentOccupancyRate(0.0)
                    .averageOccupancyRate(0.0)
                    .peakOccupancyRate(0.0)
                    .peakOccupancyTime("N/A")
                    .occupancyDetails(buildEmptyOccupancyDetails())
                    .build();
        }

        ImageAnalysisResult latestResult = recentResults.get(0);
        double currentOccupancy = calculateOccupancyRate(latestResult);
        double avgOccupancy = calculateAverageOccupancy(recentResults);

        Map.Entry<LocalDateTime, Double> peakOccupancy = findPeakOccupancyWithTime(recentResults);

        return OccupancyStatisticsDTO.builder()
                .currentOccupancyRate(currentOccupancy)
                .averageOccupancyRate(avgOccupancy)
                .peakOccupancyRate(peakOccupancy.getValue())
                .peakOccupancyTime(peakOccupancy.getKey().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .occupancyDetails(buildOccupancyDetails(latestResult))
                .build();
    }

    private AnalysisSummaryDTO.ObjectDetectionSummaryDTO calculateObjectDetectionSummary(List<ImageAnalysisResult> results) {
        long totalTables = results.stream().mapToLong(ImageAnalysisResult::getTablesDetected).sum();
        long totalChairs = results.stream().mapToLong(ImageAnalysisResult::getChairsDetected).sum();
        long totalBenches = results.stream().mapToLong(ImageAnalysisResult::getBenchesDetected).sum();
        long totalCouches = results.stream().mapToLong(ImageAnalysisResult::getCouchesDetected).sum();
        long totalPersons = results.stream().mapToLong(ImageAnalysisResult::getPersonsDetected).sum();
        double avgOccupancy = calculateAverageOccupancy(results);

        return AnalysisSummaryDTO.ObjectDetectionSummaryDTO.builder()
                .totalTablesDetected(totalTables)
                .totalChairsDetected(totalChairs)
                .totalBenchesDetected(totalBenches)
                .totalCouchesDetected(totalCouches)
                .totalPersonsDetected(totalPersons)
                .averageOccupancyRate(avgOccupancy)
                .build();
    }

    private double calculateOccupancyRate(ImageAnalysisResult result) {
        int totalSeats = result.getChairsDetected() + result.getBenchesDetected() * 3 + result.getCouchesDetected() * 3;
        int occupiedSeats = result.getOccupiedChairs() + result.getOccupiedBenches() * 3 + result.getOccupiedCouches() * 3;

        return totalSeats > 0 ? (double) occupiedSeats / totalSeats * 100 : 0;
    }

    private double calculateAverageOccupancy(List<ImageAnalysisResult> results) {
        return results.stream()
                .mapToDouble(this::calculateOccupancyRate)
                .average()
                .orElse(0.0);
    }

    private double calculatePeakOccupancy(List<ImageAnalysisResult> results) {
        return results.stream()
                .mapToDouble(this::calculateOccupancyRate)
                .max()
                .orElse(0.0);
    }

    private Map.Entry<LocalDateTime, Double> findPeakOccupancyWithTime(List<ImageAnalysisResult> results) {
        return results.stream()
                .collect(Collectors.toMap(
                        ImageAnalysisResult::getAnalysisDate,
                        this::calculateOccupancyRate
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(new AbstractMap.SimpleEntry<>(LocalDateTime.now(), 0.0));
    }

    private String findBusiestHour(List<ImageAnalysisResult> dayResults) {
        Map<Integer, Long> hourCounts = dayResults.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getAnalysisDate().getHour(),
                        Collectors.counting()
                ));

        return hourCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> String.format("%02d:00", entry.getKey()))
                .orElse("N/A");
    }

    private OccupancyStatisticsDTO.OccupancyDetailsDTO buildOccupancyDetails(ImageAnalysisResult result) {
        int totalSeats = result.getChairsDetected() +
                result.getBenchesDetected() * 3 +
                result.getCouchesDetected() * 3;

        int occupiedSeats = result.getOccupiedChairs() +
                result.getOccupiedBenches() * 3 +
                result.getOccupiedCouches() * 3;

        return OccupancyStatisticsDTO.OccupancyDetailsDTO.builder()
                .totalSeats(totalSeats)
                .occupiedSeats(occupiedSeats)
                .availableSeats(totalSeats - occupiedSeats)
                .seatingBreakdown(OccupancyStatisticsDTO.SeatingBreakdownDTO.builder()
                        .chairsOccupied(result.getOccupiedChairs())
                        .chairsAvailable(result.getUnoccupiedChairs())
                        .benchesOccupied(result.getOccupiedBenches())
                        .benchesAvailable(result.getUnoccupiedBenches())
                        .couchesOccupied(result.getOccupiedCouches())
                        .couchesAvailable(result.getUnoccupiedCouches())
                        .build())
                .build();
    }

    private OccupancyStatisticsDTO.OccupancyDetailsDTO buildEmptyOccupancyDetails() {
        return OccupancyStatisticsDTO.OccupancyDetailsDTO.builder()
                .totalSeats(0)
                .occupiedSeats(0)
                .availableSeats(0)
                .seatingBreakdown(OccupancyStatisticsDTO.SeatingBreakdownDTO.builder()
                        .chairsOccupied(0)
                        .chairsAvailable(0)
                        .benchesOccupied(0)
                        .benchesAvailable(0)
                        .couchesOccupied(0)
                        .couchesAvailable(0)
                        .build())
                .build();
    }

   /* private VideoSummaryDTO.RecentVideoDTO mapToRecentVideoDTO(Video video) {
        int totalDetectedObjects = video.getImageAnalysisResults() != null
                ? video.getImageAnalysisResults().stream()
                .mapToInt(ImageAnalysisResult::getTotalDetected)
                .sum()
                : 0;

        return VideoSummaryDTO.RecentVideoDTO.builder()
                .id(video.getId())
                .originalFileName(video.getOriginalFileName())
                .fileSize(video.getFileSize())
                .status(video.getStatus().toString())
                .uploadedAt(video.getUploadedAt())
                .duration(video.getDuration())
                .framesExtracted(video.getFramesExtracted())
                .totalDetectedObjects(totalDetectedObjects)
                .build();
    }*/
}