package com.aprendia.backend.feature.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record RelativeRequest(
    @NotBlank(message = "Relative name is mandatory")
    String name,

    @NotBlank(message = "Relationship is mandatory")
    String relationship
) {}
