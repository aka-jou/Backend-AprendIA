package com.aprendia.backend.feature.catalogs.controllers;

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
    @Operation(summary = "Obtener municipios", description = "Devuelve una lista paginada de todos los municipios.")
    public ResponseEntity<PagedResponse<MunicipalityResponseDTO>> getAllMunicipalities(
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(municipalityService.getAllMunicipalities(pageable));
    }

    @GetMapping("/by-state/{stateId}")
    @Operation(summary = "Obtener municipios por estado", description = "Devuelve una lista paginada de municipios pertenecientes a un estado específico.")
    public ResponseEntity<PagedResponse<MunicipalityResponseDTO>> getMunicipalitiesByStateId(
            @PathVariable Long stateId,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(municipalityService.getMunicipalitiesByStateId(stateId, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualizar municipio", description = "Actualiza los datos de un municipio existente.")
    public ResponseEntity<MunicipalityResponseDTO> updateMunicipality(
            @PathVariable Long id, @Valid @RequestBody MunicipalityRequestDTO request) {
        return ResponseEntity.ok(municipalityService.updateMunicipality(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Eliminar municipio", description = "Elimina físicamente un municipio del catálogo.")
    public ResponseEntity<ApiResponse<Void>> deleteMunicipality(@PathVariable Long id) {
        municipalityService.deleteMunicipality(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Municipio eliminado exitosamente."));
    }
}
