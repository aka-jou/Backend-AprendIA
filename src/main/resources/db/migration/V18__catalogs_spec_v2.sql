-- Catálogos del Módulo 3 de la Especificación Técnica v2.0:
-- Metodologías, Rutas de Aprendizaje y Perfiles.

-- ---------- Metodologías: { id, nombre, autor (opcional), status } ----------
ALTER TABLE metodologies ADD COLUMN author VARCHAR(150);
ALTER TABLE metodologies ADD COLUMN status BOOLEAN NOT NULL DEFAULT TRUE;
-- La spec no contempla la sigla: se conserva la columna (sin pérdida de datos) pero deja de ser obligatoria
ALTER TABLE metodologies ALTER COLUMN acronym DROP NOT NULL;

-- ---------- Perfiles: { id, nombre, descripcion (opcional), status } ----------
ALTER TABLE profiles ADD COLUMN description TEXT;
ALTER TABLE profiles ADD COLUMN status BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE profiles ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE profiles ADD COLUMN created_by VARCHAR(50) NOT NULL DEFAULT 'system';
-- La spec no contempla el nivel de acceso: se conserva la columna pero deja de ser obligatoria
ALTER TABLE profiles ALTER COLUMN access_level DROP NOT NULL;

-- ---------- Rutas de aprendizaje: { id, nombre, idMetodologia (FK), status } ----------
CREATE TABLE learning_paths (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    metodology_id BIGINT NOT NULL,
    status BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    CONSTRAINT fk_learning_path_metodology FOREIGN KEY (metodology_id) REFERENCES metodologies (id)
);
CREATE INDEX idx_learning_paths_metodology ON learning_paths (metodology_id);

-- ---------- Datos iniciales (los "datos mockeados que espera el frontend") ----------
-- Solo se insertan si no existe ya un registro con el mismo nombre.
INSERT INTO metodologies (name, author, status)
SELECT v.name, v.author, TRUE
FROM (VALUES
    (1, 'Logogenia',                              'Dra. Bruna Radelli'),
    (2, 'Bilingüe Bicultural',                    'Comunidad Sorda / SEP'),
    (3, 'Diseño Universal para el Aprendizaje',   'David H. Rose & Anne Meyer (CAST)'),
    (4, 'Estimulación Visual Multisensorial',     NULL),
    (5, 'Comunicación Aumentativa y Alternativa', 'ASHA')
) AS v(ord, name, author)
WHERE NOT EXISTS (SELECT 1 FROM metodologies m WHERE m.name = v.name)
ORDER BY v.ord;

INSERT INTO learning_paths (name, metodology_id, status)
SELECT v.name, m.id, TRUE
FROM (VALUES
    (1, 'Plan de Aprendizaje Logogenia - Nivel Primaria', 'Logogenia'),
    (2, 'Plan Bilingüe Bicultural - LSM Inicial',         'Bilingüe Bicultural'),
    (3, 'Lectura Comprensiva y Expresión Escrita',        'Logogenia'),
    (4, 'Estrategias DUA en el Aula Silente',             'Diseño Universal para el Aprendizaje')
) AS v(ord, name, metodology)
JOIN LATERAL (SELECT id FROM metodologies WHERE name = v.metodology ORDER BY id LIMIT 1) m ON TRUE
WHERE NOT EXISTS (SELECT 1 FROM learning_paths lp WHERE lp.name = v.name)
ORDER BY v.ord;

INSERT INTO profiles (name, description, status)
SELECT v.name, v.description, TRUE
FROM (VALUES
    (1, 'Estudiante Sordo (LSM Primario)', 'Acceso a materiales visuales y resolución de cuadernillos adaptados'),
    (2, 'Estudiante Hipoacúsico',          'Desarrollo de lectoescritura con apoyo visual y auditivo'),
    (3, 'Docente de Educación Especial',   'Gestión directa de alumnos en aula y seguimiento de evaluaciones'),
    (4, 'Asesor Técnico Pedagógico',       'Supervisión curricular y revisión técnico-pedagógica de materiales didácticos'),
    (5, 'Administrador Estatal',           'Gestión global de centros, catálogos y auditoría del sistema')
) AS v(ord, name, description)
WHERE NOT EXISTS (SELECT 1 FROM profiles p WHERE p.name = v.name)
ORDER BY v.ord;
