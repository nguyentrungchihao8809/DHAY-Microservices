package com.duan.hday.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import java.io.FileInputStream;
import java.io.IOException;

@Configuration
@Slf4j
public class FirebaseConfig {

    // Spring sẽ tự tìm biến môi trường FIREBASE_KEY_PATH từ Docker Compose
    @Value("${FIREBASE_KEY_PATH}")
    private String firebaseConfigPath;

    @PostConstruct
    public void init() {
        try {
            // Vì chạy trong Docker, file đã là một File thật trên hệ thống (FileSystem)
            FileInputStream serviceAccount = new FileInputStream(firebaseConfigPath);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info(">>>> Firebase initialized successfully from: {}", firebaseConfigPath);
            }
        } catch (IOException e) {
            log.error(">>>> Error initializing Firebase: {}", e.getMessage());
        }
    }
}
