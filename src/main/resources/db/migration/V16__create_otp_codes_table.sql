-- Códigos OTP de un solo uso para el login passwordless (Especificación v2.0, Módulo 1).
-- Se guarda el HASH del código (BCrypt), nunca el código en claro.
CREATE TABLE otp_codes (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    code_hash VARCHAR(100) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Consulta frecuente: último código emitido para un correo (cooldown y verificación)
CREATE INDEX idx_otp_codes_email_created ON otp_codes (email, created_at DESC);
