package com.aprendia.backend.feature.student.dto;

import com.aprendia.backend.common.validation.ValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

/** Objeto "person" del registro de estudiante (Especificación v2.0, tabla 6.2). */
@Builder
public record StudentPersonDTO(
    @NotBlank(message = "El primer nombre del alumno es obligatorio.")
    @Size(max = 100, message = "El primer nombre no puede superar los 100 caracteres.")
    String firstName,

    @Size(max = 100, message = "El segundo nombre no puede superar los 100 caracteres.")
    String middleName,

    @NotBlank(message = "El primer apellido es obligatorio.")
    @Size(max = 100, message = "El primer apellido no puede superar los 100 caracteres.")
    String lastName,

    @Size(max = 100, message = "El segundo apellido no puede superar los 100 caracteres.")
    String secondLastName,

    @NotBlank(message = "La CURP es obligatoria.")
    @Pattern(regexp = ValidationPatterns.CURP, message = "La CURP debe tener exactamente 18 caracteres con formato válido.")
    String curp,

    @NotNull(message = "La fecha de nacimiento es obligatoria (formato YYYY-MM-DD).")
    @Past(message = "La fecha de nacimiento debe ser una fecha pasada.")
    LocalDate birthDate,

    @NotBlank(message = "El género es obligatorio.")
    @Pattern(regexp = ValidationPatterns.GENDER_STUDENT, message = "El género debe ser 'M' (Masculino) o 'F' (Femenino).")
    String gender,

    @Pattern(regexp = ValidationPatterns.PHONE_10, message = "El teléfono debe tener exactamente 10 dígitos.")
    String phone,

    @URL(message = "imageUrl debe ser una URL válida.")
    @Size(max = 255, message = "imageUrl no puede superar los 255 caracteres.")
    String imageUrl
) {}
