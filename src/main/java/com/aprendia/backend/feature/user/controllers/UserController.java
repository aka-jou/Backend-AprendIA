package com.aprendia.backend.feature.user.controllers;

import com.aprendia.backend.feature.user.dto.StudentRequest;
import com.aprendia.backend.feature.user.dto.StudentResponse;
import com.aprendia.backend.feature.user.dto.UserResponse;
import com.aprendia.backend.common.dto.PagedResponse;
import com.aprendia.backend.feature.user.service.UserService;
import com.aprendia.backend.feature.user.dto.RegisterRequest;
import com.aprendia.backend.feature.user.dto.StudentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/users")
@Tag(name = "Usuarios", description = "Endpoints para la consulta y administración de perfiles de usuario. Requieren JWT.")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @Operation(summary = "Obtener todos los usuarios", description = "Devuelve una lista completa de los usuarios registrados. Requiere rol de administrador.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID", description = "Devuelve los detalles de un usuario en base a su ID. Requiere autenticación.")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }


    @GetMapping("/students")
    @Operation(summary = "Obtener estudiantes", description = "Devuelve una lista paginada de estudiantes. Requiere rol ADMIN.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<StudentDto>> getAllStudents(
            @ParameterObject @PageableDefault(size = 100) Pageable pageable) {
        Page<StudentDto> students = userService.getAllStudents(pageable);
        return ResponseEntity.ok(PagedResponse.fromPage(students));
    }

    @GetMapping("/students/{id}")
    @Operation(summary = "Obtener un estudiante por ID", description = "Retorna la información completa de un estudiante específico. Requiere rol ADMIN.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentDto> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getStudentById(id));
    }

    @PostMapping("/register")
    @SecurityRequirements
    @Operation(summary = "Registrar un nuevo usuario", description = "Crea una nueva cuenta de usuario con rol USER.")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        UserResponse response = userService.register(registerRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @PostMapping("/students")
    @Operation(summary = "Registrar estudiante", description = "Registra un estudiante con datos personales, domicilio y parientes. Requiere rol ADMIN.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Estudiante registrado exitosamente."),
        @ApiResponse(responseCode = "400", description = "Error de validación en datos de entrada."),
        @ApiResponse(responseCode = "401", description = "No autenticado."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos suficientes."),
        @ApiResponse(responseCode = "500", description = "Error inesperado.")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentResponse> registerStudent(@Valid @RequestBody StudentRequest request) {
        StudentResponse response = userService.registerStudent(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @Operation(summary = "Actualizar un estudiante", description = "Actualiza la información personal, dirección y familiares de un estudiante. Requiere rol ADMIN.")
    @PutMapping("/students/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentDto> updateStudent(
            @PathVariable Long id, 
            @Valid @RequestBody StudentRequest request) {
        return ResponseEntity.ok(userService.updateStudent(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un usuario", description = "Elimina de forma permanente un usuario por su ID. Requiere rol de administrador.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

}
