package com.aprendia.backend.feature.user.repository;
import com.aprendia.backend.feature.user.entities.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.lang.NonNull;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    @NonNull
    @EntityGraph(attributePaths = {"person", "address"})
    Page<Student> findAll(@NonNull Pageable pageable);

    @NonNull
    @EntityGraph(attributePaths = {"person", "address"})
    java.util.Optional<Student> findById(@NonNull Long id);

    java.util.Optional<Student> findByPersonId(Long personId);

    boolean existsByProfileId(Integer profileId);

    /**
     * Búsqueda reactiva (parámetro query): nombre por partes o completo, correo de la cuenta
     * del alumno o CURP exacta (cifrada de forma determinística, solo admite igualdad).
     */
    @EntityGraph(attributePaths = {"person", "address"})
    @Query(value = """
            SELECT s FROM Student s JOIN s.person p
            WHERE LOWER(p.firstName) LIKE :pattern
               OR LOWER(p.middleName) LIKE :pattern
               OR LOWER(p.lastName) LIKE :pattern
               OR LOWER(p.secondLastName) LIKE :pattern
               OR LOWER(CONCAT(p.firstName, ' ', p.lastName)) LIKE :pattern
               OR p.curp = :curp
               OR EXISTS (SELECT u.id FROM User u WHERE u.person = p AND LOWER(u.email) LIKE :pattern)
            """,
           countQuery = """
            SELECT COUNT(s) FROM Student s JOIN s.person p
            WHERE LOWER(p.firstName) LIKE :pattern
               OR LOWER(p.middleName) LIKE :pattern
               OR LOWER(p.lastName) LIKE :pattern
               OR LOWER(p.secondLastName) LIKE :pattern
               OR LOWER(CONCAT(p.firstName, ' ', p.lastName)) LIKE :pattern
               OR p.curp = :curp
               OR EXISTS (SELECT u.id FROM User u WHERE u.person = p AND LOWER(u.email) LIKE :pattern)
            """)
    Page<Student> search(@Param("pattern") String pattern, @Param("curp") String curp, Pageable pageable);
}
