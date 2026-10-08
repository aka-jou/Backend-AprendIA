package com.aprendia.backend.feature.auth.service.impl;

import com.aprendia.backend.feature.auth.repository.OtpCodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** Borra cada hora los códigos OTP vencidos (incluidos los señuelos) para que la tabla no crezca sin límite. */
@Configuration
@EnableScheduling
public class OtpCleanupJob {

    private static final Logger logger = LoggerFactory.getLogger(OtpCleanupJob.class);

    @Autowired
    private OtpCodeRepository otpCodeRepository;

    @Scheduled(fixedDelayString = "${app.otp.cleanup-interval-ms:3600000}", initialDelay = 60_000)
    @Transactional
    public void purgeExpired() {
        otpCodeRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        logger.debug("Limpieza de códigos OTP vencidos ejecutada.");
    }
}
