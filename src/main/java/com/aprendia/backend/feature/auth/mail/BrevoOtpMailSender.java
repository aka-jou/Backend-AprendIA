package com.aprendia.backend.feature.auth.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Canal Brevo (MAIL_PROVIDER=brevo). Requiere MAIL_API_KEY (clave de la API v3, no la contraseña SMTP) y que
 * MAIL_FROM sea un remitente validado en la cuenta de Brevo.
 */
@Component
@ConditionalOnProperty(name = "app.mail.provider", havingValue = "brevo")
public class BrevoOtpMailSender extends AbstractHttpOtpMailSender {

    @Value("${app.mail.brevo-url:https://api.brevo.com/v3/smtp/email}")
    private String url;

    @Override
    protected String providerName() {
        return "Brevo";
    }

    @Override
    protected void deliver(String to, String subject, String textBody) {
        String key = apiKey();
        postJson(url,
                Map.of("api-key", key),
                Map.of("sender", Map.of("name", fromName(), "email", fromAddress()),
                        "to", List.of(Map.of("email", to)),
                        "subject", subject,
                        "textContent", textBody));
    }
}
