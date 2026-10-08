-- Código QR para el login de estudiantes (única por usuario, opcional para cuentas que no son estudiantes)
ALTER TABLE users ADD COLUMN qr_code VARCHAR(100) UNIQUE;
