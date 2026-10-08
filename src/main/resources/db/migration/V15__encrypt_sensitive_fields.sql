-- Ensanchar columnas que ahora guardan valores cifrados (Base64(IV||ciphertext||tag)),
-- que ocupan más espacio que el texto plano original. El UNIQUE de curp se conserva.
ALTER TABLE persons ALTER COLUMN curp TYPE VARCHAR(255);
ALTER TABLE persons ALTER COLUMN phone TYPE VARCHAR(255);

ALTER TABLE addresses ALTER COLUMN street TYPE VARCHAR(500);
ALTER TABLE addresses ALTER COLUMN exterior_number TYPE VARCHAR(255);
ALTER TABLE addresses ALTER COLUMN settlement TYPE VARCHAR(255);
ALTER TABLE addresses ALTER COLUMN zip_code TYPE VARCHAR(255);
