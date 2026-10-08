package com.aprendia.backend.feature.auth.dto;

import lombok.Builder;

@Builder
public record OtpVerifyResponseDTO(
    String token,
    long iat,
    long exp,
    OtpUserInfoDTO userInfo
) {}
