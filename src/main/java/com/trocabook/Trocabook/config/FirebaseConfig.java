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

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {
    @Value("${firebase.credentials.path}")
    private String caminhoCredencial;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {

        try (FileInputStream serviceAccount =
                     new FileInputStream(caminhoCredencial)) {

            FirebaseOptions firebaseOptions =
                    FirebaseOptions.builder()
                            .setCredentials(
                                    GoogleCredentials.fromStream(serviceAccount)
                            )
                            .build();

            if (!FirebaseApp.getApps().isEmpty()) {
                return FirebaseApp.getInstance();
            }

            return FirebaseApp.initializeApp(firebaseOptions);
        }
    }

    @Bean
    public Firestore firestore(FirebaseApp firebaseApp){
        return FirestoreClient.getFirestore(firebaseApp);
    }

    @Bean
    public FirebaseAuth firebaseAuth(FirebaseApp firebaseApp){
        return FirebaseAuth.getInstance(firebaseApp);
    }

}
