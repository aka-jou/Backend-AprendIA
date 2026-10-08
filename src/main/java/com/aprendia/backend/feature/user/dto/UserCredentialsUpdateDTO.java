package com.aprendia.backend.feature.user.dto;

import com.aprendia.backend.common.validation.ValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

/** Credenciales en la actualización: la contraseña es opcional (si se omite, no cambia). */
@Builder
public record UserCredentialsUpdateDTO(
    @NotBlank(message = "El nombre de usuario es obligatorio.")
    @Pattern(regexp = ValidationPatterns.USERNAME, message = "El usuario debe tener entre 8 y 50 caracteres (letras, números, '.', '_' o '-').")
    String username,

    @Pattern(regexp = ValidationPatterns.PASSWORD, message = "La contraseña debe tener mínimo 8 caracteres, con al menos una mayúscula, un número y un símbolo.")
    String password
) {}
