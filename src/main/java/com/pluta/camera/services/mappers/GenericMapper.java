package com.pluta.camera.services.mappers;


import com.pluta.camera.entities.BaseEntity;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

public interface GenericMapper<E, D> {

    D toDTO(E entity);

    E toEntity(D dto);

    List<D> toDTOList(List<E> entityList);

    List<E> toEntityList(List<D> dtoList);

    void updateEntityFromDTO(D dto, @MappingTarget E entity);
}
