-- Campos requeridos por el catálogo de Roles (/api/v1/catalogs/roles)
ALTER TABLE roles
    ADD COLUMN description TEXT,
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN created_by VARCHAR(50) NOT NULL DEFAULT 'system';
