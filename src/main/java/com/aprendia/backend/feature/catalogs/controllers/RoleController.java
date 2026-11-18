package com.aprendia.backend.feature.catalogs.controllers;

import com.aprendia.backend.common.dto.ApiResponse;
import com.aprendia.backend.common.dto.PagedResponse;
import com.aprendia.backend.common.dto.StatusRequestDTO;
import com.aprendia.backend.common.response.ApiMessage;
import com.aprendia.backend.feature.catalogs.dto.RoleRequestDTO;
import com.aprendia.backend.feature.catalogs.dto.RoleResponseDTO;
import com.aprendia.backend.feature.catalogs.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@Tag(name = "Catálogo de Roles", description = "Endpoints para la administración del catálogo de roles del sistema. Requieren JWT y rol ADMINISTRADOR.")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Crear un rol", description = "Crea un nuevo rol en el sistema. Requiere rol ADMINISTRADOR.")
    public ResponseEntity<RoleResponseDTO> createRole(@Valid @RequestBody RoleRequestDTO request) {
        RoleResponseDTO response = roleService.createRole(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Obtener roles", description = "Devuelve la lista paginada de roles activos; con incluirInactivos=true también los desactivados. Requiere rol ADMINISTRADOR.")
    public ResponseEntity<PagedResponse<RoleResponseDTO>> getAllRoles(
            @Parameter(description = "true: incluye los roles desactivados") @RequestParam(defaultValue = "false") boolean incluirInactivos,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(roleService.getAllRoles(pageable, incluirInactivos));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualizar un rol", description = "Actualiza el nombre y/o descripción de un rol existente. Requiere rol ADMINISTRADOR.")
    public ResponseEntity<RoleResponseDTO> updateRole(@PathVariable Integer id, @Valid @RequestBody RoleRequestDTO request) {
        return ResponseEntity.ok(roleService.updateRole(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ApiMessage("Estatus del rol actualizado exitosamente.")
    @Operation(summary = "Activar o desactivar un rol", description = "Body: {\"activo\": boolean}. Los roles oficiales no se pueden desactivar (409).")
    public ResponseEntity<RoleResponseDTO> updateStatus(@PathVariable Integer id, @Valid @RequestBody StatusRequestDTO request) {
        return ResponseEntity.ok(roleService.updateStatus(id, request.activo()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Eliminar un rol", description = "Elimina un rol que ningún usuario tiene asignado. Si está en uso responde 409: desactívelo con PATCH .../status.")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Integer id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Rol eliminado exitosamente."));
    }
}
