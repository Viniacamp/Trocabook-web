package com.trocabook.Trocabook.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Value("${FIREBASE_CREDENTIALS_PATH:${firebase.credentials.path:}}")
    private String caminhoCredencial;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        InputStream serviceAccount;

        if (caminhoCredencial != null && !caminhoCredencial.isBlank()) {
            if (caminhoCredencial.startsWith("classpath:")) {
                // Remove o "classpath:" e qualquer barra inicial solta
                String pathLimpo = caminhoCredencial.replace("classpath:", "");
                if (pathLimpo.startsWith("/")) {
                    pathLimpo = pathLimpo.substring(1);
                }
                // Agora vira "credentials/trocabook-92297-firebase-adminsdk-fbsvc-d737f4aa9e.json"
                serviceAccount = new ClassPathResource(pathLimpo).getInputStream();
            } else {
                serviceAccount = new FileInputStream(caminhoCredencial);
            }
        } else {
            // Fallback caso a variável não venha preenchida
            serviceAccount = new ClassPathResource("credentials/trocabook-92297-firebase-adminsdk-fbsvc-d737f4aa9e.json").getInputStream();
        }

        try (serviceAccount) {
            FirebaseOptions firebaseOptions = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (!FirebaseApp.getApps().isEmpty()) {
                return FirebaseApp.getInstance();
            }

            return FirebaseApp.initializeApp(firebaseOptions);
        }
    }

    @Bean
    public Firestore firestore(FirebaseApp firebaseApp) {
        return FirestoreClient.getFirestore(firebaseApp);
    }

    @Bean
    public FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
        return FirebaseAuth.getInstance(firebaseApp);
    }
}