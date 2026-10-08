package com.aprendia.backend.feature.auth.service.impl;

import com.aprendia.backend.exception.TooManyRequestsException;
import com.aprendia.backend.exception.UnauthorizedException;
import com.aprendia.backend.feature.auth.dto.OtpSendResponseDTO;
import com.aprendia.backend.feature.auth.dto.OtpUserInfoDTO;
import com.aprendia.backend.feature.auth.dto.OtpVerifyResponseDTO;
import com.aprendia.backend.feature.auth.entities.OtpCode;
import com.aprendia.backend.feature.auth.mail.OtpMailSender;
import com.aprendia.backend.feature.auth.repository.OtpCodeRepository;
import com.aprendia.backend.feature.auth.service.OtpService;
import com.aprendia.backend.feature.auth.utils.JwtUtil;
import com.aprendia.backend.feature.user.entities.Person;
import com.aprendia.backend.feature.user.entities.User;
import com.aprendia.backend.feature.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/**
 * Login passwordless de 2 pasos (Especificación v2.0, Módulo 1):
 * 1) sendOtp   -> genera un código de 6 dígitos y lo envía al correo.
 * 2) verifyOtp -> valida el código y emite el JWT de siempre.
 *
 * Reglas de seguridad:
 *  - El código se guarda hasheado (BCrypt), nunca en claro.
 *  - Un solo uso, vigencia limitada, máximo de intentos por código.
 *  - Cooldown entre envíos al mismo correo.
 *  - Mismo mensaje exista o no el correo (no revela qué correos están registrados).
 */
@Service
public class OtpServiceImpl implements OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpServiceImpl.class);
    private static final String INVALID_CODE_MESSAGE = "El código es inválido o ha expirado.";

    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    private OtpCodeRepository otpCodeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private OtpMailSender otpMailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${app.otp.expiration-seconds:300}")
    private int expirationSeconds;

    @Value("${app.otp.cooldown-seconds:45}")
    private int cooldownSeconds;

    @Value("${app.otp.max-attempts:5}")
    private int maxAttempts;

    @Override
    @Transactional
    public OtpSendResponseDTO sendOtp(String rawEmail) {
        String email = normalize(rawEmail);
        OtpSendResponseDTO response = OtpSendResponseDTO.builder()
                .cooldownSeconds(cooldownSeconds)
                .expiresInSeconds(expirationSeconds)
                .build();

        boolean registered = userRepository.findByEmailIgnoreCase(email).filter(User::isActive).isPresent();

        // El cooldown y el guardado se ejecutan IGUAL exista o no el correo: así un atacante no puede
        // distinguir correos registrados por la respuesta (429 vs 200). Si el correo no existe se guarda
        // un registro "señuelo" ya marcado como usado, que nunca se envía ni valida.
        enforceCooldown(email);

        String code = generateCode();
        otpCodeRepository.deleteByEmail(email);
        otpCodeRepository.save(OtpCode.builder()
                .email(email)
                .codeHash(passwordEncoder.encode(code))
                .expiresAt(LocalDateTime.now().plusSeconds(expirationSeconds))
                .createdAt(LocalDateTime.now())
                .used(!registered)
                .build());

        if (registered) {
            // Si el correo falla, la excepción revierte la transacción y el código no queda guardado
            otpMailSender.sendOtp(email, code, expirationSeconds);
        } else {
            logger.warn("Solicitud de OTP para un correo no registrado o inactivo.");
        }
        return response;
    }

    // noRollbackFor: el contador de intentos fallidos DEBE persistir aunque se lance la excepción
    @Override
    @Transactional(noRollbackFor = UnauthorizedException.class)
    public OtpVerifyResponseDTO verifyOtp(String rawEmail, String code) {
        String email = normalize(rawEmail);

        OtpCode otp = otpCodeRepository.findFirstByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new UnauthorizedException(INVALID_CODE_MESSAGE));

        if (otp.isUsed() || otp.getExpiresAt().isBefore(LocalDateTime.now()) || otp.getAttempts() >= maxAttempts) {
            throw new UnauthorizedException(INVALID_CODE_MESSAGE);
        }

        if (!passwordEncoder.matches(code, otp.getCodeHash())) {
            otp.setAttempts(otp.getAttempts() + 1);
            otpCodeRepository.save(otp);
            throw new UnauthorizedException(INVALID_CODE_MESSAGE);
        }

        otp.setUsed(true);
        otpCodeRepository.save(otp);

        User user = userRepository.findByEmailIgnoreCase(email)
                .filter(User::isActive)
                .orElseThrow(() -> new UnauthorizedException(INVALID_CODE_MESSAGE));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtUtil.generateToken(userDetails);

        return OtpVerifyResponseDTO.builder()
                .token(token)
                .iat(jwtUtil.getClaimFromToken(token, Claims::getIssuedAt).getTime() / 1000)
                .exp(jwtUtil.getClaimFromToken(token, Claims::getExpiration).getTime() / 1000)
                .userInfo(buildUserInfo(user))
                .build();
    }

    private void enforceCooldown(String email) {
        otpCodeRepository.findFirstByEmailOrderByCreatedAtDesc(email).ifPresent(last -> {
            long elapsed = Duration.between(last.getCreatedAt(), LocalDateTime.now()).getSeconds();
            long remaining = cooldownSeconds - elapsed;
            if (remaining > 0) {
                throw new TooManyRequestsException(
                        "Espere " + remaining + " segundos antes de solicitar un nuevo código.");
            }
        });
    }

    private OtpUserInfoDTO buildUserInfo(User user) {
        Person person = user.getPerson();
        String nombre = user.getUsername();
        if (person != null) {
            nombre = String.join(" ",
                    safe(person.getFirstName()), safe(person.getMiddleName()),
                    safe(person.getLastName()), safe(person.getSecondLastName())).replaceAll("\\s+", " ").trim();
        }

        List<String> roles = user.getRoles().stream().map(role -> role.getName()).toList();

        return OtpUserInfoDTO.builder()
                .idUsuario(user.getId())
                .nombre(nombre)
                .username(user.getUsername())
                .roles(roles)
                // TODO: el modelo actual no vincula usuario con dependencia/entidad (pendiente del DDL de la spec)
                .idEntidad(null)
                .build();
    }

    private String generateCode() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
