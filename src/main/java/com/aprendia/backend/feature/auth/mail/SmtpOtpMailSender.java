package com.aprendia.backend.feature.auth.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SmtpOtpMailSender implements OtpMailSender {

    private static final Logger logger = LoggerFactory.getLogger(SmtpOtpMailSender.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${app.mail.from:no-reply@aprendia.local}")
    private String from;

    /** Solo para desarrollo local sin SMTP: escribe el código en el log en vez de enviarlo. */
    @Value("${app.otp.mock-delivery:false}")
    private boolean mockDelivery;

    public SmtpOtpMailSender(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    @Override
    public void sendOtp(String email, String code, int expiresInSeconds) {
        if (mockDelivery) {
            logger.warn("[OTP MOCK] Código para {}: {} (válido {} s). Desactivar app.otp.mock-delivery fuera de desarrollo.",
                    email, code, expiresInSeconds);
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null || !StringUtils.hasText(mailHost)) {
            // Falla explícita: nunca fingir que se envió un correo
            throw new IllegalStateException("El envío de correo no está configurado (defina MAIL_HOST).");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("Tu código de seguridad de AprendIA");
        message.setText("""
                Tu código de seguridad es: %s

                Es válido por %d minutos y solo puede usarse una vez.
                Si no intentaste iniciar sesión, ignora este mensaje.
                """.formatted(code, Math.max(1, expiresInSeconds / 60)));

        mailSender.send(message);
    }
}
