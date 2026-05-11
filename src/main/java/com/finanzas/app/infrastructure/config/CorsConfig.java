package com.finanzas.app.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    // Dominios permitidos — configurables por variable de entorno
    @Value("${app.cors.allowed-origins:http://localhost:9000,http://localhost:3000}")
    private List<String> allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        var config = new CorsConfiguration();

        // Solo orígenes explícitamente permitidos — sin comodín *
        config.setAllowedOrigins(allowedOrigins);

        // Solo headers necesarios para JWT
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With"
        ));

        // Solo métodos HTTP necesarios
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Exponer el header Authorization para que el frontend pueda leerlo
        config.setExposedHeaders(List.of("Authorization"));

        // Credenciales permitidas (necesario para JWT con cookies o headers)
        config.setAllowCredentials(true);

        // Cache del preflight por 1 hora
        config.setMaxAge(3600L);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }
}