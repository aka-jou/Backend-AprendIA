package com.aprendia.backend.feature.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record PersonRequest(
    @NotBlank(message = "First name is obligatory")
    String firstName,

    String middleName,

    @NotBlank(message = "Last name is obligatory")
    String lastName,

    String secondLastName,

    @NotBlank(message = "CURP is obligatory")
    String curp,

    LocalDateTime birthDate,

    @Size(max = 1, message = "El género debe ser de máximo 1 carácter (Ej: 'M' o 'F')")
    String gender,

    @Size(max = 50, message = "El teléfono no puede exceder los 50 caracteres")
    String phone,

    String imageUrl
) {}
