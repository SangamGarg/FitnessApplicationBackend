package com.fitnessapp.userDetailsAndAuthService.config.firebaseConfig;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.service.account.key.path}")
    private String firebaseServiceAccountKeyPath;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
//        String serviceAccountPath = System.getenv("FIREBASE_SERVICE_ACCOUNT_KEY_PATH");

        if (firebaseServiceAccountKeyPath == null || firebaseServiceAccountKeyPath.isEmpty()) {
            throw new IllegalArgumentException("Firebase service account key path is not set in the environment variables.");
        }

        InputStream serviceAccount = new FileInputStream(firebaseServiceAccountKeyPath);

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            log.info("✅ Firebase initialized successfully");
            return FirebaseApp.initializeApp(options);
        } else {
            log.info("ℹ️ Firebase already initialized");
            return FirebaseApp.getInstance();
        }
    }
}