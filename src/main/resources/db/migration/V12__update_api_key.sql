-- Actualizar la API Key de prueba por una llave segura para ambiente de desarrollo/producción
UPDATE api_keys 
SET key_hash = 'ak_live_a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d'
WHERE key_hash = 'aprendia-dev-api-key-2026';
