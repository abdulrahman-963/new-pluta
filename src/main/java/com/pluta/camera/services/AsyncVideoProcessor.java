package com.pluta.camera.services;


import com.pluta.camera.entities.Frame;
import com.pluta.camera.entities.Video;
import com.pluta.camera.enums.ProcessingStatus;
import com.pluta.camera.repositories.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncVideoProcessor {

    private final VideoRepository videoRepository;

    @Value("${video.frames.output.directory:./extracted_frames}")
    private String outputDirectory;

    @Value("${video.frames.output.duration:10}")
    private int frameDuration;

    private final ImageAnalysisService imageAnalysisService;

    private final FrameService frameService;

    @Async
    public void processVideoAsync(Long videoId, String videoPath) {
        Video video = videoRepository.findById(videoId).orElse(null);
        if (video == null) {
            return;
        }

        log.info("Starting video processing");

        try {
            // Update status to processing
            video.setStatus(ProcessingStatus.PROCESSING);
            video.setProcessingStartedAt(LocalDateTime.now());
            videoRepository.save(video);

            // Extract frames and create ImageAnalysisResult entities

            List<Frame> frames = extractFrames(videoPath, video);

            for(Frame f : frames){
                frameService.frameAnalysis(new File(f.getAnnotatedImagePath()), 0.4, 0.7,
                        video,null,f.getFrameOffsetSeconds());
            }


            //Analyze Image
           // frameService.analyze(frames,0.4,0.7,video.getCamera().getId(),video.getZone().getId(),
            //        video.getBranch().getId(),video.getTenant().getId());

            // Get video duration
            double duration = getVideoDuration(videoPath);

            // Update video with results
            video.setStatus(ProcessingStatus.COMPLETED);
            video.setDuration(duration);
            video.setFramesExtracted(frames.size());
            video.setProcessingCompletedAt(LocalDateTime.now());
            videoRepository.save(video); // This will also save the ImageAnalysisResult entities due to cascade


        } catch (IOException | InterruptedException  e) {
            log.error("Error analyzing video: {}", e.getMessage(), e);
            video.setStatus(ProcessingStatus.FAILED);
            video.setErrorMessage(e.getMessage());
            video.setProcessingCompletedAt(LocalDateTime.now());
            videoRepository.save(video);
        }
        log.info("End video processing");
    }

    private List<Frame> extractFrames(String videoPath, Video video) throws IOException, InterruptedException {
        List<Frame> extractedFrames = new ArrayList<>();

        // Get video duration first
        double duration = getVideoDuration(videoPath);

        // Calculate frame extraction points (by default every 10 seconds)
        List<Double> timePoints = new ArrayList<>();
        for (double time = 0; time < duration; time += frameDuration) {
            timePoints.add(time);
        }

        // Extract frames at each time point
        String baseFileName = video.getOriginalFileName().replaceAll("\\.[^.]+$", "");

        for (int i = 0; i < timePoints.size(); i++) {
            double timePoint = timePoints.get(i);
            String outputFileName = String.format("%s_frame_%03d_%.0fs.jpg",
                    baseFileName, i + 1, timePoint);
            String outputPath = outputDirectory + File.separator + outputFileName;

            // FFmpeg command to extract best quality frame
            List<String> command = new ArrayList<>();
            command.add("ffmpeg");
            command.add("-i");
            command.add(videoPath);
            command.add("-ss");
            command.add(String.valueOf(timePoint));
            command.add("-vframes");
            command.add("1");
            command.add("-vf");
            command.add("scale=iw*min(1920/iw\\,1080/ih):ih*min(1920/iw\\,1080/ih),pad=1920:1080:(1920-iw*min(1920/iw\\,1080/ih))/2:(1080-ih*min(1920/iw\\,1080/ih))/2");
            command.add("-q:v");
            command.add("2"); // High quality
            command.add("-y"); // Overwrite output file
            command.add(outputPath);

            if (executeFFmpegCommand(command)) {
                // Create ImageAnalysisResult entity for each extracted frame
                Frame frame = new Frame();
                frame.setAnnotatedImagePath(outputPath);
                frame.setResolution("1920x1080"); // Set based on your scaling
                frame.setVideo(video);
                frame.setFrameOffsetSeconds(timePoint);
                extractedFrames.add(frame);
            }
        }

        return extractedFrames;
    }

    private double getVideoDuration(String videoPath) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("ffprobe");
        command.add("-v");
        command.add("quiet");
        command.add("-show_entries");
        command.add("format=duration");
        command.add("-of");
        command.add("default=noprint_wrappers=1:nokey=1");
        command.add(videoPath);

        ProcessBuilder pb = new ProcessBuilder(command);
        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
        }

        process.waitFor();

        if (process.exitValue() != 0) {
            throw new RuntimeException("Failed to get video duration");
        }

        return Double.parseDouble(output.toString().trim());
    }

    private boolean executeFFmpegCommand(List<String> command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        // Read output to prevent process from hanging
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Log or process FFmpeg output if needed
                System.out.println("FFmpeg: " + line);
            }
        }

        int exitCode = process.waitFor();
        return exitCode == 0;
    }
}
