-- Tabla del catálogo de Metodologías (/api/v1/catalogs/metodologies)
CREATE TABLE metodologies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    acronym VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system'
);
