package com.dylabs.zuko.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*")); // Permitir cualquier origen (solo para desarrollo)
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")); // métodos permitidos
        config.setAllowedHeaders(List.of("*")); // cualquier cabecera (como Authorization)
        config.setAllowCredentials(true); // necesario si usas JWT o cookies

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // se aplica a todos los endpoints

        return new CorsFilter(source); // este filtro se activa automáticamente
    }

}
