package com.aprendia.backend.feature.catalogs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record StateRequestDTO(

    @NotBlank(message = "El nombre del estado es obligatorio.")
    String nombre,

    @NotNull(message = "El indicador de habilitación es obligatorio.")
    Boolean activo
) {}
