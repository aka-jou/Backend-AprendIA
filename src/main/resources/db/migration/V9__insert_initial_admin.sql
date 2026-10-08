-- Insertar el super administrador por defecto si no existe
INSERT INTO users (username, email, password, is_active, created_at, updated_at)
VALUES (
    'admin', 
    'admin@aprendia.com', 
    '$2b$12$29ztIBSJHEwzskgBjbsOBuP/9XdrFpjYkH0LLJ2ZsaBZKvG4qUseu', -- Hash BCrypt de '140823Schme-Som1917'
    TRUE, 
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP
)
ON CONFLICT (username) DO NOTHING;

-- Asignar el rol 'ROLE_ADMIN' al usuario 'admin'
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN'
ON CONFLICT (user_id, role_id) DO NOTHING;
