package com.duan.notification_service.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.core.io.ClassPathResource; // Sử dụng ClassPathResource
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() {
        try {
            // Đọc file từ thư mục resources của Project
            InputStream serviceAccount = new ClassPathResource("serviceAccountKey.json").getInputStream();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println(">>>> Firebase đã khởi tạo thành công từ Resources!");
            }
        } catch (Exception e) {
            System.err.println(">>>> LỖI KHỞI TẠO FIREBASE: " + e.getMessage());
            throw new RuntimeException("Lỗi khởi tạo Firebase: " + e.getMessage());
        }
    }
}