package com.aprendia.backend.feature.catalogs.controllers;

import com.aprendia.backend.common.dto.PagedResponse;
import com.aprendia.backend.feature.catalogs.dto.StateRequestDTO;
import com.aprendia.backend.feature.catalogs.dto.StateResponseDTO;
import com.aprendia.backend.feature.catalogs.service.StateService;
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
@RequestMapping("/v1/catalogs/state")
@Tag(name = "Catálogo de Estados", description = "Endpoints para la administración del catálogo de estados del sistema. Requieren JWT y rol ADMIN.")
public class StateController {

    @Autowired
    private StateService stateService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear un estado", description = "Crea un nuevo estado en el sistema. Requiere rol ADMIN.")
    public ResponseEntity<StateResponseDTO> createState(@Valid @RequestBody StateRequestDTO request) {
        StateResponseDTO response = stateService.createState(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener estados", description = "Devuelve la lista paginada de estados. Requiere rol ADMIN.")
    public ResponseEntity<PagedResponse<StateResponseDTO>> getAllStates(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(stateService.getAllStates(pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar un estado", description = "Actualiza el nombre y/o indicador de habilitación de un estado existente. Requiere rol ADMIN.")
    public ResponseEntity<StateResponseDTO> updateState(@PathVariable Long id, @Valid @RequestBody StateRequestDTO request) {
        return ResponseEntity.ok(stateService.updateState(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar un estado", description = "Elimina de forma permanente un estado del sistema. Requiere rol ADMIN.")
    public ResponseEntity<Void> deleteState(@PathVariable Long id) {
        stateService.deleteState(id);
        return ResponseEntity.noContent().build();
    }
}
