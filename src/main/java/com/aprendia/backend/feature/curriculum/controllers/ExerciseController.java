package com.aprendia.backend.feature.curriculum.controllers;

import com.aprendia.backend.common.dto.ApiResponse;
import com.aprendia.backend.common.response.ApiMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.aprendia.backend.feature.curriculum.dto.ExerciseRequestDTO;
import com.aprendia.backend.feature.curriculum.dto.ExerciseResponseDTO;
import com.aprendia.backend.feature.curriculum.service.ExerciseService;

@RestController
@RequestMapping("/v1/exercises")
@Tag(name = "Ejercicios", description = "Detalle, edición y baja de un ejercicio (Módulo 5). El alta y el listado están en "
        + "/v1/reactivos/{idReactivo}/exercises.")
public class ExerciseController {

    private static final String MANAGE = "hasAnyRole('ADMINISTRADOR', 'ASESOR_PEDAGOGICO')";

    @Autowired
    private ExerciseService exerciseService;

    @GetMapping("/{idEjercicio}")
    @PreAuthorize("isAuthenticated()")
    @ApiMessage("Ejercicio obtenido exitosamente.")
    @Operation(summary = "Detalle de un ejercicio")
    public ResponseEntity<ExerciseResponseDTO> getById(@PathVariable Long idEjercicio) {
        return ResponseEntity.ok(exerciseService.getExercise(idEjercicio));
    }

    @PutMapping("/{idEjercicio}")
    @PreAuthorize(MANAGE)
    @ApiMessage("Ejercicio guardado exitosamente.")
    @Operation(summary = "Actualizar ejercicio", description = "Orden, instrucción y config. No cambia de reactivo.")
    public ResponseEntity<ExerciseResponseDTO> update(@PathVariable Long idEjercicio, @Valid @RequestBody ExerciseRequestDTO request) {
        return ResponseEntity.ok(exerciseService.updateExercise(idEjercicio, request));
    }

    @DeleteMapping("/{idEjercicio}")
    @PreAuthorize(MANAGE)
    @Operation(summary = "Eliminar ejercicio", description = "Baja lógica del ejercicio.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long idEjercicio) {
        exerciseService.deleteExercise(idEjercicio);
        return ResponseEntity.ok(ApiResponse.ok(null, "Ejercicio eliminado exitosamente."));
    }
}
