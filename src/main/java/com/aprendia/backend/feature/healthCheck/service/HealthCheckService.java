package com.aprendia.backend.feature.healthCheck.service;

import com.aprendia.backend.feature.healthCheck.dto.HealthCheckResponse;

public interface HealthCheckService {
    HealthCheckResponse checkHealth();
}
