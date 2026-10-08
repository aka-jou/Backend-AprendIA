package com.aprendia.backend.feature.healthCheck.controller;

import com.aprendia.backend.feature.healthCheck.dto.HealthCheckResponse;
import com.aprendia.backend.feature.healthCheck.service.HealthCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/health")
@RequiredArgsConstructor
@Tag(name = "Health Check", description = "Monitoreo de Salud del Sistema")
public class HealthCheckController {

    private final HealthCheckService healthCheckService;

    @GetMapping
    @Operation(summary = "Revisar salud del sistema", description = "Retorna el estado de la API y la conexión con la BD")
    public ResponseEntity<HealthCheckResponse> checkHealth() {
        HealthCheckResponse response = healthCheckService.checkHealth();
        
        if ("DOWN".equals(response.databaseStatus())) {
            return ResponseEntity.status(503).body(response);
        }
        
        return ResponseEntity.ok(response);
    }
}
