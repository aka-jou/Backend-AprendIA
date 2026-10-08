package com.aprendia.backend.feature.catalogs.controllers;

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
@Tag(name = "Catálogo de Perfiles", description = "Endpoints para la administración del catálogo de perfiles de acceso. Requieren JWT y rol ADMIN.")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear un perfil", description = "Crea un nuevo perfil de acceso. Requiere rol ADMIN.")
    public ResponseEntity<ProfileResponseDTO> createProfile(@Valid @RequestBody ProfileRequestDTO request) {
        ProfileResponseDTO response = profileService.createProfile(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener perfiles", description = "Devuelve la lista de perfiles. Requiere rol ADMIN.")
    public ResponseEntity<List<ProfileResponseDTO>> getAllProfiles() {
        return ResponseEntity.ok(profileService.getAllProfiles());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar un perfil", description = "Actualiza el nombre y/o nivel de acceso de un perfil existente. Requiere rol ADMIN.")
    public ResponseEntity<ProfileResponseDTO> updateProfile(@PathVariable Long id, @Valid @RequestBody ProfileRequestDTO request) {
        return ResponseEntity.ok(profileService.updateProfile(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar un perfil", description = "Elimina de forma permanente un perfil del sistema. Requiere rol ADMIN.")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long id) {
        profileService.deleteProfile(id);
        return ResponseEntity.noContent().build();
    }
}
