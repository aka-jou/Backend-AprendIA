package com.aprendia.backend.feature.auth.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Canal Resend (MAIL_PROVIDER=resend). Requiere MAIL_API_KEY y un MAIL_FROM de un dominio verificado en
 * Resend. Con el remitente de prueba (onboarding@resend.dev) solo puede enviar al correo de la cuenta Resend.
 */
@Component
@ConditionalOnProperty(name = "app.mail.provider", havingValue = "resend")
public class ResendOtpMailSender extends AbstractHttpOtpMailSender {

    @Value("${app.mail.resend-url:https://api.resend.com/emails}")
    private String url;

    @Override
    protected String providerName() {
        return "Resend";
    }

    @Override
    protected void deliver(String to, String subject, String textBody) {
        String key = apiKey();
        String sender = fromAddress().contains("<") ? fromAddress() : fromName() + " <" + fromAddress() + ">";
        postJson(url,
                Map.of("Authorization", "Bearer " + key),
                Map.of("from", sender, "to", List.of(to), "subject", subject, "text", textBody));
    }
}
