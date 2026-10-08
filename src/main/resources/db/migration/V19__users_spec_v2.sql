-- Módulo 2 de la Especificación Técnica v2.0: Personas y Usuarios del Sistema.

-- assignment.ineNumber: clave de elector / número de credencial. La aplicación lo guarda cifrado
-- (EncryptedStringConverter), por eso la columna es más ancha que el dato en claro.
ALTER TABLE persons ADD COLUMN ine_number VARCHAR(255);

-- assignment.dependency: adscripción del usuario (centro o dependencia educativa)
ALTER TABLE users ADD COLUMN dependency_id BIGINT;
ALTER TABLE users ADD CONSTRAINT fk_user_dependency
    FOREIGN KEY (dependency_id) REFERENCES dependencies (id) ON DELETE SET NULL;
CREATE INDEX idx_users_dependency ON users (dependency_id);

-- Dependencias que aparecen en los datos mockeados de la spec (adscripciones de los usuarios de ejemplo)
INSERT INTO dependencies (name, internal_code)
SELECT v.name, v.code
FROM (VALUES
    (1, 'Dirección de Educación Especial (Tuxtla Gtz.)', 'DEE-TGZ'),
    (2, 'Supervisión Escolar 04 - Altos de Chiapas',     'SUP-04-ALT'),
    (3, 'Mesa Técnica de Inclusión y LSM',                'MTI-LSM'),
    (4, 'CAM No. 1 - Tuxtla Gutiérrez',                   'CAM-01-TGZ'),
    (5, 'Unidad de Apoyo Diagnóstico Tapachula',          'UAD-TAP'),
    (6, 'Coordinación de Tecnologías Educativas',         'CTE')
) AS v(ord, name, code)
WHERE NOT EXISTS (SELECT 1 FROM dependencies d WHERE LOWER(d.name) = LOWER(v.name))
ORDER BY v.ord;
