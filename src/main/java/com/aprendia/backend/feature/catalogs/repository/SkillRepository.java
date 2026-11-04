package com.aprendia.backend.feature.catalogs.repository;

import com.aprendia.backend.feature.catalogs.entities.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    /** GET /catalogs/skills con filtro opcional ?status=. */
    @Query("""
            select s from Skill s left join fetch s.category
            where (:status is null or s.status = :status)
            order by s.id
            """)
    List<Skill> search(@Param("status") Boolean status);

    /** Resuelve previousSkills, que llega por nombre: comparación sin mayúsculas ni espacios extremos. */
    @Query("select s from Skill s where lower(s.name) in :names")
    List<Skill> findByLowerNames(@Param("names") Collection<String> names);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    /** ¿Algún reactivo (aunque esté dado de baja) usa la habilidad? Las llaves foráneas impiden borrarla. */
    @Query(value = """
            select exists (select 1 from reactivo_skills where skill_id = :id)
                or exists (select 1 from reactivo_previous_skills where skill_id = :id)
            """, nativeQuery = true)
    boolean isUsedByReactivos(@Param("id") Long id);
}
