package com.aprendia.backend.feature.catalogs.controllers;

import com.aprendia.backend.common.dto.PagedResponse;
import com.aprendia.backend.feature.catalogs.dto.RoleRequestDTO;
import com.aprendia.backend.feature.catalogs.dto.RoleResponseDTO;
import com.aprendia.backend.feature.catalogs.service.RoleService;
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
@RequestMapping("/v1/catalogs/roles")
@Tag(name = "Catálogo de Roles", description = "Endpoints para la administración del catálogo de roles del sistema. Requieren JWT y rol ADMIN.")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear un rol", description = "Crea un nuevo rol en el sistema. Requiere rol ADMIN.")
    public ResponseEntity<RoleResponseDTO> createRole(@Valid @RequestBody RoleRequestDTO request) {
        RoleResponseDTO response = roleService.createRole(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener roles", description = "Devuelve la lista paginada de roles. Requiere rol ADMIN.")
    public ResponseEntity<PagedResponse<RoleResponseDTO>> getAllRoles(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(roleService.getAllRoles(pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar un rol", description = "Actualiza el nombre y/o descripción de un rol existente. Requiere rol ADMIN.")
    public ResponseEntity<RoleResponseDTO> updateRole(@PathVariable Integer id, @Valid @RequestBody RoleRequestDTO request) {
        return ResponseEntity.ok(roleService.updateRole(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar un rol", description = "Elimina de forma permanente un rol del sistema. Requiere rol ADMIN.")
    public ResponseEntity<Void> deleteRole(@PathVariable Integer id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}
