package com.pluta.camera.services;

import com.pluta.camera.clients.StreamAnalysisClient;
import com.pluta.camera.dtos.FrameAnalysisResultDTO;
import com.pluta.camera.dtos.StreamAnalysisRequest;
import com.pluta.camera.dtos.TableCoordinatesDTO;
import com.pluta.camera.dtos.TableDTO;
import com.pluta.camera.entities.Frame;
import com.pluta.camera.entities.StreamEntity;
import com.pluta.camera.entities.TableEntity;
import com.pluta.camera.entities.Video;
import com.pluta.camera.repositories.FrameRepository;
import com.pluta.camera.repositories.TableRepository;
import com.pluta.camera.services.interfaces.IFrameService;
import com.pluta.camera.services.mappers.FrameMapper;
import com.pluta.camera.services.mappers.TableMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Frame Service implementation for processing stream entities
 * This implementation is optimized for real-time stream processing
 */
@Service("streamFrameService")
@Transactional
@RequiredArgsConstructor
@Slf4j
public class StreamFrameService implements IFrameService {


}