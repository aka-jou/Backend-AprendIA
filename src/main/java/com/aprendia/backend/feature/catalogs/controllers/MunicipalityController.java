package com.aprendia.backend.feature.catalogs.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import com.aprendia.backend.common.response.ApiMessage;
import com.aprendia.backend.common.dto.StatusRequestDTO;
import com.aprendia.backend.common.dto.ApiResponse;
import com.aprendia.backend.common.dto.PagedResponse;
import com.aprendia.backend.feature.catalogs.dto.MunicipalityRequestDTO;
import com.aprendia.backend.feature.catalogs.dto.MunicipalityResponseDTO;
import com.aprendia.backend.feature.catalogs.service.MunicipalityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/catalogs/municipalities")
@Tag(name = "Catálogo de Municipios", description = "Endpoints para la gestión del catálogo de municipios")
public class MunicipalityController {

    @Autowired
    private MunicipalityService municipalityService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Crear municipio", description = "Registra un nuevo municipio en el catálogo.")
    public ResponseEntity<MunicipalityResponseDTO> createMunicipality(@Valid @RequestBody MunicipalityRequestDTO request) {
        return new ResponseEntity<>(municipalityService.createMunicipality(request), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Obtener municipios", description = "Lista paginada de municipios activos; con incluirInactivos=true también los desactivados.")
    public ResponseEntity<PagedResponse<MunicipalityResponseDTO>> getAllMunicipalities(
            @Parameter(description = "true: incluye los municipios desactivados") @RequestParam(defaultValue = "false") boolean incluirInactivos,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(municipalityService.getAllMunicipalities(pageable, incluirInactivos));
    }

    @GetMapping("/by-state/{stateId}")
    @Operation(summary = "Obtener municipios por estado", description = "Devuelve una lista paginada de municipios pertenecientes a un estado específico.")
    public ResponseEntity<PagedResponse<MunicipalityResponseDTO>> getMunicipalitiesByStateId(
            @PathVariable Long stateId,
            @Parameter(description = "true: incluye los municipios desactivados") @RequestParam(defaultValue = "false") boolean incluirInactivos,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(municipalityService.getMunicipalitiesByStateId(stateId, pageable, incluirInactivos));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualizar municipio", description = "Actualiza los datos de un municipio existente.")
    public ResponseEntity<MunicipalityResponseDTO> updateMunicipality(
            @PathVariable Long id, @Valid @RequestBody MunicipalityRequestDTO request) {
        return ResponseEntity.ok(municipalityService.updateMunicipality(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ApiMessage("Estatus del municipio actualizado exitosamente.")
    @Operation(summary = "Activar o desactivar un municipio", description = "Body: {\"activo\": boolean} (también acepta status o isActive). Baja lógica: el registro se conserva.")
    public ResponseEntity<MunicipalityResponseDTO> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusRequestDTO request) {
        return ResponseEntity.ok(municipalityService.updateStatus(id, request.activo()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Eliminar municipio", description = "Elimina un municipio que ningún domicilio usa. Si está en uso responde 409: desactívelo con PATCH .../status.")
    public ResponseEntity<ApiResponse<Void>> deleteMunicipality(@PathVariable Long id) {
        municipalityService.deleteMunicipality(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Municipio eliminado exitosamente."));
    }
}
