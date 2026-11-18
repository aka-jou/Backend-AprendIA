package com.aprendia.backend.feature.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

/**
 * Respuesta de POST /auth/admin y POST /auth/student.
 * - /auth/admin: accessToken, idUsuario y userInfo (mismos datos que /auth/otp/verify), para que el
 *   frontend no tenga que consultar después quién es el docente que inició sesión.
 * - /auth/student: accessToken y studentInfo.
 * Los campos que no aplican se omiten.
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponseDTO(
    String accessToken,
    Long idUsuario,
    OtpUserInfoDTO userInfo,
    StudentInfoDTO studentInfo
) {}
