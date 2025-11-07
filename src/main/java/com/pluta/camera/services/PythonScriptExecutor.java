package com.pluta.camera.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pluta.camera.dtos.FrameAnalysisResultDTO;
import com.pluta.camera.dtos.PythonAnalysisResult;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PythonScriptExecutor {

    private static final Logger logger = LoggerFactory.getLogger(PythonScriptExecutor.class);

    @Value("${analysis.python.script-path}")
    private String scriptPath;

    // ADD THIS - Use the configured Python executable
    @Value("${analysis.python.python-executable}")
    private String pythonExecutable;

    public FrameAnalysisResultDTO executeAnalysis(String imagePath, Double confidenceThreshold,
                                                  Double zoneConfidenceThreshold,Long cameraId ,
                                                  Long tableId,List<String> coordinates) throws IOException,
            InterruptedException {
        // CHANGE THIS - Use pythonExecutable instead of hardcoded "python3"
        List<String> command = Arrays.asList(
                pythonExecutable,  // ✅ Use configured path instead of "python3"
                scriptPath,
                imagePath,
                "-o","/Users/abdulrahman/Desktop/POC/CameraAi/video-procesing/labeled",
                "--confidence", confidenceThreshold.toString(),
                "--zone-threshold",zoneConfidenceThreshold.toString(),
                "--camera-id",cameraId.toString(),
                "--table-id",tableId.toString(),
                "--zone"
        );

        List<String> finalCommand = Stream.concat(command.stream(), coordinates.stream())
                .collect(Collectors.toList());


        logger.info("Executing command: {}", String.join(" ", finalCommand));

        ProcessBuilder pb = new ProcessBuilder(finalCommand);
        pb.redirectErrorStream(true);

        // OPTIONAL: Set environment variables if needed
        Map<String, String> env = pb.environment();
        // You can add environment variables here if needed

        Process process = pb.start();

        // Read output
        StringBuilder output = new StringBuilder();
        StringBuilder errorOutput = new StringBuilder();

        // Read both stdout and stderr
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();

        logger.info("Python script exit code: {}", exitCode);
        logger.info("Python script output: {}", output.toString());

        if (exitCode != 0) {
            logger.error("Python script error output: {}", errorOutput.toString());
            throw new RuntimeException("Python script failed with exit code: " + exitCode +
                    "\nOutput: " + output.toString() +
                    "\nError: " + errorOutput.toString());
        }

        // Parse JSON output
        return parseAnalysisResult(output.toString());
    }

    // ADD THIS - Method to test Python setup
   /* @PostConstruct
    public void validatePythonSetup() {
        try {
            logger.info("Validating Python setup...");
            logger.info("Python executable: {}", pythonExecutable);
            logger.info("Script path: {}", scriptPath);

            // Test Python executable
            List<String> testCommand = Arrays.asList(pythonExecutable, "--version");
            ProcessBuilder pb = new ProcessBuilder(testCommand);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                logger.info("Python validation successful: {}", output.toString().trim());
            } else {
                logger.error("Python validation failed: {}", output.toString());
            }

            // Test required modules
            testCommand = Arrays.asList(pythonExecutable, "-c", "import cv2, ultralytics, numpy; print('All modules available')");
            pb = new ProcessBuilder(testCommand);
            pb.redirectErrorStream(true);
            process = pb.start();

            output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            exitCode = process.waitFor();
            if (exitCode == 0) {
                logger.info("Python modules validation successful: {}", output.toString().trim());
            } else {
                logger.error("Python modules validation failed: {}", output.toString());
            }

        } catch (Exception e) {
            logger.error("Python setup validation failed: {}", e.getMessage());
        }
    }*/

    private FrameAnalysisResultDTO parseAnalysisResult(String output) throws JsonProcessingException {
        // Log the raw output for debugging
        logger.debug("Raw Python output: {}", output);

        // Try to extract JSON from the output
        String jsonString = extractJsonFromOutput(output);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper.readValue(jsonString, FrameAnalysisResultDTO.class);
    }


    private String extractJsonFromOutput(String output) {
        // Find the first '{' and last '}' to extract JSON
        int firstBrace = output.indexOf('{');
        int lastBrace = output.lastIndexOf('}');

        if (firstBrace == -1 || lastBrace == -1 || firstBrace >= lastBrace) {
            throw new RuntimeException("No valid JSON found in Python output: " + output);
        }

        String jsonString = output.substring(firstBrace, lastBrace + 1);
        logger.debug("Extracted JSON: {}", jsonString);

        return jsonString;
    }
}