package com.aprendia.backend.feature.user.dto;

import com.aprendia.backend.common.validation.ValidationPatterns;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

/** PUT /api/v1/users/{id} — datos personales, rol, estado y (opcionalmente) credenciales. */
@Builder
public record UserUpdateRequestDTO(
    @Valid @NotNull(message = "La sección 'persona' es obligatoria.")
    UserPersonaDTO persona,

    @Valid
    UserCredentialsUpdateDTO credentials,

    @Valid @NotNull(message = "La sección 'assignment' es obligatoria.")
    UserAssignmentDTO assignment,

    @Pattern(regexp = ValidationPatterns.USER_STATUS, message = "status debe ser 'Activo' o 'Inactivo'.")
    String status
) {}
