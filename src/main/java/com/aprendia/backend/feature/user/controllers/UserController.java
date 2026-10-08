package com.aprendia.backend.feature.user.controllers;

import com.aprendia.backend.common.dto.ApiResponse;
import com.aprendia.backend.common.dto.PagedResponse;
import com.aprendia.backend.common.response.ApiMessage;
import com.aprendia.backend.feature.user.dto.UserCreateRequestDTO;
import com.aprendia.backend.feature.user.dto.UserDetailDTO;
import com.aprendia.backend.feature.user.dto.UserSummaryDTO;
import com.aprendia.backend.feature.user.dto.UserUpdateRequestDTO;
import com.aprendia.backend.feature.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
@Tag(name = "Usuarios", description = "Personal docente, asesores, supervisores y administradores (Especificación v2.0, Módulo 2). Requieren JWT y rol ADMINISTRADOR.")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ApiMessage("Usuarios recuperados exitosamente.")
    @Operation(summary = "Listado paginado de usuarios",
            description = "Parámetros: page (base 0), size (default 10), query (busca en usuario, correo, nombre o CURP exacta). Excluye estudiantes.")
    public ResponseEntity<PagedResponse<UserSummaryDTO>> getUsers(
            @Parameter(description = "Texto de búsqueda: nombre, correo, usuario o CURP completa")
            @RequestParam(required = false) String query,
            @ParameterObject @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(userService.getUsers(query, pageable));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ApiMessage("Usuario registrado exitosamente.")
    @Operation(summary = "Registrar usuario", description = "Alta con persona, credenciales y asignación (roles y dependencia).")
    public ResponseEntity<UserDetailDTO> createUser(@Valid @RequestBody UserCreateRequestDTO request) {
        return new ResponseEntity<>(userService.createUser(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ApiMessage("Usuario obtenido exitosamente.")
    @Operation(summary = "Expediente y detalle completo del usuario")
    public ResponseEntity<UserDetailDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ApiMessage("Usuario actualizado exitosamente.")
    @Operation(summary = "Actualizar datos personales, rol o estado",
            description = "credentials es opcional; si password se omite, no cambia. status acepta 'Activo' o 'Inactivo'.")
    public ResponseEntity<UserDetailDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequestDTO request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Desactivar / dar de baja el acceso del usuario", description = "Baja lógica (is_active = false); el registro se conserva.")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Usuario desactivado exitosamente."));
    }
}
