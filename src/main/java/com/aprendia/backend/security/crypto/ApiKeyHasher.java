package com.aprendia.backend.security.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Hash de API keys: SHA-256 en hexadecimal (64 caracteres), equivalente a
 * encode(sha256(convert_to(key, 'UTF8')), 'hex') en PostgreSQL (migración V21).
 * Una API key es un secreto aleatorio de alta entropía, por eso basta un hash rápido sin sal.
 */
public final class ApiKeyHasher {

    private ApiKeyHasher() {
    }

    public static String sha256Hex(String apiKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(apiKey.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible en la JVM", e);
        }
    }
}
