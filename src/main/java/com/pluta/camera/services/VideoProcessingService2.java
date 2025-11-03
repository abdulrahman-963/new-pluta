package com.pluta.camera.services;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


@Service
public class VideoProcessingService2 {

    @Value("${video.frames.output.directory:./extracted_frames}")
    private String outputDirectory;

    @Value("${video.temp.directory:./temp}")
    private String tempDirectory;

    public String processVideo(MultipartFile file) throws Exception {
        // Create directories if they don't exist
        createDirectories();

        // Save uploaded file temporarily
        String tempFilePath = saveTemporaryFile(file);

        try {
            // Extract frames using FFmpeg
            List<String> extractedFrames = extractBestFrames(tempFilePath, file.getOriginalFilename());

            // Clean up temporary file
            Files.deleteIfExists(Paths.get(tempFilePath));

            return String.format("Successfully extracted %d frames from video '%s'",
                    extractedFrames.size(), file.getOriginalFilename());

        } catch (Exception e) {
            // Clean up temporary file in case of error
            Files.deleteIfExists(Paths.get(tempFilePath));
            throw e;
        }
    }

    private void createDirectories() throws IOException {
        Path outputPath = Paths.get(outputDirectory);
        Path tempPath = Paths.get(tempDirectory);

        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }

        if (!Files.exists(tempPath)) {
            Files.createDirectories(tempPath);
        }
    }

    private String saveTemporaryFile(MultipartFile file) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String tempFilePath = tempDirectory + File.separator + fileName;

        try (FileOutputStream fos = new FileOutputStream(tempFilePath)) {
            fos.write(file.getBytes());
        }

        return tempFilePath;
    }

    private List<String> extractBestFrames(String videoPath, String originalFileName) throws Exception {
        List<String> extractedFrames = new ArrayList<>();

        // Get video duration first
        double duration = getVideoDuration(videoPath);

        // Calculate frame extraction points (every 10 seconds)
        List<Double> timePoints = new ArrayList<>();
        for (double time = 0; time < duration; time += 1) {
            timePoints.add(time);
        }

        // Extract frames at each time point
        String baseFileName = originalFileName.replaceAll("\\.[^.]+$", "");

        for (int i = 0; i < timePoints.size(); i++) {
            double timePoint = timePoints.get(i);
            String outputFileName = String.format("%s_frame_%03d_%.0fs.jpg",
                    baseFileName, i + 1, timePoint);
            String outputPath = outputDirectory + File.separator + outputFileName;

            // FFmpeg command to extract best quality frame
            // Using scale filter to maintain aspect ratio and quality
            // Using select filter to get the frame with best quality metrics
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
                extractedFrames.add(outputPath);
            }
        }

        return extractedFrames;
    }

    private double getVideoDuration(String videoPath) throws Exception {
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

    private boolean executeFFmpegCommand(List<String> command) throws Exception {
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