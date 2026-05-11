package com.finanzas.app.infrastructure.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filtro de rate limiting por IP usando Bucket4j (token bucket algorithm).
 *
 * Límites configurados:
 *  - Endpoints de auth (/api/v1/auth/**): 10 peticiones / minuto
 *  - Resto de la API:                    100 peticiones / minuto
 *
 * El bucket se almacena en memoria (ConcurrentHashMap por IP).
 * En producción con múltiples instancias se reemplazaría por Redis.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    // Cache de buckets por IP
    private final Map<String, Bucket> authBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> apiBuckets  = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = resolveClientIp(request);
        String path     = request.getRequestURI();

        Bucket bucket = path.startsWith("/api/v1/auth")
                ? authBuckets.computeIfAbsent(clientIp, k -> buildAuthBucket())
                : apiBuckets.computeIfAbsent(clientIp, k -> buildApiBucket());

        if (bucket.tryConsume(1)) {
            // Agregar headers informativos del rate limit
            long remaining = bucket.getAvailableTokens();
            response.addHeader("X-RateLimit-Remaining", String.valueOf(remaining));
            filterChain.doFilter(request, response);
        } else {
            // Límite excedido — retornar 429 Too Many Requests
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(String.format(
                    "{\"timestamp\":\"%s\",\"status\":429,\"error\":\"Demasiadas peticiones. Intente nuevamente en un minuto.\"}",
                    LocalDateTime.now()
            ));
        }
    }

    /**
     * Bucket para endpoints de autenticación — más restrictivo.
     * 10 tokens, se recarga 10 por minuto.
     */
    private Bucket buildAuthBucket() {
        Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Bucket para el resto de la API — más permisivo.
     * 100 tokens, se recarga 100 por minuto.
     */
    private Bucket buildApiBucket() {
        Bandwidth limit = Bandwidth.classic(100, Refill.greedy(100, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Resuelve la IP real del cliente considerando proxies (Render usa proxy reverso).
     */
    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            // X-Forwarded-For puede contener múltiples IPs — tomar la primera (cliente real)
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    // Solo aplicar a rutas /api/** — ignorar Swagger, actuator, etc.
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/");
    }
}