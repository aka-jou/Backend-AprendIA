package com.aprendia.backend.feature.user.dto;

import com.aprendia.backend.common.validation.ValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

/** Sección "credentials" del registro de usuario. */
@Builder
public record UserCredentialsDTO(
    @NotBlank(message = "El nombre de usuario es obligatorio.")
    @Pattern(regexp = ValidationPatterns.USERNAME, message = "El usuario debe tener entre 8 y 50 caracteres (letras, números, '.', '_' o '-').")
    String username,

    @NotBlank(message = "La contraseña es obligatoria.")
    @Pattern(regexp = ValidationPatterns.PASSWORD, message = "La contraseña debe tener mínimo 8 caracteres, con al menos una mayúscula, un número y un símbolo.")
    String password
) {}
