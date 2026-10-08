package com.aprendia.backend.feature.student.dto;

import com.aprendia.backend.common.validation.ValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/** Objeto "address" (domicilio normalizado) del registro de estudiante. */
@Builder
public record StudentAddressDTO(
    @NotBlank(message = "La calle del domicilio es obligatoria.")
    @Size(max = 150, message = "La calle no puede superar los 150 caracteres.")
    String street,

    @NotBlank(message = "El número exterior es obligatorio.")
    @Size(max = 20, message = "El número exterior no puede superar los 20 caracteres.")
    String exteriorNumber,

    @Size(max = 100, message = "El tipo de asentamiento no puede superar los 100 caracteres.")
    String settlementType,

    @NotBlank(message = "La colonia, barrio o fraccionamiento es obligatorio.")
    @Size(max = 100, message = "El asentamiento no puede superar los 100 caracteres.")
    String settlement,

    @NotNull(message = "El municipio (municipalityId) es obligatorio.")
    @Positive(message = "municipalityId debe ser un número positivo.")
    Long municipalityId,

    @NotNull(message = "El estado (stateId) es obligatorio.")
    @Positive(message = "stateId debe ser un número positivo.")
    Long stateId,

    @NotBlank(message = "El código postal es obligatorio.")
    @Pattern(regexp = ValidationPatterns.ZIP_CODE, message = "El código postal debe tener exactamente 5 dígitos.")
    String zipCode
) {}
