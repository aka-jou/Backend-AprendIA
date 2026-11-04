package com.aprendia.backend.feature.curriculum.repository;

import com.aprendia.backend.feature.curriculum.entities.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    @Query("""
            select e from Exercise e
            where e.reactivo.id in :reactivoIds and e.deletedAt is null
            order by e.reactivo.id, e.sortOrder, e.id
            """)
    List<Exercise> findByReactivoIds(@Param("reactivoIds") Collection<Long> reactivoIds);

    /** Solo el ejercicio vivo; la visibilidad del reactivo la comprueba el servicio. */
    @Query("select e from Exercise e join fetch e.reactivo where e.id = :id and e.deletedAt is null")
    Optional<Exercise> findActiveById(@Param("id") Long id);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Exercise e set e.deletedAt = :now, e.deletedBy = :username, e.updatedAt = :now, e.updatedBy = :username
            where e.deletedAt is null and e.reactivo.id = :reactivoId
            """)
    int softDeleteByReactivo(@Param("reactivoId") Long reactivoId, @Param("now") LocalDateTime now,
                             @Param("username") String username);
}
