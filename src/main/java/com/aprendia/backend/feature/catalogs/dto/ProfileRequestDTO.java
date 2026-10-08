package com.aprendia.backend.feature.catalogs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ProfileRequestDTO(
    @NotBlank(message = "El nombre del perfil es obligatorio.")
    @Size(max = 100, message = "El nombre del perfil no puede superar los 100 caracteres.")
    String nombre,

    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres.")
    String descripcion,

    @NotNull(message = "El estatus es obligatorio.")
    Boolean status
) {}
