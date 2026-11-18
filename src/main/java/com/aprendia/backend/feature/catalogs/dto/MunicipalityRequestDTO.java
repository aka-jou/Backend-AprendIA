package com.aprendia.backend.feature.catalogs.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record MunicipalityRequestDTO(

    @JsonAlias("name")
    @NotBlank(message = "El nombre del municipio es obligatorio.")
    String nombre,

    @NotNull(message = "El ID del estado es obligatorio.")
    Long estadoId,

    /** Opcional (alias "status"). Si se omite en PUT se conserva el actual; en POST vale true. */
    @JsonAlias({"status", "isActive"})
    Boolean activo
) {}
