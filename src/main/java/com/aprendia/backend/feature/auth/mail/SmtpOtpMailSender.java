package com.aprendia.backend.feature.auth.mail;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Canal SMTP (MAIL_PROVIDER=smtp, el valor por defecto). Algunas plataformas bloquean el SMTP saliente
 * (por ejemplo Railway fuera del plan Pro): en ese caso usar MAIL_PROVIDER=resend o brevo.
 */
@Component
@ConditionalOnProperty(name = "app.mail.provider", havingValue = "smtp", matchIfMissing = true)
public class SmtpOtpMailSender extends AbstractOtpMailSender {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${app.mail.from:no-reply@aprendia.local}")
    private String from;

    public SmtpOtpMailSender(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    @Override
    protected void deliver(String to, String subject, String textBody) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null || !StringUtils.hasText(mailHost)) {
            // Falla explícita: nunca fingir que se envió un correo
            throw new IllegalStateException("El envío de correo no está configurado (defina MAIL_HOST).");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(textBody);

        mailSender.send(message);
    }
}
