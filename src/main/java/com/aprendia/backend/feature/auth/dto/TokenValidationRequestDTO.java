package com.aprendia.backend.feature.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record TokenValidationRequestDTO(
    @NotBlank(message = "El token de acceso es obligatorio.")
    String accessToken
) {}
