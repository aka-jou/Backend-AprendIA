package com.aprendia.backend.feature.auth.controllers;

import com.aprendia.backend.common.response.ApiMessage;
import com.aprendia.backend.feature.auth.dto.OtpSendRequestDTO;
import com.aprendia.backend.feature.auth.dto.OtpSendResponseDTO;
import com.aprendia.backend.feature.auth.dto.OtpVerifyRequestDTO;
import com.aprendia.backend.feature.auth.dto.OtpVerifyResponseDTO;
import com.aprendia.backend.feature.auth.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth/otp")
@Tag(name = "Autenticación OTP", description = "Login passwordless de 2 pasos con código de 6 dígitos enviado al correo.")
public class OtpAuthController {

    @Autowired
    private OtpService otpService;

    @PostMapping("/send")
    @ApiMessage("Código de seguridad enviado exitosamente al correo.")
    @Operation(summary = "Paso 1: enviar código OTP",
            description = "Envía un código de 6 dígitos al correo. Responde igual si el correo no existe.",
            security = { @SecurityRequirement(name = "ApiKeyAuth") })
    public ResponseEntity<OtpSendResponseDTO> sendOtp(@Valid @RequestBody OtpSendRequestDTO request) {
        return ResponseEntity.ok(otpService.sendOtp(request.email()));
    }

    @PostMapping("/verify")
    @ApiMessage("Autenticación exitosa.")
    @Operation(summary = "Paso 2: verificar código OTP e iniciar sesión",
            description = "Valida el código y devuelve el token JWT con los datos del usuario.",
            security = { @SecurityRequirement(name = "ApiKeyAuth") })
    public ResponseEntity<OtpVerifyResponseDTO> verifyOtp(@Valid @RequestBody OtpVerifyRequestDTO request) {
        return ResponseEntity.ok(otpService.verifyOtp(request.email(), request.code()));
    }
}
