package com.aprendia.backend.security.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

/**
 * Cifra en reposo campos sensibles (CURP, teléfono, dirección) con AES-256-GCM,
 * usando un IV determinístico: IV = HMAC-SHA256(macKey, texto_plano), truncado a 12 bytes.
 *
 * Esto hace que el mismo valor de entrada produzca siempre el mismo texto cifrado
 * (necesario para poder seguir usando UNIQUE y búsquedas por igualdad, p. ej. en
 * Person.curp), a costa de revelar qué filas comparten el mismo valor original —
 * no el valor en sí. Es el trade-off estándar aceptado para poder buscar/indexar
 * sobre una columna cifrada.
 *
 * encKey y macKey se derivan por separado del mismo secreto configurado, para no
 * reutilizar la misma clave en dos primitivas criptográficas distintas.
 */
@Component
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private static final String AES_GCM = "AES/GCM/NoPadding";
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_BYTES = 12;

    private final SecretKeySpec encKey;
    private final SecretKeySpec macKey;

    public EncryptedStringConverter(@Value("${app.encryption.secret}") String secret) {
        this.encKey = deriveKey(secret, "enc");
        this.macKey = deriveKey(secret, "mac");
    }

    private static SecretKeySpec deriveKey(String secret, String purpose) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            sha256.update(secret.getBytes(StandardCharsets.UTF_8));
            sha256.update(purpose.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(sha256.digest(), "AES");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se pudo derivar la clave de cifrado", e);
        }
    }

    @Override
    public String convertToDatabaseColumn(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        try {
            byte[] plainBytes = plaintext.getBytes(StandardCharsets.UTF_8);
            byte[] iv = deterministicIv(plainBytes);

            Cipher cipher = Cipher.getInstance(AES_GCM);
            cipher.init(Cipher.ENCRYPT_MODE, encKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plainBytes);

            byte[] out = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(ciphertext, 0, out, iv.length, ciphertext.length);
            return Base64.getEncoder().encodeToString(out);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Error al cifrar el campo", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String stored) {
        if (stored == null) {
            return null;
        }
        try {
            byte[] raw = Base64.getDecoder().decode(stored);
            byte[] iv = Arrays.copyOfRange(raw, 0, IV_BYTES);
            byte[] ciphertext = Arrays.copyOfRange(raw, IV_BYTES, raw.length);

            Cipher cipher = Cipher.getInstance(AES_GCM);
            cipher.init(Cipher.DECRYPT_MODE, encKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Error al descifrar el campo", e);
        }
    }

    private byte[] deterministicIv(byte[] plainBytes) throws GeneralSecurityException {
        Mac mac = Mac.getInstance(HMAC_SHA256);
        mac.init(macKey);
        byte[] full = mac.doFinal(plainBytes);
        return Arrays.copyOf(full, IV_BYTES);
    }
}
