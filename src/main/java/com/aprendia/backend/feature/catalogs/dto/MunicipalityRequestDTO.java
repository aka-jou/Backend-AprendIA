package com.aprendia.backend.feature.catalogs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record MunicipalityRequestDTO(

    @NotBlank(message = "El nombre del municipio es obligatorio.")
    String nombre,

    @NotNull(message = "El ID del estado es obligatorio.")
    Long estadoId,

    @NotNull(message = "El indicador de habilitación es obligatorio.")
    Boolean activo
) {}
