-- Tipos de layout, layouts, catálogo de habilidades, reactivos y ejercicios
-- (Especificación Técnica de Backend: CRUD de Tipos de Layout, Layouts, Habilidades, Reactivos y Ejercicios).
--
-- Correspondencia con el diagrama de base de datos del proyecto (nombres en español en el diagrama,
-- en inglés aquí para seguir la convención del resto del esquema):
--   tipo_layout              -> layout_types
--   layout                   -> layouts
--   categoria_habilidad      -> skill_categories
--   habilidad                -> skills
--   reactivo                 -> reactivos
--   reactivo_habilidades     -> reactivo_skills           (peso)
--   reactivo_habilidades_prev-> reactivo_previous_skills
--   ejercicio                -> exercises                 (contexto JSON)
-- Columnas que pide la spec y el diagrama no tiene: layouts.code, reactivos.status, exercises.sort_order
-- y exercises.instruction. La tabla "agente" y los puntajes (habilidad_puntaje, ejercicio_puntaje) no
-- tienen endpoints en la spec y no se crean todavía.

-- ---------- Tipos de layout ----------
CREATE TABLE layout_types (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description TEXT         NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX ux_layout_types_name ON layout_types (LOWER(name));

-- ---------- Layouts (plantillas interactivas) ----------
-- code es el identificador técnico que usan el frontend y la app móvil (templateType).
CREATE TABLE layouts (
    id             BIGSERIAL PRIMARY KEY,
    layout_type_id BIGINT        NOT NULL,
    code           VARCHAR(50)   NOT NULL,
    name           VARCHAR(150)  NOT NULL,
    description    TEXT          NOT NULL,
    image_url      VARCHAR(1000),
    attributes     JSONB         NOT NULL DEFAULT '{}'::jsonb,
    is_active      BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_layout_type FOREIGN KEY (layout_type_id) REFERENCES layout_types (id),
    CONSTRAINT ux_layouts_code UNIQUE (code),
    CONSTRAINT ck_layout_attributes CHECK (jsonb_typeof(attributes) = 'object')
);
CREATE INDEX idx_layouts_type ON layouts (layout_type_id);

-- ---------- Catálogo de habilidades ----------
CREATE TABLE skill_categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(500)
);
CREATE UNIQUE INDEX ux_skill_categories_name ON skill_categories (LOWER(name));

CREATE TABLE skills (
    id          BIGSERIAL PRIMARY KEY,
    category_id BIGINT,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    status      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_skill_category FOREIGN KEY (category_id) REFERENCES skill_categories (id)
);
-- Nombre único sin distinguir mayúsculas: previousSkills llega por nombre y debe resolverse a una sola habilidad
CREATE UNIQUE INDEX ux_skills_name ON skills (LOWER(name));

