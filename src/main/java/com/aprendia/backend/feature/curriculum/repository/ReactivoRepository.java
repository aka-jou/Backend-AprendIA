package com.aprendia.backend.feature.curriculum.repository;

import com.aprendia.backend.feature.curriculum.entities.Reactivo;
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
public interface ReactivoRepository extends JpaRepository<Reactivo, Long> {

    @Query("""
            select r from Reactivo r join fetch r.layout join fetch r.topic t join fetch t.unit u join fetch u.workbook w
            where r.id = :id and r.deletedAt is null and t.deletedAt is null and u.deletedAt is null and w.deletedAt is null
            """)
    Optional<Reactivo> findActiveById(@Param("id") Long id);

    /**
     * GET /reactivos; topicId opcional. Sin permisos de gestión solo se ven reactivos con
     * status = active de cuadernillos activos.
     */
    @Query("""
            select r from Reactivo r join fetch r.layout join fetch r.topic t join t.unit u join u.workbook w
            where r.deletedAt is null and t.deletedAt is null and u.deletedAt is null and w.deletedAt is null
              and (:topicId is null or t.id = :topicId)
              and (:canManage = true or (w.status = true and r.status = 'active'))
            order by t.id, r.id
            """)
    List<Reactivo> search(@Param("topicId") Long topicId, @Param("canManage") boolean canManage);

    /** totalReactivos por tema: [topicId, conteo]. Sin permisos de gestión solo cuentan los activos. */
    @Query("""
            select r.topic.id, count(r) from Reactivo r
            where r.topic.id in :topicIds and r.deletedAt is null
              and (:canManage = true or r.status = 'active')
            group by r.topic.id
            """)
    List<Object[]> countByTopicIds(@Param("topicIds") Collection<Long> topicIds, @Param("canManage") boolean canManage);

    boolean existsByLayoutId(Long layoutId);

    /**
     * Baja lógica de un reactivo, o de los reactivos de un tema, una unidad o un cuadernillo.
     * Solo uno de los cuatro identificadores viene con valor; los demás llegan en null.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Reactivo r set r.deletedAt = :now, r.deletedBy = :username, r.updatedAt = :now, r.updatedBy = :username
            where r.deletedAt is null
              and (r.id = :reactivoId
                   or r.topic.id = :topicId
                   or r.topic.id in (select t.id from Topic t where t.unit.id = :unitId)
                   or r.topic.id in (select t.id from Topic t where t.unit.workbook.id = :workbookId))
            """)
    int softDelete(@Param("reactivoId") Long reactivoId, @Param("topicId") Long topicId,
                   @Param("unitId") Long unitId, @Param("workbookId") Long workbookId,
                   @Param("now") LocalDateTime now, @Param("username") String username);
}
