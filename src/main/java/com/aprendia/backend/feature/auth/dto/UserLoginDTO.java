package com.aprendia.backend.feature.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UserLoginDTO(
    @NotBlank(message = "El nombre de usuario es obligatorio.")
    String username,

    @NotBlank(message = "La contraseña es obligatoria.")
    String password
) {}