-- ---------- Reactivos ----------
-- Misma baja lógica (deleted_at) que el resto del módulo curricular. status es el estado editorial.
CREATE TABLE reactivos (
    id                     BIGSERIAL PRIMARY KEY,
    topic_id               BIGINT        NOT NULL,
    layout_id              BIGINT        NOT NULL,
    title                  VARCHAR(200)  NOT NULL,
    instructions           TEXT          NOT NULL,
    image_url              VARCHAR(1000) NOT NULL,
    suggested_time_minutes INTEGER       NOT NULL,
    status                 VARCHAR(10)   NOT NULL DEFAULT 'draft',
    created_at             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by             VARCHAR(50)   NOT NULL DEFAULT 'system',
    updated_at             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by             VARCHAR(50)   NOT NULL DEFAULT 'system',
    deleted_at             TIMESTAMP,
    deleted_by             VARCHAR(50),
    CONSTRAINT fk_reactivo_topic FOREIGN KEY (topic_id) REFERENCES topics (id),
    CONSTRAINT fk_reactivo_layout FOREIGN KEY (layout_id) REFERENCES layouts (id),
    CONSTRAINT ck_reactivo_time CHECK (suggested_time_minutes >= 1),
    CONSTRAINT ck_reactivo_status CHECK (status IN ('active', 'draft', 'inactive'))
);
CREATE INDEX idx_reactivos_topic ON reactivos (topic_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_reactivos_layout ON reactivos (layout_id);

-- Habilidades evaluadas con su peso (la suma por reactivo la valida la API: 100).
-- sort_order conserva el orden en que el frontend envía las habilidades.
CREATE TABLE reactivo_skills (
    reactivo_id BIGINT       NOT NULL,
    skill_id    BIGINT       NOT NULL,
    weight      NUMERIC(5,2) NOT NULL,
    sort_order  INTEGER      NOT NULL,
    CONSTRAINT pk_reactivo_skills PRIMARY KEY (reactivo_id, skill_id),
    CONSTRAINT fk_reactivo_skills_reactivo FOREIGN KEY (reactivo_id) REFERENCES reactivos (id) ON DELETE CASCADE,
    CONSTRAINT fk_reactivo_skills_skill FOREIGN KEY (skill_id) REFERENCES skills (id),
    CONSTRAINT ck_reactivo_skills_weight CHECK (weight > 0 AND weight <= 100)
);
CREATE INDEX idx_reactivo_skills_skill ON reactivo_skills (skill_id);

-- Habilidades previas requeridas
CREATE TABLE reactivo_previous_skills (
    reactivo_id BIGINT  NOT NULL,
    skill_id    BIGINT  NOT NULL,
    sort_order  INTEGER NOT NULL,
    CONSTRAINT pk_reactivo_previous_skills PRIMARY KEY (reactivo_id, skill_id),
    CONSTRAINT fk_reactivo_prev_reactivo FOREIGN KEY (reactivo_id) REFERENCES reactivos (id) ON DELETE CASCADE,
    CONSTRAINT fk_reactivo_prev_skill FOREIGN KEY (skill_id) REFERENCES skills (id)
);
CREATE INDEX idx_reactivo_prev_skill ON reactivo_previous_skills (skill_id);

-- ---------- Ejercicios ----------
-- El diagrama no da layout propio al ejercicio: su plantilla (templateType) es el layout del reactivo.
CREATE TABLE exercises (
    id          BIGSERIAL PRIMARY KEY,
    reactivo_id BIGINT      NOT NULL,
    sort_order  INTEGER     NOT NULL,
    instruction TEXT        NOT NULL,
    context     JSONB       NOT NULL,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by  VARCHAR(50) NOT NULL DEFAULT 'system',
    updated_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by  VARCHAR(50) NOT NULL DEFAULT 'system',
    deleted_at  TIMESTAMP,
    deleted_by  VARCHAR(50),
    CONSTRAINT fk_exercise_reactivo FOREIGN KEY (reactivo_id) REFERENCES reactivos (id),
    CONSTRAINT ck_exercise_sort_order CHECK (sort_order >= 1),
    CONSTRAINT ck_exercise_context CHECK (jsonb_typeof(context) = 'object')
);
CREATE INDEX idx_exercises_reactivo ON exercises (reactivo_id, sort_order) WHERE deleted_at IS NULL;

-- ---------- Datos iniciales: los registros que la spec muestra como respuesta de GET ----------
-- Tablas recién creadas: se insertan con ID fijo para que coincidan con la spec y luego se ajusta la secuencia.
INSERT INTO layout_types (id, name, description, created_at, updated_at) VALUES
    (1, 'Discriminación Visual', 'Actividades de identificación de imágenes, alternativas y reconocimiento visual.', '2026-10-29 18:00:00', '2026-10-29 18:00:00'),
    (2, 'Construcción Léxica', 'Deletreo, formación de palabras y estructuración ortográfica.', '2026-10-29 18:00:00', '2026-10-29 18:00:00'),
    (3, 'Asociación y Emparejamiento', 'Relación de conceptos, pares correspondientes y enlaces visuales.', '2026-10-29 18:00:00', '2026-10-29 18:00:00'),
    (4, 'Expositivo y Secuencial', 'Contenido didáctico guiado, historias visuales y explicaciones con video LSM.', '2026-10-29 18:00:00', '2026-10-29 18:00:00');
SELECT setval(pg_get_serial_sequence('layout_types', 'id'), (SELECT MAX(id) FROM layout_types));

INSERT INTO layouts (id, layout_type_id, code, name, description, image_url, attributes, is_active, created_at, updated_at) VALUES
    (1, 1, 'identify_family', 'Identificar Familia (1 Imagen, 4 Opciones)',
        'El alumno observa una imagen o seña principal y elige la respuesta correcta entre 4 alternativas.',
        'https://images.unsplash.com/photo-1511895426328-dc8714191300?auto=format&fit=crop&w=400&q=80',
        '{"optionsCount": 4, "hasMainImage": true}', TRUE, '2026-10-29 18:00:00', '2026-10-29 18:00:00'),
    (2, 2, 'spelling_name', 'Deletreo de Palabra',
        'El alumno forma la palabra objetivo seleccionando y ordenando letras con apoyo de imagen y distractores.',
        'https://images.unsplash.com/photo-1546410531-bb4caa6b424d?auto=format&fit=crop&w=400&q=80',
        '{"supportsDistractors": true, "allowReferenceImage": true}', TRUE, '2026-10-29 18:00:00', '2026-10-29 18:00:00'),
    (3, 3, 'matching_7x7', 'Asociar 7 a 7 (Arrastrar)',
        'Emparejamiento por arrastre de 7 elementos origen hacia 7 elementos destino.',
        'https://images.unsplash.com/photo-1503676260728-1c00da094a0b?auto=format&fit=crop&w=400&q=80',
        '{"pairsCount": 7}', TRUE, '2026-10-29 18:00:00', '2026-10-29 18:00:00'),
    (4, 3, 'image_linking_5x5', 'Uniendo 5x5 (Líneas de Asociación)',
        'Trazado y unión directa entre 5 imágenes y 5 conceptos o señas en LSM.',
        'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=400&q=80',
        '{"pairsCount": 5}', TRUE, '2026-10-29 18:00:00', '2026-10-29 18:00:00'),
    (5, 4, 'carousel_cards', 'Carrusel de Tarjetas Didácticas',
        'Presentación secuencial de tarjetas con imagen, explicación y video en LSM opcional.',
        'https://images.unsplash.com/photo-1588072432836-e10032774350?auto=format&fit=crop&w=400&q=80',
        '{"minSlides": 2, "maxSlides": 10, "supportsLsmVideo": true}', TRUE, '2026-10-29 18:00:00', '2026-10-29 18:00:00');
SELECT setval(pg_get_serial_sequence('layouts', 'id'), (SELECT MAX(id) FROM layouts));

INSERT INTO skills (id, name, description, status) VALUES
    (1, 'Percepción Visual', 'Reconocimiento y discriminación de configuraciones visuales y señas.', TRUE),
    (2, 'Lectoescritura', 'Asociación grafema-palabra y comprensión del español escrito.', TRUE),
    (3, 'Lengua de Señas (LSM)', 'Dominio de dactilología y vocabulario en Lengua de Señas Mexicana.', TRUE),
    (4, 'Memoria y Retención', 'Retención visual y secuencial de conceptos y vocabulario.', TRUE),
    (5, 'Pensamiento Lógico', 'Asociación conceptual, categorización y resolución de problemas.', TRUE),
    (6, 'Atención y Concentración', 'Enfoque sostenido durante actividades visuales e interactivas.', TRUE),
    (7, 'Habilidades Motrices', 'Coordinación fina en trazos, arrastre y unión de elementos.', TRUE),
    (8, 'Comunicación y Lenguaje', 'Expresión y comprensión pragmática de conceptos cotidianos.', TRUE);
SELECT setval(pg_get_serial_sequence('skills', 'id'), (SELECT MAX(id) FROM skills));

-- ---------- Recursos: idLayout pasa a apuntar al catálogo ----------
-- resources.layout_id se guardaba sin catálogo (V25). Un valor que no corresponde a ningún layout se
-- limpia antes de crear la llave foránea; el seed de V26 usa 5 (carousel_cards), que sí existe.
UPDATE resources SET layout_id = NULL
WHERE layout_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM layouts l WHERE l.id = resources.layout_id);
ALTER TABLE resources DROP CONSTRAINT ck_resource_layout;
ALTER TABLE resources ADD CONSTRAINT fk_resource_layout FOREIGN KEY (layout_id) REFERENCES layouts (id);
CREATE INDEX idx_resources_layout ON resources (layout_id);
