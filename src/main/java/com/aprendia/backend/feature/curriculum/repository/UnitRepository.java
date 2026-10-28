package com.aprendia.backend.feature.curriculum.repository;

import com.aprendia.backend.feature.curriculum.entities.Unit;
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
public interface UnitRepository extends JpaRepository<Unit, Long> {

    @Query("""
            select u from Unit u join fetch u.workbook w
            where u.id = :id and u.deletedAt is null and w.deletedAt is null
            """)
    Optional<Unit> findActiveById(@Param("id") Long id);

    /** Listado de GET /units; workbookId opcional. Sin permisos de gestión solo se ven cuadernillos activos. */
    @Query("""
            select u from Unit u join u.workbook w
            where u.deletedAt is null and w.deletedAt is null
              and (:workbookId is null or w.id = :workbookId)
              and (:includeInactive = true or w.status = true)
            order by w.id, u.sortOrder, u.id
            """)
    List<Unit> search(@Param("workbookId") Long workbookId, @Param("includeInactive") boolean includeInactive);

    @Query("""
            select u from Unit u
            where u.workbook.id in :workbookIds and u.deletedAt is null
            order by u.sortOrder, u.id
            """)
    List<Unit> findByWorkbookIds(@Param("workbookIds") Collection<Long> workbookIds);

    /** totalUnidades por cuadernillo: [workbookId, conteo]. */
    @Query("""
            select u.workbook.id, count(u) from Unit u
            where u.workbook.id in :workbookIds and u.deletedAt is null
            group by u.workbook.id
            """)
    List<Object[]> countByWorkbookIds(@Param("workbookIds") Collection<Long> workbookIds);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Unit u set u.deletedAt = :now, u.deletedBy = :username, u.updatedAt = :now, u.updatedBy = :username
            where u.deletedAt is null and (u.id = :unitId or u.workbook.id = :workbookId)
            """)
    int softDelete(@Param("unitId") Long unitId, @Param("workbookId") Long workbookId,
                   @Param("now") LocalDateTime now, @Param("username") String username);
}
