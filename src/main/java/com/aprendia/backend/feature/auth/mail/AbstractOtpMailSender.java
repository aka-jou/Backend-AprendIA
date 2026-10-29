package com.aprendia.backend.feature.auth.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * Parte común de todos los canales de entrega del OTP: modo de desarrollo (mock), asunto y texto del
 * mensaje. Cada canal (SMTP, Resend, Brevo) solo implementa {@link #deliver}.
 */
public abstract class AbstractOtpMailSender implements OtpMailSender {

    private static final Logger logger = LoggerFactory.getLogger(AbstractOtpMailSender.class);

    static final String SUBJECT = "Tu código de seguridad de AprendIA";

    /** Solo para desarrollo local sin proveedor de correo: escribe el código en el log en vez de enviarlo. */
    @Value("${app.otp.mock-delivery:false}")
    private boolean mockDelivery;

    @Override
    public final void sendOtp(String email, String code, int expiresInSeconds) {
        if (mockDelivery) {
            logger.warn("[OTP MOCK] Código para {}: {} (válido {} s). Desactivar app.otp.mock-delivery fuera de desarrollo.",
                    email, code, expiresInSeconds);
            return;
        }
        deliver(email, SUBJECT, buildBody(code, expiresInSeconds));
    }

    /** Entrega el mensaje. Debe lanzar una excepción si no se pudo enviar: nunca fingir un envío. */
    protected abstract void deliver(String to, String subject, String textBody);

    static String buildBody(String code, int expiresInSeconds) {
        return """
                Tu código de seguridad es: %s

                Es válido por %d minutos y solo puede usarse una vez.
                Si no intentaste iniciar sesión, ignora este mensaje.
                """.formatted(code, Math.max(1, expiresInSeconds / 60));
    }
}
