package com.pluta.camera.services;

import com.pluta.camera.entities.ImageAnalysisResult;
import com.pluta.camera.enums.AnalysisStatus;
import com.pluta.camera.repositories.ImageAnalysisResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FileCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(FileCleanupService.class);

    @Value("${analysis.cleanup-old-files}")
    private boolean cleanupEnabled;

    @Value("${analysis.cleanup-days}")
    private int cleanupDays;

    @Value("${analysis.upload-dir}")
    private String uploadDir;

    @Autowired
    private ImageAnalysisResultRepository analysisRepository;

    @Autowired
    private FileStorageService fileStorageService;

  //  @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    public void cleanupOldFiles() {
        if (!cleanupEnabled) {
            logger.info("File cleanup is disabled");
            return;
        }

        logger.info("Starting cleanup of files older than {} days", cleanupDays);

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(cleanupDays);
        List<ImageAnalysisResult> oldResults = analysisRepository
                .findByAnalysisDateBeforeAndStatus(cutoffDate, AnalysisStatus.COMPLETED);

        int deletedCount = 0;
        for (ImageAnalysisResult result : oldResults) {
            try {
                // Delete original file
                fileStorageService.deleteFile(result.getOriginalPath());

                // Delete annotated file
                if (result.getAnnotatedImagePath() != null) {
                    fileStorageService.deleteFile(result.getAnnotatedImagePath());
                }

                deletedCount++;

            } catch (Exception e) {
                logger.error("Error deleting files for analysis {}: {}",
                        result.getId(), e.getMessage());
            }
        }

        logger.info("Cleanup completed. Deleted {} file sets", deletedCount);
    }
}
