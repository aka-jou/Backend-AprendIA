package com.aprendia.backend.feature.curriculum.repository;

import com.aprendia.backend.feature.curriculum.entities.LearningResource;
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
public interface LearningResourceRepository extends JpaRepository<LearningResource, Long> {

    @Query("""
            select r from LearningResource r join fetch r.topic t join fetch t.unit u join fetch u.workbook w
            where r.id = :id and r.deletedAt is null and t.deletedAt is null and u.deletedAt is null and w.deletedAt is null
            """)
    Optional<LearningResource> findActiveById(@Param("id") Long id);

    @Query("""
            select r from LearningResource r
            where r.topic.id = :topicId and r.deletedAt is null
            order by r.id
            """)
    List<LearningResource> findByTopicId(@Param("topicId") Long topicId);

    /** totalRecursos por tema: [topicId, conteo]. */
    @Query("""
            select r.topic.id, count(r) from LearningResource r
            where r.topic.id in :topicIds and r.deletedAt is null
            group by r.topic.id
            """)
    List<Object[]> countByTopicIds(@Param("topicIds") Collection<Long> topicIds);

    /**
     * Baja lógica de un recurso, o de los recursos de un tema, una unidad o un cuadernillo.
     * Solo uno de los cuatro identificadores viene con valor; los demás llegan en null.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update LearningResource r set r.deletedAt = :now, r.deletedBy = :username, r.updatedAt = :now, r.updatedBy = :username
            where r.deletedAt is null
              and (r.id = :resourceId
                   or r.topic.id = :topicId
                   or r.topic.id in (select t.id from Topic t where t.unit.id = :unitId)
                   or r.topic.id in (select t.id from Topic t where t.unit.workbook.id = :workbookId))
            """)
    int softDelete(@Param("resourceId") Long resourceId, @Param("topicId") Long topicId,
                   @Param("unitId") Long unitId, @Param("workbookId") Long workbookId,
                   @Param("now") LocalDateTime now, @Param("username") String username);
}
