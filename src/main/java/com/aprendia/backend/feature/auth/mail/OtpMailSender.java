package com.aprendia.backend.feature.auth.mail;

/** Entrega el código OTP al usuario. Interfaz aparte para poder cambiar el canal (SMTP, API, etc.). */
public interface OtpMailSender {
    void sendOtp(String email, String code, int expiresInSeconds);
}
