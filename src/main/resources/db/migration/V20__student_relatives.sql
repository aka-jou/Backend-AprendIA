-- Módulo 4 de la Especificación Técnica v2.0: familiares/tutores del estudiante como { name, relationship, phone }.
-- Sustituye al modelo anterior (person_relatives + relative_roles), que guardaba a cada familiar como una
-- fila en persons con el nombre completo en first_name y 'N/A' como apellido, y no guardaba el teléfono.
CREATE TABLE student_relatives (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    relationship VARCHAR(50) NOT NULL,
    phone VARCHAR(255),              -- cifrado por la aplicación (EncryptedStringConverter)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_student_relative_student FOREIGN KEY (student_id) REFERENCES students (id) ON DELETE CASCADE
);
CREATE INDEX idx_student_relatives_student ON student_relatives (student_id);

-- Migración de los familiares ya registrados. El teléfono se copia tal cual: ambos modelos usan
-- el mismo conversor y la misma clave, así que el valor cifrado sigue siendo válido.
INSERT INTO student_relatives (student_id, name, relationship, phone)
SELECT s.id,
       LEFT(TRIM(rp.first_name || CASE WHEN rp.last_name IS NULL OR rp.last_name IN ('', 'N/A') THEN '' ELSE ' ' || rp.last_name END), 150),
       LEFT(CASE rr.name
                WHEN 'Mother'      THEN 'Madre'
                WHEN 'Father'      THEN 'Padre'
                WHEN 'Tutor'       THEN 'Tutor Legal'
                WHEN 'Uncle'       THEN 'Tío'
                WHEN 'Aunt'        THEN 'Tía'
                WHEN 'Grandparent' THEN 'Abuelo/a'
                WHEN 'Other'       THEN 'Otro'
                ELSE rr.name
            END, 50),
       rp.phone
FROM person_relatives pr
JOIN students s        ON s.person_id = pr.person_id
JOIN persons rp        ON rp.id = pr.relative_person_id
JOIN relative_roles rr ON rr.id = pr.relative_role_id
ORDER BY pr.id;

-- person_relatives y relative_roles se conservan (sin uso) para permitir reversión; pueden eliminarse
-- en una migración posterior una vez validado el cambio.
