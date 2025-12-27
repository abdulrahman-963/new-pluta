package com.pluta.camera.services;

import com.pluta.camera.context.TenantContext;
import com.pluta.camera.dtos.WidgetGroupDTO;
import com.pluta.camera.dtos.WidgetDTO;
import com.pluta.camera.entities.Tenant;
import com.pluta.camera.entities.WidgetGroup;
import com.pluta.camera.entities.Widget;
import com.pluta.camera.exceptions.ResourceNotFoundException;
import com.pluta.camera.repositories.WidgetGroupRepository;
import com.pluta.camera.repositories.WidgetRepository;
import com.pluta.camera.services.mappers.WidgetGroupMapper;
import com.pluta.camera.services.mappers.WidgetMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class WidgetService  {

    private final WidgetGroupRepository widgetGroupRepository;
    private final WidgetRepository widgetRepository;
    private final WidgetMapper widgetMapper;
    private final WidgetGroupMapper widgetGroupMapper;


    // ========== WidgetGroup CRUD Operations ==========
    // Note: findById() and findAllByTenantIdAndBranchId() are inherited from ReadOnlyService

    public WidgetGroupDTO findById(Long id) {
        log.debug("Finding widget group by id: {}", id);
        WidgetGroup entity = widgetGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(this.getClass().getName()+" not found with id: " + id));
        return widgetGroupMapper.toDTO(entity);
    }

    public Page<WidgetGroupDTO> findAll(Pageable pageable) {
        log.debug("Finding all {}} with pagination: {}",this.getClass().getName(), pageable);
        Page<WidgetGroup> entities = widgetGroupRepository.findAll( pageable);
        return entities.map(widgetGroupMapper::toDTO);
    }


    @Transactional(readOnly = true)
    public WidgetGroupDTO findWidgetGroupWithWidgets(Long id) {
        log.debug("Finding widget group with widgets by id: {}", id);
        WidgetGroup widgetGroup = widgetGroupRepository.findByIdWithWidgets(id)
                .orElseThrow(() -> new ResourceNotFoundException("widgetGroup not found with id: " + id));
        return widgetGroupMapper.toDTO(widgetGroup);
    }

    public WidgetGroupDTO createWidgetGroup(WidgetGroupDTO createDTO) {
        log.debug("Creating new widget group: {}", createDTO.getName());


        WidgetGroup widgetGroup = widgetGroupMapper.toEntity(createDTO);
        WidgetGroup savedWidgetGroup = widgetGroupRepository.save(widgetGroup);

        log.info("Created widget group with id: {}", savedWidgetGroup.getId());
        return widgetGroupMapper.toDTO(savedWidgetGroup);
    }

    public WidgetGroupDTO updateWidgetGroup(Long id, WidgetGroupDTO updateDTO) {
        log.debug("Updating widget group with id: {}", id);

        WidgetGroup widgetGroup = widgetGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WidgetGroup not found with id: " + id));

        widgetGroupMapper.updateEntityFromDTO(updateDTO, widgetGroup);
        WidgetGroup updatedWidgetGroup = widgetGroupRepository.save(widgetGroup);

        log.info("Updated widget group with id: {}", id);
        return widgetGroupMapper.toDTO(updatedWidgetGroup);
    }

    public void deleteWidgetGroup(Long id) {
        log.debug("Deleting widget group with id: {}", id);

        if (!widgetGroupRepository.existsById(id)) {
            throw new ResourceNotFoundException("widgetGroup not found with id: " + id);
        }

        widgetGroupRepository.deleteById(id);
        log.info("Deleted widget group with id: {}", id);
    }

    // ========== Widget CRUD Operations ==========

    @Transactional(readOnly = true)
    public List<WidgetDTO> findWidgetsByWidgetGroupId(Long widgetGroupId) {
        log.debug("Finding widgets for widget group: {}", widgetGroupId);

        WidgetGroup widgetGroup = widgetGroupRepository.findById(widgetGroupId)
                .orElseThrow(() -> new ResourceNotFoundException("widgetGroup not found with id: " + widgetGroupId));

        return widgetMapper.toDTOList(widgetGroup.getWidgets());
    }

    @Transactional(readOnly = true)
    public WidgetDTO findWidgetById(Long widgetGroupId, Long widgetId) {
        log.debug("Finding widget by id: {} in widget group: {}", widgetId, widgetGroupId);

        Widget widget = widgetRepository.findByIdAndWidgetGroupId(widgetId, widgetGroupId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Widget not found with id: %d in widget group: %d", widgetId, widgetGroupId)));

        return widgetMapper.toDTO(widget);
    }

    public WidgetDTO addWidget(Long widgetGroupId, WidgetDTO createDTO) {
        log.debug("Adding widget to widget group: {}", widgetGroupId);

        WidgetGroup widgetGroup = widgetGroupRepository.findById(widgetGroupId)
                .orElseThrow(() -> new ResourceNotFoundException("widgetGroup not found with id: " + widgetGroupId));


        Widget widget = widgetMapper.toEntity(createDTO);
        widget.setWidgetGroup(widgetGroup);

        widgetGroup.addWidget(widget);
        Widget savedWidget = widgetRepository.save(widget);

        log.info("Added widget with id: {} to widget group: {}", savedWidget.getId(), widgetGroupId);
        return widgetMapper.toDTO(savedWidget);
    }

    public WidgetDTO updateWidget(Long widgetGroupId, Long widgetId, WidgetDTO updateDTO) {
        log.debug("Updating widget with id: {} in widget group: {}", widgetId, widgetGroupId);

        Widget widget = widgetRepository.findByIdAndWidgetGroupId(widgetId, widgetGroupId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Widget not found with id: %d in widget group: %d", widgetId, widgetGroupId)));

        widgetMapper.updateEntityFromDTO(updateDTO, widget);
        Widget updatedWidget = widgetRepository.save(widget);

        log.info("Updated widget with id: {} in widget group: {}", widgetId, widgetGroupId);
        return widgetMapper.toDTO(updatedWidget);
    }

    public void deleteWidget(Long widgetGroupId, Long widgetId) {
        log.debug("Deleting widget with id: {} from widget group: {}", widgetId, widgetGroupId);

        Widget widget = widgetRepository.findByIdAndWidgetGroupId(widgetId, widgetGroupId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Widget not found with id: %d in widget group: %d", widgetId, widgetGroupId)));

        WidgetGroup widgetGroup = widget.getWidgetGroup();
        widgetGroup.removeWidget(widget);

        widgetRepository.delete(widget);
        log.info("Deleted widget with id: {} from widget group: {}", widgetId, widgetGroupId);
    }

    // ========== Utility Methods ==========

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return widgetGroupRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public long countWidgetsByWidgetGroupId(Long widgetGroupId) {
        WidgetGroup widgetGroup = widgetGroupRepository.findById(widgetGroupId)
                .orElseThrow(() -> new ResourceNotFoundException("widgetGroup not found with id: " + widgetGroupId));
        return widgetGroup.getWidgets().size();
    }



}
