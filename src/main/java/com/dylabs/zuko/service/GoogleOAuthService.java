package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.response.GoogleUserInfo;
import com.dylabs.zuko.exception.userExeptions.OAuthException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class GoogleOAuthService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    public GoogleUserInfo getUserInfo(String jwtToken) {
        try {
            // Decodificar el JWT directamente (sin llamada HTTP)
            return decodeGoogleJWT(jwtToken);
        } catch (Exception e) {
            throw new OAuthException("Error al procesar el JWT de Google: " + e.getMessage(), e);
        }
    }

    private GoogleUserInfo decodeGoogleJWT(String jwtToken) {
        try {
            // Dividir el JWT en sus 3 partes
            String[] parts = jwtToken.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("JWT inválido");
            }

            // Decodificar la parte del payload (índice 1)
            String payload = parts[1];
            byte[] decodedBytes = Base64.getUrlDecoder().decode(payload);
            String jsonPayload = new String(decodedBytes);

            // Log para debugging
            System.out.println("🔍 JWT Payload decodificado: " + jsonPayload);

            // Parsear el JSON usando Jackson
            JsonNode node = objectMapper.readTree(jsonPayload);

            // Obtener los valores de manera segura
            String sub = getTextSafely(node, "sub");
            String name = getTextSafely(node, "name");
            String givenName = getTextSafely(node, "given_name");
            String familyName = getTextSafely(node, "family_name");
            String picture = getTextSafely(node, "picture");
            String email = getTextSafely(node, "email");
            Boolean emailVerified = getBooleanSafely(node, "email_verified");
            String locale = getTextSafely(node, "locale");

            return new GoogleUserInfo(
                    sub, name, givenName, familyName,
                    picture, email, emailVerified, locale
            );

        } catch (Exception e) {
            throw new OAuthException("Error decodificando JWT de Google: " + e.getMessage(), e);
        }
    }

    // Métodos helper para manejo seguro de campos
    private String getTextSafely(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return field != null && !field.isNull() ? field.asText() : null;
    }

    private Boolean getBooleanSafely(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return field != null && !field.isNull() ? field.asBoolean() : null;
    }
}
