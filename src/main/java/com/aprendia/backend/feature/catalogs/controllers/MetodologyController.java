package com.aprendia.backend.feature.catalogs.controllers;

import com.aprendia.backend.common.dto.PagedResponse;
import com.aprendia.backend.feature.catalogs.dto.MetodologyRequestDTO;
import com.aprendia.backend.feature.catalogs.dto.MetodologyResponseDTO;
import com.aprendia.backend.feature.catalogs.service.MetodologyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/catalogs/metodologies")
@Tag(name = "Catálogo de Metodologías", description = "Endpoints para la administración del catálogo de metodologías pedagógicas. Requieren JWT y rol ADMIN.")
public class MetodologyController {

    @Autowired
    private MetodologyService metodologyService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear una metodología", description = "Crea una nueva metodología pedagógica. Requiere rol ADMIN.")
    public ResponseEntity<MetodologyResponseDTO> createMetodology(@Valid @RequestBody MetodologyRequestDTO request) {
        MetodologyResponseDTO response = metodologyService.createMetodology(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener metodologías", description = "Devuelve la lista paginada de metodologías. Requiere rol ADMIN.")
    public ResponseEntity<PagedResponse<MetodologyResponseDTO>> getAllMetodologies(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(metodologyService.getAllMetodologies(pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar una metodología", description = "Actualiza el nombre y/o sigla de una metodología existente. Requiere rol ADMIN.")
    public ResponseEntity<MetodologyResponseDTO> updateMetodology(@PathVariable Long id, @Valid @RequestBody MetodologyRequestDTO request) {
        return ResponseEntity.ok(metodologyService.updateMetodology(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar una metodología", description = "Elimina de forma permanente una metodología del sistema. Requiere rol ADMIN.")
    public ResponseEntity<Void> deleteMetodology(@PathVariable Long id) {
        metodologyService.deleteMetodology(id);
        return ResponseEntity.noContent().build();
    }
}
