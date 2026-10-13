package com.aprendia.backend.feature.student.dto;

import com.aprendia.backend.common.validation.ValidationPatterns;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDate;

/**
 * Elemento de "relatives": tutor o familiar del alumno (entrada y salida).
 *
 * Obligatorios: firstName, lastName y relationship. El resto es opcional; si se envía vacío ("")
 * se guarda como null. {@code name} se ignora en la entrada: el servidor lo calcula a partir de las
 * partes del nombre y lo devuelve en la salida.
 */
@Builder
public record StudentRelativeDTO(
    @Pattern(regexp = "^$|" + ValidationPatterns.CURP,
             message = "La CURP del familiar debe tener exactamente 18 caracteres con formato válido.")
    String curp,

    @NotBlank(message = "El primer nombre del familiar es obligatorio.")
    @Size(max = 100, message = "El primer nombre del familiar no puede superar los 100 caracteres.")
    String firstName,

    @Size(max = 100, message = "El segundo nombre del familiar no puede superar los 100 caracteres.")
    String middleName,

    @NotBlank(message = "El primer apellido del familiar es obligatorio.")
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
