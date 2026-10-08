package com.aprendia.backend.feature.user.repository;

import com.aprendia.backend.feature.user.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByUsernameIgnoreCase(String username);
    Optional<User> findByQrCode(String qrCode);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCase(String username);
    Optional<User> findByPersonId(Long personId);
    List<User> findByPersonIdIn(Collection<Long> personIds);

    /** Personal del sistema: todos los usuarios que NO tienen el rol de estudiante. */
    @Query(value = """
            SELECT u FROM User u
            WHERE NOT EXISTS (SELECT r.id FROM User s JOIN s.roles r WHERE s.id = u.id AND r.name = :studentRole)
            """,
           countQuery = """
            SELECT COUNT(u) FROM User u
            WHERE NOT EXISTS (SELECT r.id FROM User s JOIN s.roles r WHERE s.id = u.id AND r.name = :studentRole)
            """)
    Page<User> findStaff(@Param("studentRole") String studentRole, Pageable pageable);

    /**
     * Búsqueda reactiva del personal (parámetro query): usuario, correo, nombre (por partes o completo)
     * o CURP exacta. La CURP está cifrada de forma determinística, por eso solo admite igualdad;
     * Hibernate aplica el conversor al parámetro :curp automáticamente.
     */
    @Query(value = """
            SELECT u FROM User u LEFT JOIN u.person p
            WHERE NOT EXISTS (SELECT r.id FROM User s JOIN s.roles r WHERE s.id = u.id AND r.name = :studentRole)
              AND (LOWER(u.username) LIKE :pattern
                OR LOWER(u.email) LIKE :pattern
                OR LOWER(p.firstName) LIKE :pattern
                OR LOWER(p.middleName) LIKE :pattern
                OR LOWER(p.lastName) LIKE :pattern
                OR LOWER(p.secondLastName) LIKE :pattern
                OR LOWER(CONCAT(p.firstName, ' ', p.lastName)) LIKE :pattern
                OR p.curp = :curp)
            """,
           countQuery = """
            SELECT COUNT(u) FROM User u LEFT JOIN u.person p
            WHERE NOT EXISTS (SELECT r.id FROM User s JOIN s.roles r WHERE s.id = u.id AND r.name = :studentRole)
              AND (LOWER(u.username) LIKE :pattern
                OR LOWER(u.email) LIKE :pattern
                OR LOWER(p.firstName) LIKE :pattern
                OR LOWER(p.middleName) LIKE :pattern
                OR LOWER(p.lastName) LIKE :pattern
                OR LOWER(p.secondLastName) LIKE :pattern
                OR LOWER(CONCAT(p.firstName, ' ', p.lastName)) LIKE :pattern
                OR p.curp = :curp)
            """)
    Page<User> searchStaff(@Param("studentRole") String studentRole,
                           @Param("pattern") String pattern,
                           @Param("curp") String curp,
                           Pageable pageable);
}
