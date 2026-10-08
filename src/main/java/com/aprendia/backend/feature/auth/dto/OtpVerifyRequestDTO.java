package com.aprendia.backend.feature.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record OtpVerifyRequestDTO(
    @NotBlank(message = "El correo electrónico es obligatorio.")
    @Email(message = "El correo electrónico no tiene un formato válido.")
    @Size(max = 100, message = "El correo electrónico no puede superar los 100 caracteres.")
    String email,

    @NotBlank(message = "El código de seguridad es obligatorio.")
    @Pattern(regexp = "\\d{6}", message = "El código de seguridad debe tener exactamente 6 dígitos.")
    String code
) {}
