package com.aprendia.backend.feature.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.hibernate.validator.constraints.URL;

import java.util.List;

/** Sección "assignment" del registro de usuario. */
@Builder
public record UserAssignmentDTO(
    @NotEmpty(message = "Debe asignar al menos un rol (ej. ['ADMINISTRADOR']).")
    List<@NotBlank(message = "Los roles no pueden estar vacíos.") String> roles,

    @NotBlank(message = "La dependencia o adscripción es obligatoria.")
    @Size(max = 100, message = "La dependencia no puede superar los 100 caracteres.")
    String dependency,

    @Size(max = 20, message = "El número INE no puede superar los 20 caracteres.")
    String ineNumber,

    @URL(message = "photoUrl debe ser una URL válida.")
    @Size(max = 255, message = "photoUrl no puede superar los 255 caracteres.")
    String photoUrl
) {}
