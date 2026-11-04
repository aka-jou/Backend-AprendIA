-- Búsqueda de persona por CURP y vinculación de familiares (Especificación Técnica de Backend: Búsqueda
-- de Persona por CURP y Vinculación de Familiares en Estudiantes).
--
-- Antes (V20 y V22) cada familiar guardaba sus datos completos dentro de student_relatives: una misma
-- madre con dos hijos inscritos quedaba registrada dos veces. Ahora el familiar es una fila única de
-- persons y student_relatives solo guarda el vínculo (student_id, person_id, relationship), como pide
-- la spec. Los datos de contacto que la spec devuelve en la búsqueda (correo y domicilio) pasan a persons.

-- ---------------------------------------------------------------- 1. persons: contacto y domicilio
-- Mismas columnas (y mismo cifrado en la aplicación) que tenía student_relatives desde V22.
-- Nullable: los estudiantes y usuarios ya registrados no los tienen; el domicilio del estudiante sigue en addresses.
ALTER TABLE persons
    ADD COLUMN email                   VARCHAR(100),
    ADD COLUMN address_street          VARCHAR(500),   -- cifrado
    ADD COLUMN address_exterior_number VARCHAR(255),   -- cifrado
    ADD COLUMN address_settlement_type VARCHAR(100),
    ADD COLUMN address_settlement      VARCHAR(255),   -- cifrado
    ADD COLUMN address_municipality_id INT,
    ADD COLUMN address_state_id        INT,
    ADD COLUMN address_zip_code        VARCHAR(255);   -- cifrado

-- ---------------------------------------------------------------- 2. familiares existentes -> persons
-- El cifrado es determinístico (mismo texto, mismo cifrado, misma clave): una CURP de student_relatives
-- se compara directamente con persons.curp. Si ya existe una persona con esa CURP se reutiliza; si no,
-- se crea. Los familiares sin CURP se crean uno por uno. Los valores cifrados se copian tal cual.
ALTER TABLE student_relatives ADD COLUMN person_id BIGINT;

DO $$
DECLARE
    r   RECORD;
    pid BIGINT;
BEGIN
    FOR r IN SELECT * FROM student_relatives ORDER BY id LOOP
        pid := NULL;
        IF r.curp IS NOT NULL THEN
            SELECT p.id INTO pid FROM persons p WHERE p.curp = r.curp;
        END IF;
        IF pid IS NULL THEN
            INSERT INTO persons (first_name, middle_name, last_name, second_last_name, curp, birth_date, gender,
                                 phone, email, address_street, address_exterior_number, address_settlement_type,
                                 address_settlement, address_municipality_id, address_state_id, address_zip_code)
            VALUES (
                -- Los familiares anteriores a V22 solo tienen el nombre completo en "name"
                LEFT(COALESCE(NULLIF(TRIM(r.first_name), ''), r.name), 100),
                r.middle_name,
                COALESCE(r.last_name, ''),
                r.second_last_name,
                r.curp,
                r.birth_date::timestamp,
                r.gender,
                r.phone,
                r.email,
                r.address_street,
                r.address_exterior_number,
                r.address_settlement_type,
                r.address_settlement,
                r.address_municipality_id,
                r.address_state_id,
                r.address_zip_code)
            RETURNING id INTO pid;
        END IF;
        UPDATE student_relatives SET person_id = pid WHERE id = r.id;
    END LOOP;

    IF EXISTS (SELECT 1 FROM student_relatives WHERE person_id IS NULL) THEN
        RAISE EXCEPTION 'V28: quedaron familiares sin persona vinculada';
    END IF;
END $$;

-- Un mismo familiar registrado dos veces para el mismo alumno (misma CURP) queda en un solo vínculo
DELETE FROM student_relatives a
USING student_relatives b
WHERE a.student_id = b.student_id
  AND a.person_id = b.person_id
  AND a.id > b.id;

ALTER TABLE student_relatives ALTER COLUMN person_id SET NOT NULL;
ALTER TABLE student_relatives
    ADD CONSTRAINT fk_student_relative_person FOREIGN KEY (person_id) REFERENCES persons (id);
ALTER TABLE student_relatives
    ADD CONSTRAINT ux_student_relative_person UNIQUE (student_id, person_id);
CREATE INDEX idx_student_relatives_person ON student_relatives (person_id);

-- ---------------------------------------------------------------- 3. student_relatives queda como vínculo
-- Los datos ya están en persons; se retiran para no tener dos copias de los mismos datos personales.
ALTER TABLE student_relatives
    DROP COLUMN name,
    DROP COLUMN first_name,
    DROP COLUMN middle_name,
    DROP COLUMN last_name,
    DROP COLUMN second_last_name,
    DROP COLUMN curp,
    DROP COLUMN birth_date,
    DROP COLUMN gender,
    DROP COLUMN phone,
    DROP COLUMN email,
    DROP COLUMN address_street,
    DROP COLUMN address_exterior_number,
    DROP COLUMN address_settlement_type,
    DROP COLUMN address_settlement,
    DROP COLUMN address_municipality_id,
    DROP COLUMN address_state_id,
    DROP COLUMN address_zip_code;
