package com.aprendia.backend.feature.user.dto;

import com.aprendia.backend.common.validation.ValidationPatterns;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDate;

/**
 * Sección "persona" de PUT /api/v1/users/{id}. Todos los campos son opcionales: el que se omite (null)
 * conserva su valor actual. secondName y secondSurname vacíos ("") se borran.
 *
 * Acepta los nombres de la spec (firstName, firstSurname...) y los del directorio del frontend
 * (primerNombre, primerApellido, telefono, genero, fechaNacimiento...).
 */
@Builder
public record UserUpdatePersonaDTO(
    @Pattern(regexp = ValidationPatterns.CURP, message = "La CURP debe tener exactamente 18 caracteres con formato válido.")
    String curp,

    @JsonAlias("primerNombre")
    @Size(max = 100, message = "El primer nombre no puede superar los 100 caracteres.")
    String firstName,

    @JsonAlias("segundoNombre")
    @Size(max = 100, message = "El segundo nombre no puede superar los 100 caracteres.")
    String secondName,

    @JsonAlias("primerApellido")
    @Size(max = 100, message = "El primer apellido no puede superar los 100 caracteres.")
    String firstSurname,

    @JsonAlias("segundoApellido")
    @Size(max = 100, message = "El segundo apellido no puede superar los 100 caracteres.")
    String secondSurname,

    @JsonAlias("fechaNacimiento")
    @Past(message = "La fecha de nacimiento debe ser una fecha pasada.")
    LocalDate birthDate,

    @JsonAlias("genero")
    @Pattern(regexp = ValidationPatterns.GENDER_STAFF, message = "El género debe ser 'H' (Hombre), 'M' (Mujer) o 'X'.")
    String gender,

    @Email(message = "El correo institucional no tiene un formato válido.")
    @Size(max = 100, message = "El correo no puede superar los 100 caracteres.")
    String email,

    @JsonAlias("telefono")
    @Pattern(regexp = ValidationPatterns.PHONE_10, message = "El teléfono debe tener 10 dígitos (se permiten espacios o guiones como separadores).")
    String phone
) {}
