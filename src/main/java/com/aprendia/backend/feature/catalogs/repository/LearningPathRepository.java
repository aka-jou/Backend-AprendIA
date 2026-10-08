package com.aprendia.backend.feature.catalogs.repository;

import com.aprendia.backend.feature.catalogs.entities.LearningPath;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningPathRepository extends JpaRepository<LearningPath, Long> {

    // Carga la metodología en la misma consulta (evita N+1 al armar nombreMetodologia)
    @EntityGraph(attributePaths = "metodology")
    List<LearningPath> findAllByOrderByIdAsc();

    @NonNull
    @EntityGraph(attributePaths = "metodology")
    Optional<LearningPath> findById(@NonNull Long id);

    boolean existsByMetodologyId(Long metodologyId);
}
