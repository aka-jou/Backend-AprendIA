package com.aprendia.backend.feature.healthCheck.service.impl;

import com.aprendia.backend.feature.healthCheck.dto.HealthCheckResponse;
import com.aprendia.backend.feature.user.repository.UserRepository;
import com.aprendia.backend.feature.healthCheck.service.HealthCheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthCheckServiceImpl implements HealthCheckService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public HealthCheckResponse checkHealth() {
        String dbStatus = "DOWN";
        String message = "Service is up but Database connection failed";
        
        try {
            userRepository.count();
            dbStatus = "UP";
            message = "Service and Database are running normally";
        } catch (Exception e) {
            log.error("Error connecting to database during health check", e);
        }

        return HealthCheckResponse.builder()
                .status("UP")
                .databaseStatus(dbStatus)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
