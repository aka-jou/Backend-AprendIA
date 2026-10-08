package com.aprendia.backend.feature.user.dto;

import lombok.Builder;

@Builder
public record StudentResponse(
    Long id,
    String qrCode,
    String createdAt,
    String createdBy
) {}
