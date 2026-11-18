package com.aprendia.backend.feature.user.dto;

import com.aprendia.backend.common.validation.ValidationPatterns;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

/**
 * PUT /api/v1/users/{id}: actualización parcial. Lo que no se envía conserva su valor actual.
 *
 * Admite dos formas de cuerpo, que también se pueden mezclar:
 * - La del registro: { persona, credentials, assignment: { roles, dependency, photoUrl }, status: "Activo" }.
 * - La del directorio del frontend: { persona: { primerNombre, ... }, email, roles[], idDependencia
 *   (o entidad), activo }.
 * Si un dato viene en las dos formas, gana la segunda (email, roles, idDependencia/entidad y activo).
 */
@Builder
public record UserUpdateRequestDTO(
    @Valid
    UserUpdatePersonaDTO persona,

    @Valid
    UserCredentialsUpdateDTO credentials,

    @Valid
    UserAssignmentDTO assignment,

    @Pattern(regexp = ValidationPatterns.USER_STATUS, message = "status debe ser 'Activo' o 'Inactivo'.")
    String status,

    /** Correo de acceso. Equivale a persona.email. */
    @Email(message = "El correo no tiene un formato válido.")
    @Size(max = 100, message = "El correo no puede superar los 100 caracteres.")
    String email,

    /** Equivale a assignment.roles. Si viene, no puede estar vacío. */
    @Size(min = 1, message = "Debe asignar al menos un rol (ej. ['DOCENTE_AULA']).")
    List<@NotBlank(message = "Los roles no pueden estar vacíos.") String> roles,

    /** Dependencia por ID (alias idEntidad). Equivale a assignment.dependency, que va por nombre. */
    @JsonAlias("idEntidad")
    @Positive(message = "idDependencia debe ser un número positivo.")
    Long idDependencia,

    /** Dependencia por ID (texto numérico) o por nombre. */
    @Size(max = 100, message = "entidad no puede superar los 100 caracteres.")
    String entidad,

    /** Activar o desactivar. Equivale a status "Activo" / "Inactivo". */
    @JsonAlias("isActive")
    Boolean activo
) {}
