package com.pluta.camera.services.mappers;

import com.pluta.camera.dtos.BranchDTO;
import com.pluta.camera.entities.Branch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface BranchMapper extends GenericMapper<Branch, BranchDTO>{

    @Mapping(source = "tenant.id", target = "tenantId")
    @Mapping(source = "tenant.code", target = "tenantCode")
    BranchDTO toDTO(Branch entity);

}
