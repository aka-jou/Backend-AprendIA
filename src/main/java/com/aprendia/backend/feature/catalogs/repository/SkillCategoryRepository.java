package com.aprendia.backend.feature.catalogs.repository;

import com.aprendia.backend.feature.catalogs.entities.SkillCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkillCategoryRepository extends JpaRepository<SkillCategory, Long> {
}
