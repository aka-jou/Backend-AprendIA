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
import com.aprendia.backend.feature.curriculum.dto.ExerciseRequestDTO;
import com.aprendia.backend.feature.curriculum.dto.ExerciseResponseDTO;
import com.aprendia.backend.feature.curriculum.dto.ReactivoRequestDTO;
import com.aprendia.backend.feature.curriculum.dto.ReactivoResponseDTO;
import com.aprendia.backend.feature.curriculum.service.ExerciseService;
import com.aprendia.backend.feature.curriculum.service.ReactivoService;

@RestController
@RequestMapping("/v1/reactivos")
@Tag(name = "Reactivos", description = "Reactivos pedagógicos de un tema con sus habilidades y ejercicios (Módulo 4), "
        + "y alta/listado de ejercicios de un reactivo (Módulo 5). Lectura: cualquier usuario autenticado "
        + "(sin permisos de gestión solo se ven los de status=active). Escritura: ADMINISTRADOR y ASESOR_PEDAGOGICO.")
public class ReactivoController {

    private static final String MANAGE = "hasAnyRole('ADMINISTRADOR', 'ASESOR_PEDAGOGICO')";

    @Autowired
    private ReactivoService reactivoService;

    @Autowired
    private ExerciseService exerciseService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @ApiMessage("Reactivos obtenidos exitosamente.")
    @Operation(summary = "Listar reactivos", description = "Filtro opcional idTema. Cada reactivo trae sus ejercicios "
            + "en formato web (exercises) y móvil (instances). 404 si idTema no existe o no es visible.")
    public ResponseEntity<List<ReactivoResponseDTO>> getAll(
            @Parameter(description = "Filtra por tema") @RequestParam(required = false) Long idTema) {
        return ResponseEntity.ok(reactivoService.getReactivos(idTema));
    }

    @GetMapping("/{idReactivo}")
    @PreAuthorize("isAuthenticated()")
    @ApiMessage("Reactivo obtenido exitosamente.")
    @Operation(summary = "Detalle de un reactivo con todos sus ejercicios")
    public ResponseEntity<ReactivoResponseDTO> getById(@PathVariable Long idReactivo) {
        return ResponseEntity.ok(reactivoService.getReactivo(idReactivo));
    }

    @PostMapping
    @PreAuthorize(MANAGE)
    @ApiMessage("Reactivo registrado exitosamente.")
    @Operation(summary = "Crear reactivo", description = "Con sus habilidades (pesos que suman 100), habilidades previas "
            + "(por nombre, del catálogo) y su lista de ejercicios. El config de cada ejercicio se valida según el layout.")
    public ResponseEntity<ReactivoResponseDTO> create(@Valid @RequestBody ReactivoRequestDTO request) {
        return new ResponseEntity<>(reactivoService.createReactivo(request), HttpStatus.CREATED);
    }

    @PutMapping("/{idReactivo}")
    @PreAuthorize(MANAGE)
    @ApiMessage("Reactivo actualizado exitosamente.")
    @Operation(summary = "Actualizar reactivo", description = "Reemplazo completo. En exercises[], un ejercicio con id se "
            + "actualiza y conserva su ID; sin id se crea; los que no se envían se eliminan.")
    public ResponseEntity<ReactivoResponseDTO> update(@PathVariable Long idReactivo, @Valid @RequestBody ReactivoRequestDTO request) {
        return ResponseEntity.ok(reactivoService.updateReactivo(idReactivo, request));
    }

    @DeleteMapping("/{idReactivo}")
    @PreAuthorize(MANAGE)
    @Operation(summary = "Eliminar reactivo", description = "Baja lógica del reactivo y de todos sus ejercicios.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long idReactivo) {
        reactivoService.deleteReactivo(idReactivo);
        return ResponseEntity.ok(ApiResponse.ok(null, "Reactivo eliminado exitosamente."));
    }

    @GetMapping("/{idReactivo}/exercises")
    @PreAuthorize("isAuthenticated()")
    @ApiMessage("Ejercicios obtenidos exitosamente.")
    @Operation(summary = "Listar ejercicios de un reactivo", description = "Ordenados por order.")
    public ResponseEntity<List<ExerciseResponseDTO>> getExercises(@PathVariable Long idReactivo) {
        return ResponseEntity.ok(exerciseService.getExercises(idReactivo));
    }

    @PostMapping("/{idReactivo}/exercises")
    @PreAuthorize(MANAGE)
    @ApiMessage("Ejercicio guardado exitosamente.")
    @Operation(summary = "Agregar ejercicio a un reactivo", description = "templateType es opcional; si viene, debe ser el layout del reactivo.")
    public ResponseEntity<ExerciseResponseDTO> createExercise(@PathVariable Long idReactivo,
                                                              @Valid @RequestBody ExerciseRequestDTO request) {
        return new ResponseEntity<>(exerciseService.createExercise(idReactivo, request), HttpStatus.CREATED);
    }
}
