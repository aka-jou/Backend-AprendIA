package com.aprendia.backend.feature.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record RegisterRequest(

    @NotBlank(message = "El nombre de usuario es obligatorio.")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres.")
    String username,

    @NotBlank(message = "El correo electrónico es obligatorio.")
    @Email(message = "El formato de correo electrónico no es válido.")
    @Size(max = 100, message = "El correo electrónico no puede superar los 100 caracteres.")
    String email,

    @NotBlank(message = "La contraseña es obligatoria.")
    @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres.")
    String password
) {}

