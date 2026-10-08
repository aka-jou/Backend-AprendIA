package com.aprendia.backend.feature.auth.service;

import com.aprendia.backend.feature.auth.dto.OtpSendResponseDTO;
import com.aprendia.backend.feature.auth.dto.OtpVerifyResponseDTO;

public interface OtpService {
    OtpSendResponseDTO sendOtp(String email);
    OtpVerifyResponseDTO verifyOtp(String email, String code);
}
