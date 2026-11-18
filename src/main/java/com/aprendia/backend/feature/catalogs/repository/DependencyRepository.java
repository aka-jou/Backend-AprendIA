package com.aprendia.backend.feature.catalogs.repository;

import com.aprendia.backend.feature.catalogs.entities.Dependency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DependencyRepository extends JpaRepository<Dependency, Long> {

    Optional<Dependency> findFirstByNameIgnoreCase(String name);

    Page<Dependency> findByStatusTrue(Pageable pageable);

    /** ¿Algún usuario está adscrito a la dependencia? La FK pondría su dependencia en NULL al borrarla. */
    @Query(value = "select exists (select 1 from users where dependency_id = :id)", nativeQuery = true)
    boolean hasUsers(@Param("id") Long id);
}
