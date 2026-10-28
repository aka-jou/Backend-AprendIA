-- Módulo de Cuadernillos y Estructura Curricular (anexo de la Especificación Técnica v2.0).
-- Jerarquía: Ruta de aprendizaje -> Cuadernillo -> Unidad -> Tema -> Recurso.
--
-- Decisiones de diseño (documentadas en el informe técnico, paso 13):
--  * status (solo cuadernillos) es el estado editorial que define la spec: true = activo,
--    false = inactivo o borrador. Se cambia con PUT.
--  * deleted_at es la baja lógica de DELETE. Un registro con deleted_at deja de existir para la API
--    (no aparece en listados, GET por id responde 404) pero se conserva en la base. Así se cumplen
--    las dos lecturas de la spec ("eliminar o desactivar") sin mezclar borrado con borrador.
--  * La baja de un nivel marca con la misma marca de tiempo a todos sus descendientes.
--  * No se agrega ninguna restricción de unicidad sobre "orden": la spec no la pide y el frontend
--    puede reordenar en varios pasos. Los listados ordenan por (orden, id).

-- ---------- Rutas de aprendizaje: campos que devuelve el árbol curricular ----------
-- El árbol (sección 6) expone "perfil" y "urlImagen" de la ruta; la sección 5.2 no los tenía.
-- Ambos son opcionales para no invalidar las rutas existentes.
ALTER TABLE learning_paths ADD COLUMN profile_id BIGINT;
ALTER TABLE learning_paths ADD COLUMN image_url VARCHAR(1000);
ALTER TABLE learning_paths
    ADD CONSTRAINT fk_learning_path_profile FOREIGN KEY (profile_id) REFERENCES profiles (id);
CREATE INDEX idx_learning_paths_profile ON learning_paths (profile_id);

-- ---------- Cuadernillos ----------
CREATE TABLE workbooks (
    id               BIGSERIAL PRIMARY KEY,
    learning_path_id BIGINT        NOT NULL,
    name             VARCHAR(150)  NOT NULL,
    description      TEXT          NOT NULL,
    objective        TEXT          NOT NULL,
    author           VARCHAR(200)  NOT NULL,
    image_url        VARCHAR(1000) NOT NULL,
    status           BOOLEAN       NOT NULL DEFAULT TRUE,
    sort_order       INTEGER       NOT NULL DEFAULT 1,
    created_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by       VARCHAR(50)   NOT NULL DEFAULT 'system',
    updated_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by       VARCHAR(50)   NOT NULL DEFAULT 'system',
    deleted_at       TIMESTAMP,
    deleted_by       VARCHAR(50),
    CONSTRAINT fk_workbook_learning_path FOREIGN KEY (learning_path_id) REFERENCES learning_paths (id),
    CONSTRAINT ck_workbook_sort_order CHECK (sort_order >= 1)
);
-- Índice parcial: todas las lecturas filtran por deleted_at IS NULL
CREATE INDEX idx_workbooks_learning_path ON workbooks (learning_path_id, sort_order) WHERE deleted_at IS NULL;

-- ---------- Unidades didácticas ----------
CREATE TABLE units (
    id          BIGSERIAL PRIMARY KEY,
    workbook_id BIGINT        NOT NULL,
    name        VARCHAR(150)  NOT NULL,
    image_url   VARCHAR(1000),
    sort_order  INTEGER       NOT NULL DEFAULT 1,
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by  VARCHAR(50)   NOT NULL DEFAULT 'system',
    updated_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by  VARCHAR(50)   NOT NULL DEFAULT 'system',
    deleted_at  TIMESTAMP,
    deleted_by  VARCHAR(50),
    CONSTRAINT fk_unit_workbook FOREIGN KEY (workbook_id) REFERENCES workbooks (id),
    CONSTRAINT ck_unit_sort_order CHECK (sort_order >= 1)
);
CREATE INDEX idx_units_workbook ON units (workbook_id, sort_order) WHERE deleted_at IS NULL;

-- ---------- Temas curriculares ----------
CREATE TABLE topics (
    id          BIGSERIAL PRIMARY KEY,
    unit_id     BIGINT        NOT NULL,
    name        VARCHAR(150)  NOT NULL,
    description TEXT,
    image_url   VARCHAR(1000),
    sort_order  INTEGER       NOT NULL DEFAULT 1,
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by  VARCHAR(50)   NOT NULL DEFAULT 'system',
    updated_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by  VARCHAR(50)   NOT NULL DEFAULT 'system',
    deleted_at  TIMESTAMP,
    deleted_by  VARCHAR(50),
    CONSTRAINT fk_topic_unit FOREIGN KEY (unit_id) REFERENCES units (id),
    CONSTRAINT ck_topic_sort_order CHECK (sort_order >= 1)
);
CREATE INDEX idx_topics_unit ON topics (unit_id, sort_order) WHERE deleted_at IS NULL;

-- ---------- Recursos didácticos ----------
-- layout_id: la spec usa "idLayout" sin definir catálogo; se guarda como entero sin FK.
-- content: objeto { "slides": [ { order, title, description, imageUrl } ] } en JSONB.
CREATE TABLE resources (
    id          BIGSERIAL PRIMARY KEY,
    topic_id    BIGINT        NOT NULL,
    layout_id   INTEGER,
    title       VARCHAR(200)  NOT NULL,
    image_url   VARCHAR(1000),
    content     JSONB         NOT NULL,
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by  VARCHAR(50)   NOT NULL DEFAULT 'system',
    updated_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by  VARCHAR(50)   NOT NULL DEFAULT 'system',
    deleted_at  TIMESTAMP,
    deleted_by  VARCHAR(50),
    CONSTRAINT fk_resource_topic FOREIGN KEY (topic_id) REFERENCES topics (id),
    CONSTRAINT ck_resource_layout CHECK (layout_id IS NULL OR layout_id >= 1),
    CONSTRAINT ck_resource_content CHECK (jsonb_typeof(content -> 'slides') = 'array')
);
CREATE INDEX idx_resources_topic ON resources (topic_id) WHERE deleted_at IS NULL;
