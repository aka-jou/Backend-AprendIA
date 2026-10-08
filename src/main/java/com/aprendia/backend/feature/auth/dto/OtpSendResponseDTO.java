package com.aprendia.backend.feature.auth.dto;

import lombok.Builder;

@Builder
public record OtpSendResponseDTO(
    int cooldownSeconds,
    int expiresInSeconds
) {}
