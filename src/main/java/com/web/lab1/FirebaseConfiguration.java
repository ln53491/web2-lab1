package com.web.lab1;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Configuration
public class FirebaseConfiguration {

    @Value("${firebase.database.url}")
    private String databaseUrl;

    @Value("${key.for.firebase}")
    private String keyPath;

    @Bean
    public FirebaseApp initializeFirebaseApp() throws IOException {
//        FileInputStream serviceAccount = new FileInputStream(keyPath);
        InputStream serviceAccount = new URL(keyPath).openStream();

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl(databaseUrl)
                .build();

        return FirebaseApp.initializeApp(options);
    }
}

