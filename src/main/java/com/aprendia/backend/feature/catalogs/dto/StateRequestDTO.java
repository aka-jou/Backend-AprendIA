package com.aprendia.backend.feature.catalogs.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record StateRequestDTO(

    /** Acepta también "name". */
    @JsonAlias("name")
    @NotBlank(message = "El nombre del estado es obligatorio.")
    String nombre,

    /** Opcional (alias "status"). Si se omite en PUT se conserva el actual; en POST vale true. */
    @JsonAlias({"status", "isActive"})
    Boolean activo
) {}
