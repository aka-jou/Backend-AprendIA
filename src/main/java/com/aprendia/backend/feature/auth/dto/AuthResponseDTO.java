package com.aprendia.backend.feature.auth.dto;

import lombok.Builder;

@Builder
public record AuthResponseDTO(
    String accessToken,
    StudentInfoDTO studentInfo
) {}
