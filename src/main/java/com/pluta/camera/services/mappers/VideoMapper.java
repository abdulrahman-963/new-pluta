package com.pluta.camera.services.mappers;

import com.pluta.camera.dtos.VideoResponseDto;
import com.pluta.camera.entities.Video;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.time.Duration;
import java.util.Objects;

@Mapper(componentModel = "spring")
public interface VideoMapper extends GenericMapper<Video, VideoResponseDto>{

    @AfterMapping
    public default void addDuration(Video video, @MappingTarget VideoResponseDto videoDto){
        if (Objects.nonNull( videoDto.getProcessingStartedAt())
                && Objects.nonNull(videoDto.getProcessingCompletedAt()) ){
        videoDto.setProcessingTime( Duration.between(videoDto.getProcessingStartedAt(),
                videoDto.getProcessingCompletedAt()).toSecondsPart());

        }
    }
}
