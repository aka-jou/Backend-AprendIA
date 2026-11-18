package com.aprendia.backend.feature.catalogs.repository;

import com.aprendia.backend.feature.catalogs.entities.State;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StateRepository extends JpaRepository<State, Long> {

    Page<State> findByActiveTrue(Pageable pageable);

    /** ¿Lo usan municipios o domicilios (de alumnos o de personas)? */
    @Query(value = """
            select exists (select 1 from municipalities where state_id = :id)
                or exists (select 1 from addresses where state_id = :id)
                or exists (select 1 from persons where address_state_id = :id)
            """, nativeQuery = true)
    boolean isInUse(@Param("id") Long id);
}
