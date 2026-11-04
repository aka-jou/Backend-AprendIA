package com.aprendia.backend.feature.curriculum.controllers;

import com.aprendia.backend.common.dto.ApiResponse;
import com.aprendia.backend.common.response.ApiMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.aprendia.backend.feature.curriculum.dto.LayoutRequestDTO;
import com.aprendia.backend.feature.curriculum.dto.LayoutResponseDTO;
import com.aprendia.backend.feature.curriculum.service.LayoutService;

@RestController
@RequestMapping("/v1/layouts")
@Tag(name = "Layouts", description = "Plantillas interactivas que usan recursos, reactivos y ejercicios (Módulo 2). "
        + "Lectura: cualquier usuario autenticado. Escritura: ADMINISTRADOR y ASESOR_PEDAGOGICO.")
public class LayoutController {

    private static final String MANAGE = "hasAnyRole('ADMINISTRADOR', 'ASESOR_PEDAGOGICO')";

    @Autowired
    private LayoutService layoutService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @ApiMessage("Layouts obtenidos exitosamente.")
    @Operation(summary = "Listar layouts", description = "Filtros opcionales: idTipoLayout e isActive. Ordenado por ID.")
    public ResponseEntity<List<LayoutResponseDTO>> getAll(
            @Parameter(description = "Filtra por tipo de layout") @RequestParam(required = false) Long idTipoLayout,
            @Parameter(description = "true: solo habilitados; false: solo deshabilitados") @RequestParam(required = false) Boolean isActive) {
        return ResponseEntity.ok(layoutService.getLayouts(idTipoLayout, isActive));
    }

    @GetMapping("/{idLayout}")
    @PreAuthorize("isAuthenticated()")
    @ApiMessage("Layout obtenido exitosamente.")
    @Operation(summary = "Detalle de un layout")
    public ResponseEntity<LayoutResponseDTO> getById(@PathVariable Long idLayout) {
        return ResponseEntity.ok(layoutService.getLayout(idLayout));
    }

    @PostMapping
    @PreAuthorize(MANAGE)
    @ApiMessage("Layout registrado exitosamente.")
    @Operation(summary = "Crear layout", description = "codigo es único (409 si ya existe). 400 si idTipoLayout no existe.")
    public ResponseEntity<LayoutResponseDTO> create(@Valid @RequestBody LayoutRequestDTO request) {
        return new ResponseEntity<>(layoutService.createLayout(request), HttpStatus.CREATED);
    }

    @PutMapping("/{idLayout}")
    @PreAuthorize(MANAGE)
    @ApiMessage("Layout actualizado exitosamente.")
    @Operation(summary = "Actualizar layout")
    public ResponseEntity<LayoutResponseDTO> update(@PathVariable Long idLayout, @Valid @RequestBody LayoutRequestDTO request) {
        return ResponseEntity.ok(layoutService.updateLayout(idLayout, request));
    }

    @DeleteMapping("/{idLayout}")
    @PreAuthorize(MANAGE)
    @Operation(summary = "Eliminar layout", description = "409 si lo usan recursos o reactivos: en ese caso se desactiva con isActive=false.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long idLayout) {
        layoutService.deleteLayout(idLayout);
        return ResponseEntity.ok(ApiResponse.ok(null, "Layout eliminado exitosamente."));
    }
}
