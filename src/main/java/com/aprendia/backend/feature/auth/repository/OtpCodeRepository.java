package com.aprendia.backend.feature.auth.repository;

import com.aprendia.backend.feature.auth.entities.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    Optional<OtpCode> findFirstByEmailOrderByCreatedAtDesc(String email);

    void deleteByEmail(String email);

    void deleteByExpiresAtBefore(LocalDateTime threshold);
}
