package com.aprendia.backend.feature.curriculum.repository;

import com.aprendia.backend.feature.curriculum.entities.Layout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LayoutRepository extends JpaRepository<Layout, Long> {

    /** GET /layouts con filtros opcionales ?idTipoLayout= y ?isActive=. */
    @Query("""
            select l from Layout l join fetch l.layoutType t
            where (:layoutTypeId is null or t.id = :layoutTypeId)
              and (:active is null or l.active = :active)
            order by l.id
            """)
    List<Layout> search(@Param("layoutTypeId") Long layoutTypeId, @Param("active") Boolean active);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    boolean existsByLayoutTypeId(Long layoutTypeId);
}
