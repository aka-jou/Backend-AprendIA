package com.aprendia.backend.feature.student.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

/** Cuerpo de POST /api/v1/students y PUT /api/v1/students/{id}. */
@Builder
public record StudentRequestDTO(
    @Valid @NotNull(message = "El objeto 'person' es obligatorio.")
    StudentPersonDTO person,

    @Valid @NotNull(message = "El objeto 'address' es obligatorio.")
    StudentAddressDTO address,

    @Valid @NotNull(message = "La lista 'relatives' es obligatoria (puede ser vacía).")
    @Size(max = 10, message = "Se permiten como máximo 10 familiares.")
    List<StudentRelativeDTO> relatives,

    @NotNull(message = "El perfil pedagógico (profile) es obligatorio.")
    @Positive(message = "profile debe ser un número positivo.")
    Long profile,

    /** Docente asignado. En el alta (POST) lo fija el docente autenticado: si viene, debe ser su propio id. En PUT: id de un usuario activo con rol DOCENTE_AULA. */
    @Positive(message = "teacherId debe ser un número positivo.")
    Long teacherId,

    @NotBlank(message = "El correo de acceso a la app móvil es obligatorio.")
    @Email(message = "El correo no tiene un formato válido.")
    @Size(max = 100, message = "El correo no puede superar los 100 caracteres.")
    String email,

    /** Solo en PUT: activa o desactiva la cuenta del alumno (alias isActive). Si se omite, no cambia. */
    @JsonAlias({"isActive", "status"})
    Boolean activo
) {}
