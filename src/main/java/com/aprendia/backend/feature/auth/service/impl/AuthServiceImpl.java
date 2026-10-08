package com.aprendia.backend.feature.auth.service.impl;

import com.aprendia.backend.exception.BadRequestException;
import com.aprendia.backend.feature.auth.utils.JwtUtil;
import com.aprendia.backend.feature.auth.dto.AuthResponse;
import com.aprendia.backend.feature.auth.dto.AuthResponseDTO;
import com.aprendia.backend.feature.auth.dto.StudentInfoDTO;
import com.aprendia.backend.feature.auth.dto.StudentQrLoginDTO;
import com.aprendia.backend.feature.auth.dto.UserLoginDTO;
import com.aprendia.backend.feature.auth.service.AuthService;
import com.aprendia.backend.feature.user.entities.Person;
import com.aprendia.backend.feature.user.entities.User;
import com.aprendia.backend.feature.user.repository.StudentRepository;
import com.aprendia.backend.feature.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private StudentRepository studentRepository;

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDTO login(UserLoginDTO loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.username(),
                        loginRequest.password()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtUtil.generateToken(userDetails);

        return AuthResponseDTO.builder()
                .accessToken(jwt)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDTO loginStudent(StudentQrLoginDTO studentQrLoginDTO) {
        User user = userRepository.findByQrCode(studentQrLoginDTO.qrCode())
                .orElseThrow(() -> new BadRequestException("Código QR inválido o mal formado."));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getUsername());

        boolean isStudent = userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_STUDENT"));
        if (!isStudent) {
            throw new BadRequestException("Código QR inválido o mal formado.");
        }

        String jwt = jwtUtil.generateToken(userDetails);

        return AuthResponseDTO.builder()
                .accessToken(jwt)
                .studentInfo(buildStudentInfo(user))
                .build();
    }

    private StudentInfoDTO buildStudentInfo(User user) {
        Person person = user.getPerson();
        if (person == null) {
            return null;
        }

        Long studentId = studentRepository.findByPersonId(person.getId())
                .map(student -> student.getId())
                .orElse(null);

        String fullName = (person.getFirstName() + " " + person.getLastName()).trim();

        return StudentInfoDTO.builder()
                .studentId(studentId)
                .name(fullName)
                .build();
    }

    @Override
    public AuthResponse validateToken(String accessToken) {
        boolean isValid = false;
        try {
            String username = jwtUtil.getUsernameFromToken(accessToken);
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
            isValid = jwtUtil.validateToken(accessToken, userDetails);
        } catch (Exception e) {
            isValid = false;
        }
        return AuthResponse.builder()
                .success(isValid)
                .build();
    }
}
