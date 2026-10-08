CREATE TABLE persons (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    second_last_name VARCHAR(100),
    curp VARCHAR(18) UNIQUE,
    birth_date TIMESTAMP,
    gender CHAR(1),
    phone VARCHAR(50),
    image_url VARCHAR(255)
);

ALTER TABLE users ADD COLUMN person_id BIGINT;
ALTER TABLE users ADD CONSTRAINT fk_user_person FOREIGN KEY (person_id) REFERENCES persons(id) ON DELETE CASCADE;

CREATE TABLE students (
    id BIGSERIAL PRIMARY KEY,
    person_id BIGINT NOT NULL,
    profile_id INT,
    educator_id BIGINT,
    qr_url VARCHAR(255),
    CONSTRAINT fk_student_person FOREIGN KEY (person_id) REFERENCES persons(id) ON DELETE CASCADE
);

-- Addresses (domicilio) pointing to students
CREATE TABLE addresses (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    street VARCHAR(150) NOT NULL,
    exterior_number VARCHAR(50),
    settlement_type VARCHAR(100),
    settlement VARCHAR(100),
    municipality_id INT,
    state_id INT,
    zip_code CHAR(10),
    CONSTRAINT fk_address_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

-- Relative Roles (rol_pariente)
CREATE TABLE relative_roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);

-- Insert base relative roles
INSERT INTO relative_roles (name) VALUES ('Mother'), ('Father'), ('Tutor'), ('Uncle'), ('Aunt'), ('Grandparent'), ('Other');

-- Person Relatives (persona_pariente) - Many to Many
CREATE TABLE person_relatives (
    id BIGSERIAL PRIMARY KEY,
    person_id BIGINT NOT NULL,
    relative_person_id BIGINT NOT NULL,
    relative_role_id INT NOT NULL,
    CONSTRAINT fk_pr_person FOREIGN KEY (person_id) REFERENCES persons(id) ON DELETE CASCADE,
    CONSTRAINT fk_pr_relative FOREIGN KEY (relative_person_id) REFERENCES persons(id) ON DELETE CASCADE,
    CONSTRAINT fk_pr_role FOREIGN KEY (relative_role_id) REFERENCES relative_roles(id) ON DELETE CASCADE,
    CONSTRAINT uq_person_relative UNIQUE (person_id, relative_person_id, relative_role_id)
);
