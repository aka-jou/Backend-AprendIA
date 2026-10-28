package com.aprendia.backend.feature.curriculum.repository;

import com.aprendia.backend.feature.curriculum.entities.Workbook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkbookRepository extends JpaRepository<Workbook, Long> {

    /** Cuadernillo no eliminado, con su ruta cargada (nombreRuta). */
    @Query("""
            select w from Workbook w join fetch w.learningPath
            where w.id = :id and w.deletedAt is null
            """)
    Optional<Workbook> findActiveById(@Param("id") Long id);

    /**
     * Listado de GET /workbooks.
     * - learningPathId y status: filtros opcionales (null = sin filtro).
     * - pattern: texto ya normalizado a minúsculas y con comodines escapados con '!', o null.
     *   Se usa '!' y no '\' como carácter de escape: el analizador de HQL interpreta la barra
     *   invertida dentro de literales.
     */
    @Query("""
            select w from Workbook w join fetch w.learningPath lp
            where w.deletedAt is null
              and (:learningPathId is null or lp.id = :learningPathId)
              and (:status is null or w.status = :status)
              and (:pattern is null
                   or lower(w.name) like :pattern escape '!'
                   or lower(w.author) like :pattern escape '!'
                   or lower(w.description) like :pattern escape '!')
            order by lp.id, w.sortOrder, w.id
            """)
    List<Workbook> search(@Param("learningPathId") Long learningPathId,
                          @Param("status") Boolean status,
                          @Param("pattern") String pattern);

    /** Cuadernillos de una ruta para el árbol curricular. */
    @Query("""
            select w from Workbook w
            where w.learningPath.id = :learningPathId and w.deletedAt is null
              and (:includeInactive = true or w.status = true)
            order by w.sortOrder, w.id
            """)
    List<Workbook> findForTree(@Param("learningPathId") Long learningPathId,
                               @Param("includeInactive") boolean includeInactive);

    /** Incluye eliminados lógicamente: la FK impide borrar físicamente la ruta mientras existan. */
    boolean existsByLearningPathId(Long learningPathId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Workbook w set w.deletedAt = :now, w.deletedBy = :username, w.updatedAt = :now, w.updatedBy = :username
            where w.id = :id and w.deletedAt is null
            """)
    int softDeleteById(@Param("id") Long id, @Param("now") LocalDateTime now, @Param("username") String username);
}
