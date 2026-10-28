-- Datos iniciales del módulo curricular: los registros que la especificación muestra como respuesta
-- de ejemplo (datos mockeados del frontend). Mismo criterio que V18:
--  * Se insertan en orden fijo; en una base limpia los IDs coinciden con los de la spec.
--  * Cada INSERT se omite si ya existe un registro con el mismo nombre bajo el mismo padre,
--    por lo que la migración no duplica datos capturados a mano.
--  * Solo se siembra lo que la spec describe con datos completos. Los contadores de la spec que no
--    tienen registros detrás (ej. totalTemas = 7 de U1, totalUnidades = 1 de C2) no se inventan:
--    la API devuelve el conteo real de lo que existe en la base.

-- ---------- Ruta 1: perfil e imagen que muestra el árbol curricular (sección 6) ----------
UPDATE learning_paths lp
SET profile_id = p.id,
    image_url  = 'https://images.unsplash.com/photo-1497633762265-9d179a990aa6?auto=format&fit=crop&w=600&q=80'
FROM (SELECT id FROM profiles WHERE name = 'Estudiante Sordo (LSM Primario)' ORDER BY id LIMIT 1) p
WHERE lp.name = 'Plan de Aprendizaje Logogenia - Nivel Primaria'
  AND lp.profile_id IS NULL
  AND lp.image_url IS NULL;

-- ---------- Cuadernillos (sección 2.2) ----------
INSERT INTO workbooks (learning_path_id, name, description, objective, author, image_url, status, sort_order)
SELECT lp.id, v.name, v.description, v.objective, v.author, v.image_url, TRUE, v.sort_order
FROM (VALUES
    (1, 'Plan de Aprendizaje Logogenia - Nivel Primaria', 'C1: Primeras Letras',
        'Iniciación al abecedario dactilológico, grafemas y primeras palabras cotidianas.',
        'Reconocer las letras del alfabeto manual en LSM y asociarlas con palabras de su entorno familiar.',
        'Dra. Bruna Radelli / Asesores CAM Chiapas',
        'https://images.unsplash.com/photo-1546410531-bb4caa6b424d?auto=format&fit=crop&w=600&q=80', 1),
    (2, 'Plan de Aprendizaje Logogenia - Nivel Primaria', 'C2: Entre Números',
        'Conteo visual, ordenamiento numérico y razonamiento cuantitativo.',
        'Dominar la numeración visual en LSM y su representación cardinal y ordinal del 1 al 20.',
        'Mtro. Roberto Carlos Domínguez',
        'https://images.unsplash.com/photo-1509228468518-180dd4864904?auto=format&fit=crop&w=600&q=80', 2),
    (3, 'Plan Bilingüe Bicultural - LSM Inicial', 'C1: Fundamentos de LSM',
        'Configuraciones manuales básicas, orientación espacial y expresión facial.',
        'Desarrollar soltura en el espacio de señación y señas elementales del entorno.',
        'Comunidad Sorda de Chiapas / SEP',
        'https://images.unsplash.com/photo-1588072432836-e10032774350?auto=format&fit=crop&w=600&q=80', 1)
) AS v(ord, path_name, name, description, objective, author, image_url, sort_order)
JOIN LATERAL (SELECT id FROM learning_paths WHERE name = v.path_name ORDER BY id LIMIT 1) lp ON TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM workbooks w WHERE w.learning_path_id = lp.id AND w.name = v.name AND w.deleted_at IS NULL
)
ORDER BY v.ord;

-- ---------- Unidades del cuadernillo C1: Primeras Letras (sección 3.1) ----------
INSERT INTO units (workbook_id, name, image_url, sort_order)
SELECT w.id, v.name, v.image_url, v.sort_order
FROM (VALUES
    (1, 'U1: Primeras letras',
        'https://images.unsplash.com/photo-1503676260728-1c00da094a0b?auto=format&fit=crop&w=400&q=80', 1),
    (2, 'U2: Español entre amigos',
        'https://images.unsplash.com/photo-1529156069898-49953e39b3ac?auto=format&fit=crop&w=400&q=80', 2)
) AS v(ord, name, image_url, sort_order)
JOIN LATERAL (
    SELECT w.id FROM workbooks w
    JOIN learning_paths lp ON lp.id = w.learning_path_id
    WHERE w.name = 'C1: Primeras Letras'
      AND lp.name = 'Plan de Aprendizaje Logogenia - Nivel Primaria'
      AND w.deleted_at IS NULL
    ORDER BY w.id LIMIT 1
) w ON TRUE
WHERE NOT EXISTS (SELECT 1 FROM units u WHERE u.workbook_id = w.id AND u.name = v.name AND u.deleted_at IS NULL)
ORDER BY v.ord;

-- ---------- Temas de la unidad U1: Primeras letras (sección 4.1) ----------
INSERT INTO topics (unit_id, name, description, image_url, sort_order)
SELECT u.id, v.name, v.description, v.image_url, v.sort_order
FROM (VALUES
    (1, 'Dactilología', 'Configuración manual de cada una de las letras del abecedario.',
        'https://images.unsplash.com/photo-1588072432836-e10032774350?auto=format&fit=crop&w=400&q=80', 1),
    (2, '¿Quién soy?', 'Identidad, nombre propio, presentación personal y señas individuales.',
        'https://images.unsplash.com/photo-1511895426328-dc8714191300?auto=format&fit=crop&w=400&q=80', 2),
    (3, 'Señas de familia', 'Vocabulario y relaciones familiares en LSM: mamá, papá, hermanos, tíos y abuelos.',
        'https://images.unsplash.com/photo-1577896851231-70ef18881754?auto=format&fit=crop&w=400&q=80', 3)
) AS v(ord, name, description, image_url, sort_order)
JOIN LATERAL (
    SELECT u.id FROM units u
    JOIN workbooks w ON w.id = u.workbook_id
    WHERE u.name = 'U1: Primeras letras'
      AND w.name = 'C1: Primeras Letras'
      AND u.deleted_at IS NULL
    ORDER BY u.id LIMIT 1
) u ON TRUE
WHERE NOT EXISTS (SELECT 1 FROM topics t WHERE t.unit_id = u.id AND t.name = v.name AND t.deleted_at IS NULL)
ORDER BY v.ord;

-- ---------- Recurso del tema "Señas de familia" (sección 5.1) ----------
INSERT INTO resources (topic_id, layout_id, title, image_url, content)
SELECT t.id, 5, 'Guía Visual: Miembros de la Familia en LSM',
       'https://images.unsplash.com/photo-1511895426328-dc8714191300?auto=format&fit=crop&w=400&q=80',
       '{"slides": [
           {"order": 1, "title": "Mamá",
            "description": "La seña se realiza con la letra M tocando suavemente la barbilla.",
            "imageUrl": "https://images.unsplash.com/photo-1511895426328-dc8714191300?auto=format&fit=crop&w=400&q=80"},
           {"order": 2, "title": "Papá",
            "description": "La seña se realiza con la letra P en la frente o mejilla.",
            "imageUrl": "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=400&q=80"}
        ]}'::jsonb
FROM (
    SELECT t.id FROM topics t
    JOIN units u ON u.id = t.unit_id
    WHERE t.name = 'Señas de familia'
      AND u.name = 'U1: Primeras letras'
      AND t.deleted_at IS NULL
    ORDER BY t.id LIMIT 1
) t
WHERE NOT EXISTS (
    SELECT 1 FROM resources r
    WHERE r.topic_id = t.id AND r.title = 'Guía Visual: Miembros de la Familia en LSM' AND r.deleted_at IS NULL
);
