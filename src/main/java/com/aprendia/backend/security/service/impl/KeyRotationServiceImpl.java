package com.aprendia.backend.security.service.impl;

import com.aprendia.backend.security.crypto.ApiKeyHasher;
import com.aprendia.backend.security.entities.ApiKey;
import com.aprendia.backend.security.repository.ApiKeyRepository;
import com.aprendia.backend.security.service.KeyRotationService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeyRotationServiceImpl implements KeyRotationService {

    private final ApiKeyRepository apiKeyRepository;

    @Override
    // Desactivada por defecto ("-"). Se activa con API_KEY_ROTATION_CRON, ej. "0 0 0 1 1/3 ?" (trimestral).
    // Al rotar, TODAS las keys vigentes quedan obsoletas: los clientes deben recibir la nueva por FCM.
    @Scheduled(cron = "${app.security.api-key-rotation.cron:-}")
    @Transactional
    public void rotateApiKey() {
        log.info("Starting scheduled API Key rotation...");

        apiKeyRepository.findAll().forEach(key -> {
            key.setDeprecated(true);
            key.setExpiresAt(LocalDateTime.now());
            apiKeyRepository.save(key);
        });

        String newKey = UUID.randomUUID().toString().replace("-", "") + 
                        UUID.randomUUID().toString().replace("-", "");
        
        ApiKey apiKey = ApiKey.builder()
                .keyHash(ApiKeyHasher.sha256Hex(newKey))   // en BD solo el hash; la key en claro solo viaja por FCM
                .isDeprecated(false)
                .createdAt(LocalDateTime.now())
                .build();
        
        apiKeyRepository.save(apiKey);
        log.info("New API Key generated and saved to database.");

        sendNewKeyViaFCM(newKey);
    }

    private void sendNewKeyViaFCM(String newApiKey) {
        try {
            Message message = Message.builder()
                    .putData("type", "API_KEY_ROTATION")
                    .putData("new_api_key", newApiKey)
                    .setTopic("security-updates")
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent API Key rotation message to FCM: {}", response);
        } catch (Exception e) {
            log.error("Failed to send API Key via FCM: {}", e.getMessage(), e);
        }
    }
}
