package com.aprendia.backend.feature.auth.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record OtpUserInfoDTO(
    Long idUsuario,
    String nombre,
    String username,
    List<String> roles,
    Long idEntidad
) {}
