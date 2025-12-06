package com.pluta.camera.repositories;

import com.pluta.camera.entities.Video;
import com.pluta.camera.enums.ProcessingStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findByUpdatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT v FROM Video v ORDER BY v.updatedAt DESC")
    List<Video> findAllOrderByUpdatedAtDesc();

    default List<Video> findTopByOrderByUploadedAtDesc(int limit) {
        return findAllOrderByUpdatedAtDesc().stream()
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());
    }

    Long countByStatus(ProcessingStatus status);


    @Query("SELECT COUNT(v) FROM Video v WHERE v.processingCompletedAt BETWEEN :startDate AND :endDate")
    Long countProcessedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT v FROM Video v WHERE v.processingStartedAt IS NOT NULL AND v.processingCompletedAt IS NOT NULL")
    List<Video> findAllWithProcessingTimes();

    @Query("SELECT SUM(v.fileSize) FROM Video v")
    Long getTotalStorageSize();

    @Query("SELECT SUM(v.duration) FROM Video v WHERE v.duration IS NOT NULL")
    Double getTotalDuration();

    List<Video> findByTenantIdAndBranchId(Pageable page, Long tenantId, Long branchId);

}
