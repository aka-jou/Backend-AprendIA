package com.aprendia.backend.feature.catalogs.repository;

import com.aprendia.backend.feature.catalogs.entities.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(String name);

    Page<Role> findByStatusTrue(Pageable pageable);

    /** ¿Algún usuario tiene asignado el rol? user_roles borra en cascada, por eso se revisa antes de eliminar. */
    @Query(value = "select exists (select 1 from user_roles where role_id = :id)", nativeQuery = true)
    boolean isAssigned(@Param("id") Integer id);
}
