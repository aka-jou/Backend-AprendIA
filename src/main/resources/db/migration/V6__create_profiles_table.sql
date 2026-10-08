-- Tabla del catálogo de Perfiles (/api/v1/catalogs/profiles)
CREATE TABLE profiles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    access_level INTEGER NOT NULL
);
