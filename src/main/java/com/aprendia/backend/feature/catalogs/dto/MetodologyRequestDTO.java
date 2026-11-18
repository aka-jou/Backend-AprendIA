package com.aprendia.backend.feature.catalogs.dto;

import com.aprendia.backend.common.validation.ValidationPatterns;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Cuerpo de POST y PUT /catalogs/metodologies. sigla, descripcion y urlImagen son opcionales: si se
 * omiten en PUT se conserva el valor actual (una cadena vacía lo borra). status también es opcional
 * (alias "activo"): en POST vale true y en PUT, si se omite, se conserva.
 */
@Builder
public record MetodologyRequestDTO(
    @NotBlank(message = "El nombre de la metodología es obligatorio.")
    @Size(max = 100, message = "El nombre de la metodología no puede superar los 100 caracteres.")
    String nombre,

    @Size(max = 10, message = "La sigla no puede superar los 10 caracteres.")
    String sigla,

    @Size(max = 150, message = "El autor no puede superar los 150 caracteres.")
    String autor,

    @Size(max = 5000, message = "La descripción no puede superar los 5000 caracteres.")
    String descripcion,

    @Size(max = 1000, message = "urlImagen no puede superar los 1000 caracteres.")
    @Pattern(regexp = ValidationPatterns.HTTP_URL, message = "urlImagen debe ser una URL http(s) válida.")
    String urlImagen,

    @JsonAlias("activo")
    Boolean status
) {}
