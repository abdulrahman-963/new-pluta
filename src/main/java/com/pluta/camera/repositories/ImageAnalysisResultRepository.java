package com.pluta.camera.repositories;

import com.pluta.camera.entities.ImageAnalysisResult;
import com.pluta.camera.enums.AnalysisStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface ImageAnalysisResultRepository extends JpaRepository<ImageAnalysisResult, Long> {

    Page<ImageAnalysisResult> findByVideoId(Long videoId, Pageable pageable);

    List<ImageAnalysisResult> findByStatusOrderByAnalysisDateDesc(AnalysisStatus status);

    // Additional methods for cleanup and analytics
    List<ImageAnalysisResult> findByAnalysisDateBeforeAndStatus(LocalDateTime cutoffDate, AnalysisStatus status);

    List<ImageAnalysisResult> findByAnalysisDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<ImageAnalysisResult> findByAnalysisDateAfter(LocalDateTime date);

    @Query("SELECT iar FROM ImageAnalysisResult iar ORDER BY iar.analysisDate DESC")
    List<ImageAnalysisResult> findAllOrderByAnalysisDateDesc();

    default List<ImageAnalysisResult> findTopByOrderByAnalysisDateDesc(int limit) {
        return findAllOrderByAnalysisDateDesc().stream()
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());
    }

    Long countByStatus(AnalysisStatus status);

    @Query("SELECT iar FROM ImageAnalysisResult iar WHERE iar.video.id = :videoId ORDER BY iar.analysisDate DESC")
    List<ImageAnalysisResult> findByVideoIdOrderByAnalysisDateDesc(@Param("videoId") Long videoId);

    @Query("SELECT AVG(iar.confidenceThreshold) FROM ImageAnalysisResult iar WHERE iar.confidenceThreshold IS NOT NULL")
    Double getAverageConfidenceThreshold();

    @Query("SELECT SUM(iar.personsDetected) FROM ImageAnalysisResult iar WHERE iar.analysisDate BETWEEN :startDate AND :endDate")
    Long getTotalPersonsDetectedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT iar FROM ImageAnalysisResult iar WHERE iar.analysisDate = " +
            "(SELECT MAX(iar2.analysisDate) FROM ImageAnalysisResult iar2)")
    ImageAnalysisResult findLatestAnalysis();

    @Query("SELECT iar FROM ImageAnalysisResult iar WHERE iar.analysisDate BETWEEN :startDate AND :endDate")
    List<ImageAnalysisResult> findAllBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT iar FROM ImageAnalysisResult iar WHERE iar.analysisDate >= :startDate ORDER BY iar.analysisDate DESC")
    List<ImageAnalysisResult> findRecentAnalyses(@Param("startDate") LocalDateTime startDate);

}
