package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.response.GoogleUserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class GoogleOAuthService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    public GoogleUserInfo getUserInfo(String accessToken) {
        try {
            String url = GOOGLE_USERINFO_URL + "?access_token=" + accessToken;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(), GoogleUserInfo.class);
            }

            throw new RuntimeException("Error al obtener información del usuario de Google");
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar la respuesta de Google: " + e.getMessage());
        }
    }
}
