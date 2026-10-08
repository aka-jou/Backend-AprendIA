package com.aprendia.backend.feature.user.repository;
import com.aprendia.backend.feature.user.entities.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.lang.NonNull;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    @NonNull
    @EntityGraph(attributePaths = {"person", "address"})
    Page<Student> findAll(@NonNull Pageable pageable);

    @NonNull
    @EntityGraph(attributePaths = {"person", "address"})
    java.util.Optional<Student> findById(@NonNull Long id);

    java.util.Optional<Student> findByPersonId(Long personId);
}
