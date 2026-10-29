package com.aprendia.backend.feature.auth.mail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Base de los canales que envían el correo por una API HTTPS (Resend, Brevo). Funciona en cualquier plan de
 * Railway porque no usa los puertos SMTP. Usa solo el cliente HTTP del JDK: no agrega dependencias.
 */
abstract class AbstractHttpOtpMailSender extends AbstractOtpMailSender {

    private static final Logger logger = LoggerFactory.getLogger(AbstractHttpOtpMailSender.class);

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final int MAX_LOGGED_BODY = 300;

    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.mail.api-key:}")
    private String apiKey;

    @Value("${app.mail.from:no-reply@aprendia.local}")
    private String from;

    @Value("${app.mail.from-name:AprendIA}")
    private String fromName;

    /** Nombre del proveedor, solo para los mensajes de error. */
    protected abstract String providerName();

    protected String apiKey() {
        if (!StringUtils.hasText(apiKey)) {
            // Falla explícita: nunca fingir que se envió un correo
            throw new IllegalStateException("El envío de correo no está configurado (defina MAIL_API_KEY para "
                    + providerName() + ").");
        }
        return apiKey.trim();
    }

    protected String fromAddress() {
        return from.trim();
    }

    protected String fromName() {
        return fromName.trim();
    }

    /** Envía el JSON por POST y lanza una excepción si la API no responde con 2xx. */
    protected void postJson(String url, Map<String, String> headers, Object payload) {
        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo construir la petición al servicio de correo.", e);
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                .timeout(REQUEST_TIMEOUT)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("User-Agent", "AprendIA-Backend");
        headers.forEach(builder::header);

        HttpResponse<String> response;
        try {
            response = httpClient.send(builder.POST(HttpRequest.BodyPublishers.ofString(json)).build(),
                    HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo contactar al servicio de correo " + providerName() + ".", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Se interrumpió el envío al servicio de correo " + providerName() + ".", e);
        }

        int status = response.statusCode();
        if (status < 200 || status >= 300) {
            // El cuerpo explica el motivo (remitente no verificado, clave inválida...). Nunca incluye secretos.
            logger.error("El servicio de correo {} rechazó el envío (HTTP {}): {}", providerName(), status,
                    truncate(response.body()));
            throw new IllegalStateException("El servicio de correo " + providerName()
                    + " rechazó el envío (HTTP " + status + ").");
        }
    }

    private static String truncate(String body) {
        if (body == null) {
            return "";
        }
        return body.length() <= MAX_LOGGED_BODY ? body : body.substring(0, MAX_LOGGED_BODY) + "...";
    }
}
