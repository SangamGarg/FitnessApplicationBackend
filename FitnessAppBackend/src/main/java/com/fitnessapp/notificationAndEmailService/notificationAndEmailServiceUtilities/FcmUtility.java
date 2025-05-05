package com.fitnessapp.notificationAndEmailService.notificationAndEmailServiceUtilities;

import com.fitnessapp.notificationAndEmailService.models.dtos.notificationDtos.request.SendNotificationRequestDto;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public final class FcmUtility {


    @Value("${firebase.service.account.key.path}")
    private String firebaseServiceAccountKeyPath;

    @Value("${firebase.project.id}")
    private String projectId;


    public String getAccessToken() throws IOException {

        if (firebaseServiceAccountKeyPath == null || firebaseServiceAccountKeyPath.isEmpty()) {
            throw new IllegalArgumentException("Firebase service account key path is not set in the environment variables.");
        }

        InputStream serviceAccount = new FileInputStream(firebaseServiceAccountKeyPath);

        final List<String> SCOPES = List.of(
                "https://www.googleapis.com/auth/firebase.messaging"
        );
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(serviceAccount)
                .createScoped(SCOPES);
        googleCredentials.refresh();
        return googleCredentials.getAccessToken().getTokenValue();
    }


    public ResponseEntity<?> sendNotificationWithRestClient(SendNotificationRequestDto requestDto) {
        String url = "https://fcm.googleapis.com/v1/projects/" + projectId + "/messages:send";

        RestClient restClient = null;
        try {
            restClient = RestClient.builder()
                    .baseUrl(url)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();


            // Build payload dynamically from SendNotificationRequestDto
            Map<String, Object> payload = Map.of(
                    "message", Map.of(
                            "token", requestDto.getDeviceGroupKey(), // Use deviceGroupKey from requestDto
                            "notification", Map.of(
                                    "title", requestDto.getTitle(), // From the DTO
                                    "body", requestDto.getBody(),   // From the DTO
                                    "image", requestDto.getImageUrl() // From the DTO
                            ),
                            "data", requestDto.getData() // Custom data from DTO
                    )
            );

            return restClient.post()
                    .body(payload)
                    .retrieve()
                    .toEntity(String.class)
                    ;
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Sending Message Failed" + e.getLocalizedMessage());
        }
    }
}