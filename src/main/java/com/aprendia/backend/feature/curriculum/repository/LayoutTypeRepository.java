package com.aprendia.backend.feature.curriculum.repository;

import com.aprendia.backend.feature.curriculum.entities.LayoutType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LayoutTypeRepository extends JpaRepository<LayoutType, Long> {

    List<LayoutType> findAllByOrderByIdAsc();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
