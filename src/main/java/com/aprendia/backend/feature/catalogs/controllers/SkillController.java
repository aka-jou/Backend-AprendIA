package com.aprendia.backend.feature.catalogs.controllers;

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
import com.aprendia.backend.feature.catalogs.dto.SkillRequestDTO;
import com.aprendia.backend.feature.catalogs.dto.SkillResponseDTO;
import com.aprendia.backend.feature.catalogs.service.SkillService;

/**
 * La spec nombra el módulo con dos rutas (/api/v1/catalogs/skills y /api/v1/skills): ambas llegan
 * al mismo controlador.
 */
@RestController
@RequestMapping({"/v1/catalogs/skills", "/v1/skills"})
@Tag(name = "Catálogo de habilidades", description = "Habilidades que se ponderan al 100% en cada reactivo (Módulo 3). "
        + "Lectura: cualquier usuario autenticado. Alta y edición: ADMINISTRADOR y ASESOR_PEDAGOGICO. Baja: ADMINISTRADOR.")
public class SkillController {

    private static final String MANAGE = "hasAnyRole('ADMINISTRADOR', 'ASESOR_PEDAGOGICO')";

    @Autowired
    private SkillService skillService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @ApiMessage("Habilidades obtenidas exitosamente.")
    @Operation(summary = "Listar habilidades", description = "Ordenadas por ID. ?status=true devuelve solo las activas.")
    public ResponseEntity<List<SkillResponseDTO>> getAll(
            @Parameter(description = "true: activas; false: inactivas; sin valor: todas") @RequestParam(required = false) Boolean status) {
        return ResponseEntity.ok(skillService.getSkills(status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @ApiMessage("Habilidad obtenida exitosamente.")
    @Operation(summary = "Detalle de una habilidad")
    public ResponseEntity<SkillResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(skillService.getSkill(id));
    }

    @PostMapping
    @PreAuthorize(MANAGE)
    @ApiMessage("Habilidad registrada exitosamente.")
    @Operation(summary = "Registrar habilidad", description = "Nombre único (409 si ya existe). idCategoria opcional.")
    public ResponseEntity<SkillResponseDTO> create(@Valid @RequestBody SkillRequestDTO request) {
        return new ResponseEntity<>(skillService.createSkill(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize(MANAGE)
    @ApiMessage("Habilidad actualizada exitosamente.")
    @Operation(summary = "Actualizar habilidad", description = "Si se omite status, se conserva.")
    public ResponseEntity<SkillResponseDTO> update(@PathVariable Long id, @Valid @RequestBody SkillRequestDTO request) {
        return ResponseEntity.ok(skillService.updateSkill(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Eliminar habilidad", description = "409 si la usan reactivos: en ese caso se desactiva con status=false.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        skillService.deleteSkill(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Habilidad eliminada exitosamente."));
    }
}
