package com.aprendia.backend.feature.user.dto;

import lombok.Builder;
import java.util.List;

@Builder
public record UserResponse(
    Long id,
    String username,
    String email,
    boolean isActive,
    List<String> roles
) {}
