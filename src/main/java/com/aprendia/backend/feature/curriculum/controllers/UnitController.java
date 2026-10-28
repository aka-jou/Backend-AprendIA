package com.aprendia.backend.feature.curriculum.controllers;

import com.aprendia.backend.common.dto.ApiResponse;
import com.aprendia.backend.common.response.ApiMessage;
import com.aprendia.backend.feature.curriculum.dto.UnitRequestDTO;
import com.aprendia.backend.feature.curriculum.dto.UnitResponseDTO;
import com.aprendia.backend.feature.curriculum.service.UnitService;
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

@RestController
@RequestMapping("/v1/units")
@Tag(name = "Unidades didácticas", description = "Unidades de un cuadernillo (anexo curricular, sección 3). "
        + "GET por id, PUT y DELETE no están en la spec; se agregan por consistencia con /workbooks.")
public class UnitController {

    private static final String MANAGE = "hasAnyRole('ADMINISTRADOR', 'ASESOR_PEDAGOGICO')";

    @Autowired
    private UnitService unitService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @ApiMessage("Unidades obtenidas exitosamente.")
    @Operation(summary = "Listado de unidades", description = "Ordenado por orden e ID. 404 si idCuadernillo no existe o no es visible.")
    public ResponseEntity<List<UnitResponseDTO>> getAll(
            @Parameter(description = "Filtra por cuadernillo") @RequestParam(required = false) Long idCuadernillo) {
        return ResponseEntity.ok(unitService.getUnits(idCuadernillo));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @ApiMessage("Unidad obtenida exitosamente.")
    @Operation(summary = "Detalle de una unidad")
    public ResponseEntity<UnitResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(unitService.getUnitById(id));
    }

    @PostMapping
    @PreAuthorize(MANAGE)
    @ApiMessage("Unidad registrada exitosamente.")
    @Operation(summary = "Registrar unidad", description = "orden es opcional (por defecto 1). 400 si el cuadernillo no existe.")
    public ResponseEntity<UnitResponseDTO> create(@Valid @RequestBody UnitRequestDTO request) {
        return new ResponseEntity<>(unitService.createUnit(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize(MANAGE)
    @ApiMessage("Unidad actualizada exitosamente.")
    @Operation(summary = "Actualizar unidad", description = "Permite moverla a otro cuadernillo cambiando idCuadernillo. Si se omite orden, se conserva.")
    public ResponseEntity<UnitResponseDTO> update(@PathVariable Long id, @Valid @RequestBody UnitRequestDTO request) {
        return ResponseEntity.ok(unitService.updateUnit(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(MANAGE)
    @Operation(summary = "Eliminar unidad", description = "Baja lógica de la unidad y de sus temas y recursos.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        unitService.deleteUnit(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Unidad eliminada exitosamente."));
    }
}
