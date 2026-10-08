package com.aprendia.backend.feature.user.repository;
import com.aprendia.backend.feature.user.entities.RelativeRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RelativeRoleRepository extends JpaRepository<RelativeRole, Integer> {
    Optional<RelativeRole> findByName(String name);
}
