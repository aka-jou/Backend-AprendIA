package com.aprendia.backend.feature.auth.controllers;

import com.aprendia.backend.feature.auth.dto.*;
import com.aprendia.backend.feature.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Autenticación", description = "Endpoints para el inicio de sesión y validación de tokens.")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/admin")
    @Operation(summary = "Iniciar sesión (admin/usuario)", deprecated = true, description = "OBSOLETO: usar /auth/otp/send y /auth/otp/verify. Autentica por usuario y contraseña y devuelve un token JWT.", security = { @SecurityRequirement(name = "ApiKeyAuth") })
    public ResponseEntity<AuthResponseDTO> loginAdmin(@Valid @RequestBody UserLoginDTO loginRequest) {
        AuthResponseDTO response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/student")
    @Operation(summary = "Iniciar sesión (estudiante)", description = "Valida credenciales del estudiante mediante un código QR y devuelve un token JWT.", security = { @SecurityRequirement(name = "ApiKeyAuth") })
    public ResponseEntity<AuthResponseDTO> loginStudent(@Valid @RequestBody StudentQrLoginDTO studentQrLoginDTO) {
        AuthResponseDTO response = authService.loginStudent(studentQrLoginDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/validate-token")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Validar token JWT", description = "Valida que un token de acceso (JWT) esté vigente.")
    public ResponseEntity<AuthResponse> validateToken(@Valid @RequestBody TokenValidationRequestDTO request) {
        AuthResponse response = authService.validateToken(request.accessToken());
        return ResponseEntity.ok(response);
    }
}
