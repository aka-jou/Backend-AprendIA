-- Alinea el catálogo de roles con la Especificación Técnica v2.0 (Módulo 2, campo assignment.roles).
-- Se conserva el prefijo ROLE_ que exige Spring Security; la API lo expone sin prefijo
-- (ej. ROLE_ADMINISTRADOR <-> "ADMINISTRADOR").

-- Renombrar los roles existentes cuyo significado coincide con la spec (se conservan los IDs y asignaciones)
UPDATE roles SET name = 'ROLE_ADMINISTRADOR',
                 description = COALESCE(description, 'Administración global del sistema')
WHERE name = 'ROLE_ADMIN'
  AND NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_ADMINISTRADOR');

UPDATE roles SET name = 'ROLE_DOCENTE_AULA',
                 description = COALESCE(description, 'Docente frente a grupo')
WHERE name = 'ROLE_EDUCATOR'
  AND NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_DOCENTE_AULA');

-- Roles nuevos definidos por la spec
INSERT INTO roles (name, description)
SELECT v.name, v.description
FROM (VALUES
    ('ROLE_SUPERVISOR_ZONA',       'Supervisión escolar de zona'),
    ('ROLE_ASESOR_PEDAGOGICO',     'Asesoría técnico-pedagógica'),
    ('ROLE_PSICOLOGO_CAM',         'Psicología en Centro de Atención Múltiple'),
    ('ROLE_ENLACE_INSTITUCIONAL',  'Enlace institucional')
) AS v(name, description)
WHERE NOT EXISTS (SELECT 1 FROM roles r WHERE r.name = v.name);
