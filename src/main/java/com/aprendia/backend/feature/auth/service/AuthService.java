package com.aprendia.backend.feature.auth.service;

import com.aprendia.backend.feature.auth.dto.AuthResponse;
import com.aprendia.backend.feature.auth.dto.AuthResponseDTO;
import com.aprendia.backend.feature.auth.dto.StudentQrLoginDTO;
import com.aprendia.backend.feature.auth.dto.UserLoginDTO;

public interface AuthService {
    AuthResponseDTO login(UserLoginDTO loginRequest);
    AuthResponseDTO loginStudent(StudentQrLoginDTO studentQrLoginDTO);
    AuthResponse validateToken(String accessToken);
}
