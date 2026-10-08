package com.aprendia.backend.feature.catalogs.controllers;

import com.aprendia.backend.common.dto.ApiResponse;
import com.aprendia.backend.common.response.ApiMessage;
import com.aprendia.backend.feature.catalogs.dto.MetodologyRequestDTO;
import com.aprendia.backend.feature.catalogs.dto.MetodologyResponseDTO;
import com.aprendia.backend.feature.catalogs.service.MetodologyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/catalogs/metodologies")
@Tag(name = "Catálogo de Metodologías", description = "Metodologías pedagógicas (Especificación v2.0, sección 5.1). Requieren JWT y rol ADMINISTRADOR.")
public class MetodologyController {

    @Autowired
    private MetodologyService metodologyService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ApiMessage("Metodología creada exitosamente.")
    @Operation(summary = "Crear Metodología")
    public ResponseEntity<MetodologyResponseDTO> create(@Valid @RequestBody MetodologyRequestDTO request) {
        return new ResponseEntity<>(metodologyService.createMetodology(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ApiMessage("Metodologías obtenidas exitosamente.")
    @Operation(summary = "Listar Metodologías", description = "Lista completa (sin paginar), ordenada por ID, como la consume el frontend.")
    public ResponseEntity<List<MetodologyResponseDTO>> getAll() {
        return ResponseEntity.ok(metodologyService.getAllMetodologies());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ApiMessage("Metodología obtenida exitosamente.")
    @Operation(summary = "Obtener Metodología por ID")
    public ResponseEntity<MetodologyResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(metodologyService.getMetodologyById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ApiMessage("Metodología actualizada exitosamente.")
    @Operation(summary = "Actualizar Metodología")
    public ResponseEntity<MetodologyResponseDTO> update(@PathVariable Long id, @Valid @RequestBody MetodologyRequestDTO request) {
        return ResponseEntity.ok(metodologyService.updateMetodology(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Eliminar Metodología", description = "Responde 409 si otros registros lo referencian; en ese caso use status=false.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        metodologyService.deleteMetodology(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Metodología eliminada exitosamente."));
    }
}
