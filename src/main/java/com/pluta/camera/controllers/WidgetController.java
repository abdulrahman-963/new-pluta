package com.pluta.camera.controllers;


import com.pluta.camera.dtos.WidgetDTO;
import com.pluta.camera.dtos.WidgetGroupDTO;
import com.pluta.camera.services.WidgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@RestController
@RequestMapping("/v1/widgets-groups")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "WidgetGroup & Widget Management", description = "APIs for managing widget groups and widgets")
//@PreAuthorize("hasRole('SUPPER-ADMIN')")
public class WidgetController {

    private final WidgetService widgetService;

    // ========== WidgetGroup Endpoints ==========

    @GetMapping
    @Operation(summary = "Get all widget groups")
    public ResponseEntity<Page<WidgetGroupDTO>> getAllWidgetGroups(Pageable pageable) {
        log.debug("REST request to get all widget groups");
        Page<WidgetGroupDTO> widgetGroups = widgetService.findAll(pageable);
        return ResponseEntity.ok(widgetGroups);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get widget group by ID")
    public ResponseEntity<WidgetGroupDTO> getWidgetGroupById(@PathVariable Long id) {
        log.debug("REST request to get WidgetGroup : {}", id);
        WidgetGroupDTO widgetGroup = widgetService.findById(id);
        return ResponseEntity.ok(widgetGroup);
    }

    @GetMapping("/{id}/with-widgets")
    @Operation(summary = "Get widget group with all widgets")
    public ResponseEntity<WidgetGroupDTO> getWidgetGroupWithWidgets(@PathVariable Long id) {
        log.debug("REST request to get WidgetGroup with widgets : {}", id);
        WidgetGroupDTO widgetGroup = widgetService.findWidgetGroupWithWidgets(id);
        return ResponseEntity.ok(widgetGroup);
    }

    @PostMapping
    @Operation(summary = "Create a new widget group")
    public ResponseEntity<WidgetGroupDTO> createWidgetGroup(@Valid @RequestBody WidgetGroupDTO createDTO) {
        log.debug("REST request to create WidgetGroup : {}", createDTO);
        WidgetGroupDTO createdWidgetGroup = widgetService.createWidgetGroup(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdWidgetGroup);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing widget group")
    public ResponseEntity<WidgetGroupDTO> updateWidgetGroup(
            @PathVariable Long id,
            @Valid @RequestBody WidgetGroupDTO updateDTO) {
        log.debug("REST request to update WidgetGroup : {}, {}", id, updateDTO);
        WidgetGroupDTO updatedWidgetGroup = widgetService.updateWidgetGroup(id, updateDTO);
        return ResponseEntity.ok(updatedWidgetGroup);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a widget group")
    public ResponseEntity<Void> deleteWidgetGroup(@PathVariable Long id) {
        log.debug("REST request to delete WidgetGroup : {}", id);
        widgetService.deleteWidgetGroup(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if widget group exists")
    public ResponseEntity<Boolean> widgetGroupExists(@PathVariable Long id) {
        log.debug("REST request to check if WidgetGroup exists : {}", id);
        boolean exists = widgetService.existsById(id);
        return ResponseEntity.ok(exists);
    }

    // ========== Widget Endpoints ==========

    @GetMapping("/{widgetGroupId}/widgets")
    @Operation(summary = "Get all widgets for a widget group")
    public ResponseEntity<List<WidgetDTO>> getWidgetsByWidgetGroupId(@PathVariable Long widgetGroupId) {
        log.debug("REST request to get Widgets for WidgetGroup : {}", widgetGroupId);
        List<WidgetDTO> widgets = widgetService.findWidgetsByWidgetGroupId(widgetGroupId);
        return ResponseEntity.ok(widgets);
    }

    @GetMapping("/{widgetGroupId}/widgets/{widgetId}")
    @Operation(summary = "Get widget by ID within a widget group")
    public ResponseEntity<WidgetDTO> getWidgetById(
            @PathVariable Long widgetGroupId,
            @PathVariable Long widgetId) {
        log.debug("REST request to get Widget : {} in WidgetGroup : {}", widgetId, widgetGroupId);
        WidgetDTO widget = widgetService.findWidgetById(widgetGroupId, widgetId);
        return ResponseEntity.ok(widget);
    }

    @PostMapping("/{widgetGroupId}/widgets")
    @Operation(summary = "Add a new widget to widget group")
    public ResponseEntity<WidgetDTO> addWidget(
            @PathVariable Long widgetGroupId,
            @Valid @RequestBody WidgetDTO createDTO) {
        log.debug("REST request to add Widget to WidgetGroup : {}", widgetGroupId);
        WidgetDTO createdWidget = widgetService.addWidget(widgetGroupId, createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdWidget);
    }

    @PutMapping("/{widgetGroupId}/widgets/{widgetId}")
    @Operation(summary = "Update a widget")
    public ResponseEntity<WidgetDTO> updateWidget(
            @PathVariable Long widgetGroupId,
            @PathVariable Long widgetId,
            @Valid @RequestBody WidgetDTO updateDTO) {
        log.debug("REST request to update Widget : {} in WidgetGroup : {}", widgetId, widgetGroupId);
        WidgetDTO updatedWidget = widgetService.updateWidget(widgetGroupId, widgetId, updateDTO);
        return ResponseEntity.ok(updatedWidget);
    }

    @DeleteMapping("/{widgetGroupId}/widgets/{widgetId}")
    @Operation(summary = "Delete a widget")
    public ResponseEntity<Void> deleteWidget(
            @PathVariable Long widgetGroupId,
            @PathVariable Long widgetId) {
        log.debug("REST request to delete Widget : {} from WidgetGroup : {}", widgetId, widgetGroupId);
        widgetService.deleteWidget(widgetGroupId, widgetId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{widgetGroupId}/widgets/count")
    @Operation(summary = "Count widgets in a widget group")
    public ResponseEntity<Long> countWidgets(@PathVariable Long widgetGroupId) {
        log.debug("REST request to count Widgets in WidgetGroup : {}", widgetGroupId);
        long count = widgetService.countWidgetsByWidgetGroupId(widgetGroupId);
        return ResponseEntity.ok(count);
    }
}
