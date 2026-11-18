-- Actualizar, eliminar y desactivar (documento "Endpoints y Campos Faltantes para Backend").
-- Agrega la columna de estado a las tablas que no la tenían, para poder desactivar sin borrar,
-- y los campos de catálogo que el frontend ya envía (metodologías y dependencias).

-- ---------------------------------------------------------------- roles
-- Roles oficiales: ADMINISTRADOR, DOCENTE_AULA, ENLACE_INSTITUCIONAL y STUDENT. Los demás roles de V17
-- quedan desactivados, no borrados: los usuarios que ya los tienen los conservan y se pueden reactivar
-- con PATCH /catalogs/roles/{id}/status.
ALTER TABLE roles ADD COLUMN status BOOLEAN NOT NULL DEFAULT TRUE;

UPDATE roles SET status = FALSE
WHERE name NOT IN ('ROLE_ADMINISTRADOR', 'ROLE_DOCENTE_AULA', 'ROLE_ENLACE_INSTITUCIONAL', 'ROLE_STUDENT');

-- ---------------------------------------------------------------- metodologías
-- "sigla" usa la columna acronym que existe desde V5.
ALTER TABLE metodologies
    ADD COLUMN description TEXT,
    ADD COLUMN image_url   VARCHAR(1000);

-- ---------------------------------------------------------------- dependencias
ALTER TABLE dependencies
    ADD COLUMN type   VARCHAR(50),
    ADD COLUMN status BOOLEAN NOT NULL DEFAULT TRUE;

-- ---------------------------------------------------------------- módulo curricular
-- Desactivar (status = false) es distinto de eliminar (deleted_at): lo desactivado se conserva y se
-- puede reactivar; los alumnos no lo ven.
ALTER TABLE units     ADD COLUMN status BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE topics    ADD COLUMN status BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE resources ADD COLUMN status BOOLEAN NOT NULL DEFAULT TRUE;
