package com.aprendia.backend.feature.curriculum.repository;

import com.aprendia.backend.feature.curriculum.entities.Topic;
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
public interface TopicRepository extends JpaRepository<Topic, Long> {

    @Query("""
            select t from Topic t join fetch t.unit u join fetch u.workbook w
            where t.id = :id and t.deletedAt is null and u.deletedAt is null and w.deletedAt is null
            """)
    Optional<Topic> findActiveById(@Param("id") Long id);

    /** Listado de GET /topics; unitId opcional. Sin permisos de gestión solo se ven cuadernillos activos. */
    @Query("""
            select t from Topic t join t.unit u join u.workbook w
            where t.deletedAt is null and u.deletedAt is null and w.deletedAt is null
              and (:unitId is null or u.id = :unitId)
              and (:includeInactive = true or w.status = true)
            order by u.id, t.sortOrder, t.id
            """)
    List<Topic> search(@Param("unitId") Long unitId, @Param("includeInactive") boolean includeInactive);

    @Query("""
            select t from Topic t
            where t.unit.id in :unitIds and t.deletedAt is null
            order by t.sortOrder, t.id
            """)
    List<Topic> findByUnitIds(@Param("unitIds") Collection<Long> unitIds);

    /** totalTemas por unidad: [unitId, conteo]. */
    @Query("""
            select t.unit.id, count(t) from Topic t
            where t.unit.id in :unitIds and t.deletedAt is null
            group by t.unit.id
            """)
    List<Object[]> countByUnitIds(@Param("unitIds") Collection<Long> unitIds);

    /**
     * Baja lógica de un tema, de los temas de una unidad o de los temas de un cuadernillo.
     * Solo uno de los tres identificadores viene con valor; los demás llegan en null.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Topic t set t.deletedAt = :now, t.deletedBy = :username, t.updatedAt = :now, t.updatedBy = :username
            where t.deletedAt is null
              and (t.id = :topicId
                   or t.unit.id = :unitId
                   or t.unit.id in (select u.id from Unit u where u.workbook.id = :workbookId))
            """)
    int softDelete(@Param("topicId") Long topicId, @Param("unitId") Long unitId, @Param("workbookId") Long workbookId,
                   @Param("now") LocalDateTime now, @Param("username") String username);
}
