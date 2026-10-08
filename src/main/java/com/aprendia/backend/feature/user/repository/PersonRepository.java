package com.aprendia.backend.feature.user.repository;
import com.aprendia.backend.feature.user.entities.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    boolean existsByCurp(String curp);
    Optional<Person> findByCurp(String curp);
}
