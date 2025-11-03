package com.pluta.camera.repositories;


import com.pluta.camera.entities.Frame;
import com.pluta.camera.enums.AnalysisStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FrameRepository extends JpaRepository<Frame, Long>, JpaSpecificationExecutor<Frame> {

    // ========================================================================
    // Basic Queries by Foreign Keys
    // ========================================================================

    List<Frame> findByVideoId(Long videoId);

    List<Frame> findByStreamId(Long streamId);

    List<Frame> findByTenantId(Long tenantId);

    List<Frame> findByBranchId(Long branchId);

    // ========================================================================
    // Composite Queries - Tenant + Branch
    // ========================================================================

    List<Frame> findByTenantIdAndBranchId(Long tenantId, Long branchId);

    List<Frame> findByTenantIdAndBranchIdAndVideoId(Long tenantId, Long branchId, Long videoId);

    List<Frame> findByTenantIdAndBranchIdAndStreamId(Long tenantId, Long branchId, Long streamId);

    // ========================================================================
    // Status Queries
    // ========================================================================

    List<Frame> findByStatus(AnalysisStatus status);

    List<Frame> findByTenantIdAndStatus(Long tenantId, AnalysisStatus status);

    List<Frame> findByBranchIdAndStatus(Long branchId, AnalysisStatus status);

    List<Frame> findByVideoIdAndStatus(Long videoId, AnalysisStatus status);

    List<Frame> findByStreamIdAndStatus(Long streamId, AnalysisStatus status);

    // ========================================================================
    // Time-based Queries
    // ========================================================================

    List<Frame> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Frame> findByTenantIdAndCreatedAtBetween(Long tenantId, LocalDateTime start, LocalDateTime end);

    List<Frame> findByBranchIdAndCreatedAtBetween(Long branchId, LocalDateTime start, LocalDateTime end);

    List<Frame> findByVideoIdAndCreatedAtBetween(Long videoId, LocalDateTime start, LocalDateTime end);

    List<Frame> findByStreamIdAndCreatedAtBetween(Long streamId, LocalDateTime start, LocalDateTime end);

    // ========================================================================
    // Detection-based Queries
    // ========================================================================

    List<Frame> findByPersonsDetectedGreaterThan(Integer minPersons);

    List<Frame> findByTenantIdAndPersonsDetectedGreaterThan(Long tenantId, Integer minPersons);

    List<Frame> findByTotalDetectedGreaterThan(Integer minTotal);

    // ========================================================================
    // Latest/First Queries
    // ========================================================================

    Optional<Frame> findFirstByVideoIdOrderByCreatedAtDesc(Long videoId);

    Optional<Frame> findFirstByVideoIdOrderByCreatedAtAsc(Long videoId);

    Optional<Frame> findFirstByStreamIdOrderByCreatedAtDesc(Long streamId);

    List<Frame> findTop10ByTenantIdOrderByCreatedAtDesc(Long tenantId);

    List<Frame> findTop10ByBranchIdOrderByCreatedAtDesc(Long branchId);

    // ========================================================================
    // Count Queries
    // ========================================================================

    long countByVideoId(Long videoId);

    long countByStreamId(Long streamId);

    long countByTenantId(Long tenantId);

    long countByBranchId(Long branchId);

    long countByStatus(AnalysisStatus status);

    long countByTenantIdAndStatus(Long tenantId, AnalysisStatus status);

    long countByBranchIdAndStatus(Long branchId, AnalysisStatus status);

    long countByVideoIdAndStatus(Long videoId, AnalysisStatus status);

    // ========================================================================
    // Existence Checks
    // ========================================================================

    boolean existsByVideoId(Long videoId);

    boolean existsByStreamId(Long streamId);

    boolean existsByTenantId(Long tenantId);

    boolean existsByBranchId(Long branchId);

    // ========================================================================
    // Delete Operations
    // ========================================================================

    void deleteByVideoId(Long videoId);

    void deleteByStreamId(Long streamId);

    void deleteByTenantId(Long tenantId);

    void deleteByBranchId(Long branchId);

    // ========================================================================
    // Custom Analytics Queries
    // ========================================================================
/*
    @Query("SELECT AVG(f.personsDetected) FROM Frame f JOIN f.tenant t WHERE t.id = :tenantId AND f.status = 'COMPLETED'")
    Double getAveragePersonsDetectedByTenant(@Param("tenantId") Long tenantId);

    @Query("SELECT AVG(f.personsDetected) FROM Frame f WHERE f.branchId = :branchId AND f.status = 'COMPLETED'")
    Double getAveragePersonsDetectedByBranch(@Param("branchId") Long branchId);

    @Query("SELECT AVG(f.personsDetected) FROM Frame f WHERE f.videoId = :videoId AND f.status = 'COMPLETED'")
    Double getAveragePersonsDetectedByVideo(@Param("videoId") Long videoId);

    @Query("SELECT MAX(f.personsDetected) FROM Frame f WHERE f.tenantId = :tenantId AND f.status = 'COMPLETED'")
    Integer getMaxPersonsDetectedByTenant(@Param("tenantId") Long tenantId);

    @Query("SELECT MAX(f.personsDetected) FROM Frame f WHERE f.branchId = :branchId AND f.status = 'COMPLETED'")
    Integer getMaxPersonsDetectedByBranch(@Param("branchId") Long branchId);

    // ========================================================================
    // Occupancy Analytics
    // ========================================================================

    @Query("SELECT AVG(CAST(f.occupiedChairs AS double) / NULLIF(f.chairsDetected, 0)) " +
            "FROM Frame f WHERE f.tenantId = :tenantId AND f.status = 'COMPLETED' AND f.chairsDetected > 0")
    Double getAverageChairOccupancyRateByTenant(@Param("tenantId") Long tenantId);

    @Query("SELECT AVG(CAST(f.occupiedChairs AS double) / NULLIF(f.chairsDetected, 0)) " +
            "FROM Frame f WHERE f.branchId = :branchId AND f.status = 'COMPLETED' AND f.chairsDetected > 0")
    Double getAverageChairOccupancyRateByBranch(@Param("branchId") Long branchId);

    @Query("SELECT AVG(CAST(f.occupiedChairs AS double) / NULLIF(f.chairsDetected, 0)) " +
            "FROM Frame f WHERE f.videoId = :videoId AND f.status = 'COMPLETED' AND f.chairsDetected > 0")
    Double getAverageChairOccupancyRateByVideo(@Param("videoId") Long videoId);

    // ========================================================================
    // Total Detection Sums
    // ========================================================================

    @Query("SELECT SUM(f.tablesDetected) FROM Frame f WHERE f.tenantId = :tenantId AND f.status = 'COMPLETED'")
    Long getTotalTablesDetectedByTenant(@Param("tenantId") Long tenantId);

    @Query("SELECT SUM(f.chairsDetected) FROM Frame f WHERE f.tenantId = :tenantId AND f.status = 'COMPLETED'")
    Long getTotalChairsDetectedByTenant(@Param("tenantId") Long tenantId);

    @Query("SELECT SUM(f.personsDetected) FROM Frame f WHERE f.tenantId = :tenantId AND f.status = 'COMPLETED'")
    Long getTotalPersonsDetectedByTenant(@Param("tenantId") Long tenantId);

    @Query("SELECT SUM(f.occupiedChairs) FROM Frame f WHERE f.branchId = :branchId AND f.status = 'COMPLETED'")
    Long getTotalOccupiedChairsByBranch(@Param("branchId") Long branchId);

    @Query("SELECT SUM(f.unoccupiedChairs) FROM Frame f WHERE f.branchId = :branchId AND f.status = 'COMPLETED'")
    Long getTotalUnoccupiedChairsByBranch(@Param("branchId") Long branchId);

    // ========================================================================
    // Time-series Analytics
    // ========================================================================

    @Query("SELECT f FROM Frame f WHERE f.tenantId = :tenantId " +
            "AND f.createdAt BETWEEN :start AND :end " +
            "AND f.status = 'COMPLETED' " +
            "ORDER BY f.createdAt ASC")
    List<Frame> getFrameTimeSeriesByTenant(
            @Param("tenantId") Long tenantId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT f FROM Frame f WHERE f.branchId = :branchId " +
            "AND f.createdAt BETWEEN :start AND :end " +
            "AND f.status = 'COMPLETED' " +
            "ORDER BY f.createdAt ASC")
    List<Frame> getFrameTimeSeriesByBranch(
            @Param("branchId") Long branchId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT f FROM Frame f WHERE f.videoId = :videoId " +
            "AND f.status = 'COMPLETED' " +
            "ORDER BY f.createdAt ASC")
    List<Frame> getFrameTimeSeriesByVideo(@Param("videoId") Long videoId);

    // ========================================================================
    // Peak Detection Queries
    // ========================================================================

    @Query("SELECT f FROM Frame f WHERE f.tenantId = :tenantId " +
            "AND f.status = 'COMPLETED' " +
            "ORDER BY f.personsDetected DESC")
    List<Frame> findPeakOccupancyFramesByTenant(@Param("tenantId") Long tenantId);

    @Query("SELECT f FROM Frame f WHERE f.branchId = :branchId " +
            "AND f.status = 'COMPLETED' " +
            "ORDER BY f.personsDetected DESC")
    List<Frame> findPeakOccupancyFramesByBranch(@Param("branchId") Long branchId);

    // ========================================================================
    // Confidence Threshold Queries
    // ========================================================================

    List<Frame> findByConfidenceThresholdGreaterThanEqual(Double threshold);

    List<Frame> findByTenantIdAndConfidenceThresholdGreaterThanEqual(Long tenantId, Double threshold);

    @Query("SELECT AVG(f.confidenceThreshold) FROM Frame f WHERE f.tenantId = :tenantId AND f.status = 'COMPLETED'")
    Double getAverageConfidenceByTenant(@Param("tenantId") Long tenantId);

    // ========================================================================
    // Failed Frame Queries
    // ========================================================================

    @Query("SELECT f FROM Frame f WHERE f.tenantId = :tenantId " +
            "AND f.status = 'FAILED' " +
            "ORDER BY f.createdAt DESC")
    List<Frame> getFailedFramesByTenant(@Param("tenantId") Long tenantId);

    @Query("SELECT f FROM Frame f WHERE f.branchId = :branchId " +
            "AND f.status = 'FAILED' " +
            "ORDER BY f.createdAt DESC")
    List<Frame> getFailedFramesByBranch(@Param("branchId") Long branchId);

    @Query("SELECT COUNT(f) FROM Frame f WHERE f.tenantId = :tenantId " +
            "AND f.status = 'FAILED' " +
            "AND f.createdAt >= :since")
    long countFailedFramesSince(@Param("tenantId") Long tenantId, @Param("since") LocalDateTime since);*/
}