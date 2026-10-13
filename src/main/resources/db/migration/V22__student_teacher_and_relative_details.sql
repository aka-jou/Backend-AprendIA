-- Observaciones del frontend sobre el Módulo 4 (estudiantes):
--   1. Relación docente-estudiante: cada alumno tiene un docente asignado (teacherId -> users.id).
--   2. Familiares con datos completos: nombre por partes, CURP, fecha de nacimiento, género,
--      teléfono, correo y domicilio propio.

-- ---------------------------------------------------------------- 1. docente asignado
-- students.educator_id existía desde V3 sin llave foránea ni uso en la aplicación. Se reutiliza
-- como teacher_id. Los valores que no apuntan a un usuario existente se limpian antes de crear la FK.
UPDATE students s
SET educator_id = NULL
WHERE s.educator_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM users u WHERE u.id = s.educator_id);

ALTER TABLE students RENAME COLUMN educator_id TO teacher_id;

ALTER TABLE students
    ADD CONSTRAINT fk_student_teacher FOREIGN KEY (teacher_id) REFERENCES users (id) ON DELETE SET NULL;

CREATE INDEX idx_students_teacher ON students (teacher_id);

-- ---------------------------------------------------------------- 2. familiares completos
-- Columnas nullable: los familiares registrados antes de este cambio solo tienen name/relationship/phone.
-- La obligatoriedad de los campos nuevos se valida en la API.
ALTER TABLE student_relatives
    ADD COLUMN first_name               VARCHAR(100),
    ADD COLUMN middle_name              VARCHAR(100),
    ADD COLUMN last_name                VARCHAR(100),
    ADD COLUMN second_last_name         VARCHAR(100),
    ADD COLUMN curp                     VARCHAR(255),   -- cifrado por la aplicación
    ADD COLUMN birth_date               DATE,
    ADD COLUMN gender                   CHAR(1),
    ADD COLUMN email                    VARCHAR(100),
    ADD COLUMN address_street           VARCHAR(500),   -- cifrado
    ADD COLUMN address_exterior_number  VARCHAR(255),   -- cifrado
    ADD COLUMN address_settlement_type  VARCHAR(100),
    ADD COLUMN address_settlement       VARCHAR(255),   -- cifrado
    ADD COLUMN address_municipality_id  INT,
    ADD COLUMN address_state_id         INT,
    ADD COLUMN address_zip_code         VARCHAR(255);   -- cifrado

ALTER TABLE student_relatives
    ADD CONSTRAINT ck_student_relative_gender CHECK (gender IS NULL OR gender IN ('M', 'F'));
