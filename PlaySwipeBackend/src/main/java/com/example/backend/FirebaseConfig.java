package com.example.backend;

/*
 *  FirebaseConfig
 *  --------------
 *  Inicializa el SDK de Firebase Admin cuando arranca Spring Boot.
 *  La clave de la cuenta de servicio NO está en el repositorio: se indica su
 *  ruta con la variable de entorno FIREBASE_CREDENTIALS (ver README).
 */

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(FirebaseConfig.class);

    @Bean
    public FirebaseApp initFirebase(@Value("${firebase.credentials:}") String credPath) throws Exception {
        if (credPath == null || credPath.isBlank()) {
            throw new IllegalStateException(
                    "Falta FIREBASE_CREDENTIALS: la ruta al JSON de la cuenta de servicio de Firebase.");
        }
        try (InputStream serviceAccount = new FileInputStream(credPath)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            log.info("Firebase inicializado");
            return FirebaseApp.initializeApp(options);
        }
    }
}
