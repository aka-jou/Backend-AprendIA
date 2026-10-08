-- Tabla del catálogo de Dependencias (/api/v1/catalogs/dependencies)
CREATE TABLE dependencies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    internal_code VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system'
);
