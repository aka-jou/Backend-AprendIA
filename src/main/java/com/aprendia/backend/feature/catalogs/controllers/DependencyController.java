package com.aprendia.backend.feature.catalogs.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import com.aprendia.backend.common.response.ApiMessage;
import com.aprendia.backend.common.dto.StatusRequestDTO;
import com.aprendia.backend.common.dto.ApiResponse;
import com.aprendia.backend.common.dto.PagedResponse;
import com.aprendia.backend.feature.catalogs.dto.DependencyRequestDTO;
import com.aprendia.backend.feature.catalogs.dto.DependencyResponseDTO;
import com.aprendia.backend.feature.catalogs.service.DependencyService;
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
@RequestMapping("/v1/catalogs/dependencies")
@Tag(name = "Catálogo de Dependencias", description = "Endpoints para la administración del catálogo de dependencias institucionales. Requieren JWT y rol ADMINISTRADOR.")
public class DependencyController {

    @Autowired
    private DependencyService dependencyService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Crear una dependencia", description = "Crea una nueva dependencia institucional. Requiere rol ADMINISTRADOR.")
    public ResponseEntity<DependencyResponseDTO> createDependency(@Valid @RequestBody DependencyRequestDTO request) {
        DependencyResponseDTO response = dependencyService.createDependency(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Obtener dependencias", description = "Lista paginada de dependencias activas; con incluirInactivos=true también las desactivadas. Requiere rol ADMINISTRADOR.")
    public ResponseEntity<PagedResponse<DependencyResponseDTO>> getAllDependencies(
            @Parameter(description = "true: incluye las dependencias desactivadas") @RequestParam(defaultValue = "false") boolean incluirInactivos,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(dependencyService.getAllDependencies(pageable, incluirInactivos));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualizar una dependencia", description = "Body: { nombre, tipo, codigoInterno, status }. status es opcional: si se omite se conserva. Requiere rol ADMINISTRADOR.")
    public ResponseEntity<DependencyResponseDTO> updateDependency(@PathVariable Long id, @Valid @RequestBody DependencyRequestDTO request) {
        return ResponseEntity.ok(dependencyService.updateDependency(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ApiMessage("Estatus de la dependencia actualizado exitosamente.")
    @Operation(summary = "Activar o desactivar una dependencia", description = "Body: {\"activo\": boolean} (también acepta status o isActive). Baja lógica: el registro se conserva.")
    public ResponseEntity<DependencyResponseDTO> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusRequestDTO request) {
        return ResponseEntity.ok(dependencyService.updateStatus(id, request.activo()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Eliminar una dependencia", description = "Elimina una dependencia sin usuarios adscritos. Si tiene usuarios responde 409: desactívela con PATCH .../status.")
    public ResponseEntity<ApiResponse<Void>> deleteDependency(@PathVariable Long id) {
        dependencyService.deleteDependency(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Dependencia eliminada exitosamente."));
    }
}
