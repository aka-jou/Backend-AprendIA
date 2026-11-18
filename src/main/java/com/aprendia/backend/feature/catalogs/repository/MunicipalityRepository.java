package com.aprendia.backend.feature.catalogs.repository;

import com.aprendia.backend.feature.catalogs.entities.Municipality;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MunicipalityRepository extends JpaRepository<Municipality, Long> {
    Page<Municipality> findByStateId(Long stateId, Pageable pageable);

    Page<Municipality> findByActiveTrue(Pageable pageable);

    Page<Municipality> findByStateIdAndActiveTrue(Long stateId, Pageable pageable);

    /** ¿Lo usa algún domicilio (de alumno o de persona)? */
    @Query(value = """
            select exists (select 1 from addresses where municipality_id = :id)
                or exists (select 1 from persons where address_municipality_id = :id)
            """, nativeQuery = true)
    boolean isInUse(@Param("id") Long id);
}
