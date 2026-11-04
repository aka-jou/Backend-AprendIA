package com.aprendia.backend.feature.student.dto;

import com.aprendia.backend.common.validation.ValidationPatterns;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDate;

/**
 * Elemento de "relatives": tutor o familiar del alumno (entrada y salida).
 *
 * Vinculación (spec de búsqueda por CURP, sección 3):
 * - Con personId: se vincula esa persona tal cual; el resto de los datos de la persona se ignora.
 * - Sin personId y con una CURP ya registrada: se vincula la persona de esa CURP (no se duplica).
 * - Sin personId y sin CURP registrada: se crea la persona; firstName y lastName son obligatorios.
 * relationship es obligatorio siempre. Los opcionales enviados vacíos ("") se guardan como null.
 * {@code relativeId} y {@code name} son solo de salida.
 */
@Builder
public record StudentRelativeDTO(
    /** Solo salida: ID del vínculo (student_relatives.id), el {relativeId} de /students/{id}/relatives. */
    Long relativeId,

    @Positive(message = "personId debe ser un número positivo.")
    Long personId,

    @Pattern(regexp = "^$|" + ValidationPatterns.CURP,
             message = "La CURP del familiar debe tener exactamente 18 caracteres con formato válido.")
    String curp,

    @Size(max = 100, message = "El primer nombre del familiar no puede superar los 100 caracteres.")
    String firstName,

    @Size(max = 100, message = "El segundo nombre del familiar no puede superar los 100 caracteres.")
    String middleName,

    @Size(max = 100, message = "El primer apellido del familiar no puede superar los 100 caracteres.")
    String lastName,

    @Size(max = 100, message = "El segundo apellido del familiar no puede superar los 100 caracteres.")
    String secondLastName,

    /** Solo salida: nombre completo calculado. */
    String name,

    @Past(message = "La fecha de nacimiento del familiar debe ser una fecha pasada.")
    LocalDate birthDate,

    @Pattern(regexp = "^$|" + ValidationPatterns.GENDER_STUDENT,
             message = "El género del familiar debe ser 'M' (Masculino) o 'F' (Femenino).")
    String gender,

    @Pattern(regexp = "^$|" + ValidationPatterns.PHONE_10,
             message = "El teléfono del familiar debe tener 10 dígitos (se permiten espacios o guiones como separadores).")
    String phone,

    @Email(message = "El correo del familiar no tiene un formato válido.")
    @Size(max = 100, message = "El correo del familiar no puede superar los 100 caracteres.")
    String email,

    @NotBlank(message = "El parentesco es obligatorio (ej. Madre, Padre, Tutor Legal, Abuelo/a).")
    @Size(max = 50, message = "El parentesco no puede superar los 50 caracteres.")
    String relationship,

    /** Domicilio del familiar (opcional). Si se envía, se valida igual que el del alumno. */
    @Valid
    StudentAddressDTO address
) {}
