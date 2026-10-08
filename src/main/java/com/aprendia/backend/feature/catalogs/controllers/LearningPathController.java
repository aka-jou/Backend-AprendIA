package com.aprendia.backend.feature.catalogs.controllers;

import com.aprendia.backend.common.dto.ApiResponse;
import com.aprendia.backend.common.response.ApiMessage;
import com.aprendia.backend.feature.catalogs.dto.LearningPathRequestDTO;
import com.aprendia.backend.feature.catalogs.dto.LearningPathResponseDTO;
import com.aprendia.backend.feature.catalogs.service.LearningPathService;
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
@RequestMapping("/v1/catalogs/learning-paths")
@Tag(name = "Catálogo de Rutas de Aprendizaje", description = "Rutas de aprendizaje ligadas a una metodología (Especificación v2.0, sección 5.2). Requieren JWT y rol ADMINISTRADOR.")
public class LearningPathController {

    @Autowired
    private LearningPathService learningPathService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ApiMessage("Ruta de aprendizaje creada exitosamente.")
    @Operation(summary = "Crear Ruta de aprendizaje")
    public ResponseEntity<LearningPathResponseDTO> create(@Valid @RequestBody LearningPathRequestDTO request) {
        return new ResponseEntity<>(learningPathService.createLearningPath(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ApiMessage("Rutas de aprendizaje obtenidas exitosamente.")
    @Operation(summary = "Listar Rutas de aprendizaje", description = "Lista completa (sin paginar), ordenada por ID, como la consume el frontend.")
    public ResponseEntity<List<LearningPathResponseDTO>> getAll() {
        return ResponseEntity.ok(learningPathService.getAllLearningPaths());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ApiMessage("Ruta de aprendizaje obtenida exitosamente.")
    @Operation(summary = "Obtener Ruta de aprendizaje por ID")
    public ResponseEntity<LearningPathResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(learningPathService.getLearningPathById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ApiMessage("Ruta de aprendizaje actualizada exitosamente.")
    @Operation(summary = "Actualizar Ruta de aprendizaje")
    public ResponseEntity<LearningPathResponseDTO> update(@PathVariable Long id, @Valid @RequestBody LearningPathRequestDTO request) {
        return ResponseEntity.ok(learningPathService.updateLearningPath(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Eliminar Ruta de aprendizaje", description = "Responde 409 si otros registros lo referencian; en ese caso use status=false.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        learningPathService.deleteLearningPath(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Ruta de aprendizaje eliminada exitosamente."));
    }
}
