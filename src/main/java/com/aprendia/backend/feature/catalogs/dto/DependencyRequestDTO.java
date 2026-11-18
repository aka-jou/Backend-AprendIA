package com.aprendia.backend.feature.catalogs.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record DependencyRequestDTO(

    @NotBlank(message = "El nombre de la dependencia es obligatorio.")
    @Size(max = 100, message = "El nombre de la dependencia no puede superar los 100 caracteres.")
    String nombre,

    /** Opcional: clasificación de la dependencia (ej. "CAM", "USAER", "ONG"). */
    @Size(max = 50, message = "El tipo no puede superar los 50 caracteres.")
    String tipo,

    @NotBlank(message = "El código interno es obligatorio.")
    @Size(max = 50, message = "El código interno no puede superar los 50 caracteres.")
    String codigoInterno,

    /** Opcional (alias "activo"). Si se omite en PUT se conserva el actual; en POST vale true. */
    @JsonAlias("activo")
    Boolean status
) {}
