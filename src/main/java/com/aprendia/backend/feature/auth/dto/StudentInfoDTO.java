package com.aprendia.backend.feature.auth.dto;

import lombok.Builder;

@Builder
public record StudentInfoDTO(
    Long studentId,
    String name
) {}
