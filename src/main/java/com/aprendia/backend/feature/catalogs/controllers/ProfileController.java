package com.aprendia.backend.feature.catalogs.controllers;

import com.aprendia.backend.common.dto.ApiResponse;
import com.aprendia.backend.common.response.ApiMessage;
import com.aprendia.backend.feature.catalogs.dto.ProfileRequestDTO;
import com.aprendia.backend.feature.catalogs.dto.ProfileResponseDTO;
import com.aprendia.backend.feature.catalogs.service.ProfileService;
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
@RequestMapping("/v1/catalogs/profiles")
@Tag(name = "Catálogo de Perfiles", description = "Perfiles pedagógicos y de personal (Especificación v2.0, sección 5.3). Requieren JWT y rol ADMINISTRADOR.")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ApiMessage("Perfil creado exitosamente.")
    @Operation(summary = "Crear Perfil")
    public ResponseEntity<ProfileResponseDTO> create(@Valid @RequestBody ProfileRequestDTO request) {
        return new ResponseEntity<>(profileService.createProfile(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ApiMessage("Perfiles obtenidos exitosamente.")
    @Operation(summary = "Listar Perfiles", description = "Lista completa (sin paginar), ordenada por ID, como la consume el frontend.")
    public ResponseEntity<List<ProfileResponseDTO>> getAll() {
        return ResponseEntity.ok(profileService.getAllProfiles());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ApiMessage("Perfil obtenido exitosamente.")
    @Operation(summary = "Obtener Perfil por ID")
    public ResponseEntity<ProfileResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(profileService.getProfileById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ApiMessage("Perfil actualizado exitosamente.")
    @Operation(summary = "Actualizar Perfil")
    public ResponseEntity<ProfileResponseDTO> update(@PathVariable Long id, @Valid @RequestBody ProfileRequestDTO request) {
        return ResponseEntity.ok(profileService.updateProfile(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Eliminar Perfil", description = "Responde 409 si otros registros lo referencian; en ese caso use status=false.")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        profileService.deleteProfile(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Perfil eliminado exitosamente."));
    }
}
