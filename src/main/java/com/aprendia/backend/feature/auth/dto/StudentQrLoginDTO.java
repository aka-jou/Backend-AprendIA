package com.aprendia.backend.feature.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record StudentQrLoginDTO(
    @NotBlank(message = "El código QR es obligatorio.")
    String qrCode
) {}
