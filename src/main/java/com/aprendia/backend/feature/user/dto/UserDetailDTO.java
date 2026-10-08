package com.aprendia.backend.feature.user.dto;

import lombok.Builder;

import java.util.List;

/**
 * Expediente completo del usuario (GET/POST/PUT). Las secciones persona y assignment usan los
 * mismos nombres que el registro para que el frontend pueda precargar el formulario de edición.
 */
@Builder
public record UserDetailDTO(
    Long id,
    String username,
    String nombreCompleto,
    String email,
    String status,
    List<String> roles,
    Persona persona,
    Assignment assignment,
    String createdAt,
    String updatedAt
) {
    @Builder
    public record Persona(
        String curp,
        String firstName,
        String secondName,
        String firstSurname,
        String secondSurname,
        String birthDate,
        String gender,
        String email,
        String phone
    ) {}

    @Builder
    public record Assignment(
        List<String> roles,
        String dependency,
        Long idDependencia,
        String ineNumber,
        String photoUrl
    ) {}
}
