package com.aprendia.backend.security.service.impl;

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
    @Scheduled(cron = "0 0 0 1 1/3 ?")
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
                .keyHash(newKey)
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
