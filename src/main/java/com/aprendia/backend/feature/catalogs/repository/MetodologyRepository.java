package com.aprendia.backend.feature.catalogs.repository;

import com.aprendia.backend.feature.catalogs.entities.Metodology;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetodologyRepository extends JpaRepository<Metodology, Long> {
    List<Metodology> findAllByOrderByIdAsc();
}
