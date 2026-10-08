package com.aprendia.backend.feature.healthCheck.dto;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record HealthCheckResponse(
    String status,
    String databaseStatus,
    String message,
    LocalDateTime timestamp
) {}