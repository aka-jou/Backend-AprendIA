-- La columna api_keys.key_hash guardaba la API key en claro (V11/V12). Se reemplaza cada valor por su
-- SHA-256 en hexadecimal; ApiKeyFilter calcula el mismo hash sobre la cabecera X-API-KEY.
-- Idempotente: no toca valores que ya son un hash hexadecimal de 64 caracteres.
UPDATE api_keys
SET key_hash = encode(sha256(convert_to(key_hash, 'UTF8')), 'hex')
WHERE key_hash !~ '^[0-9a-f]{64}$';
