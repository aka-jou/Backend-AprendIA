package com.aprendia.backend.feature.user.dto;

import com.aprendia.backend.common.validation.ValidationPatterns;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDate;

/** Sección "persona" del registro de usuario (Especificación v2.0, tabla 4.2). */
@Builder
public record UserPersonaDTO(
    @NotBlank(message = "La CURP es obligatoria.")
    @Pattern(regexp = ValidationPatterns.CURP, message = "La CURP debe tener exactamente 18 caracteres con formato válido.")
    String curp,

    @NotBlank(message = "El primer nombre es obligatorio.")
    @Size(max = 100, message = "El primer nombre no puede superar los 100 caracteres.")
    String firstName,

    @Size(max = 100, message = "El segundo nombre no puede superar los 100 caracteres.")
    String secondName,

    @NotBlank(message = "El primer apellido es obligatorio.")
    @Size(max = 100, message = "El primer apellido no puede superar los 100 caracteres.")
    String firstSurname,

    @Size(max = 100, message = "El segundo apellido no puede superar los 100 caracteres.")
    String secondSurname,

    @NotNull(message = "La fecha de nacimiento es obligatoria (formato YYYY-MM-DD).")
    @Past(message = "La fecha de nacimiento debe ser una fecha pasada.")
    LocalDate birthDate,

    @NotBlank(message = "El género es obligatorio.")
    @Pattern(regexp = ValidationPatterns.GENDER_STAFF, message = "El género debe ser 'H' (Hombre), 'M' (Mujer) o 'X'.")
    String gender,

    @NotBlank(message = "El correo institucional es obligatorio.")
    @Email(message = "El correo institucional no tiene un formato válido.")
    @Size(max = 100, message = "El correo no puede superar los 100 caracteres.")
    String email,

    @NotBlank(message = "El teléfono es obligatorio.")
    @Pattern(regexp = ValidationPatterns.PHONE_10, message = "El teléfono debe tener exactamente 10 dígitos.")
    String phone
) {}
