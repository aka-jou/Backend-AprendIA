package com.aprendia.backend.feature.curriculum.controllers;

import com.aprendia.backend.common.dto.ApiResponse;
import com.aprendia.backend.common.response.ApiMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.aprendia.backend.feature.curriculum.dto.LayoutTypeRequestDTO;
import com.aprendia.backend.feature.curriculum.dto.LayoutTypeResponseDTO;
import com.aprendia.backend.feature.curriculum.service.LayoutTypeService;

@RestController
@RequestMapping("/v1/layout-types")
@Tag(name = "Tipos de layout", description = "Categorías pedagógicas que agrupan las plantillas interactivas (Módulo 1). "
        + "Lectura: cualquier usuario autenticado. Escritura: ADMINISTRADOR y ASESOR_PEDAGOGICO.")
public class LayoutTypeController {

    private static final String MANAGE = "hasAnyRole('ADMINISTRADOR', 'ASESOR_PEDAGOGICO')";

    @Autowired
    private LayoutTypeService layoutTypeService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @ApiMessage("Tipos de layout obtenidos exitosamente.")
    @Operation(summary = "Listar tipos de layout")
    public ResponseEntity<List<LayoutTypeResponseDTO>> getAll() {
        return ResponseEntity.ok(layoutTypeService.getLayoutTypes());
    }

    @GetMapping("/{idTipoLayout}")
    @PreAuthorize("isAuthenticated()")
    @ApiMessage("Tipo de layout obtenido exitosamente.")
    @Operation(summary = "Detalle de un tipo de layout")
    public ResponseEntity<LayoutTypeResponseDTO> getById(@PathVariable Long idTipoLayout) {
        return ResponseEntity.ok(layoutTypeService.getLayoutType(idTipoLayout));
    }

    @PostMapping
    @PreAuthorize(MANAGE)
    @ApiMessage("Tipo de layout registrado exitosamente.")
    @Operation(summary = "Crear tipo de layout", description = "409 si ya existe otro con el mismo nombre.")
    public ResponseEntity<LayoutTypeResponseDTO> create(@Valid @RequestBody LayoutTypeRequestDTO request) {
        return new ResponseEntity<>(layoutTypeService.createLayoutType(request), HttpStatus.CREATED);
    }

    @PutMapping("/{idTipoLayout}")
    @PreAuthorize(MANAGE)
    @ApiMessage("Tipo de layout actualizado exitosamente.")
    @Operation(summary = "Actualizar tipo de layout")
    public ResponseEntity<LayoutTypeResponseDTO> update(@PathVariable Long idTipoLayout,
                                                        @Valid @RequestBody LayoutTypeRequestDTO request) {
        return ResponseEntity.ok(layoutTypeService.updateLayoutType(idTipoLayout, request));
    }

    @DeleteMapping("/{idTipoLayout}")
    @PreAuthorize(MANAGE)
    @Operation(summary = "Eliminar tipo de layout", description = "409 si tiene layouts asociados.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long idTipoLayout) {
        layoutTypeService.deleteLayoutType(idTipoLayout);
        return ResponseEntity.ok(ApiResponse.ok(null, "Tipo de layout eliminado exitosamente."));
    }
}
