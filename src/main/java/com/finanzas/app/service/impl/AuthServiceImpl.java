package com.finanzas.app.service.impl;

import com.finanzas.app.domain.model.LoginAttempt;
import com.finanzas.app.domain.model.User;
import com.finanzas.app.domain.repository.LoginAttemptRepository;
import com.finanzas.app.domain.repository.UserRepository;
import com.finanzas.app.infrastructure.security.JwtUtil;
import com.finanzas.app.presentation.dto.request.LoginRequest;
import com.finanzas.app.presentation.dto.request.RegisterRequest;
import com.finanzas.app.presentation.dto.response.AuthResponse;
import com.finanzas.app.presentation.exception.AccountLockedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl {

    // HU-17: máximo de intentos fallidos antes del bloqueo
    private static final int MAX_FAILED_ATTEMPTS = 3;
    // HU-17: duración del bloqueo temporal en minutos
    private static final int LOCK_DURATION_MINUTES = 30;

    private final UserRepository userRepository;
    private final LoginAttemptRepository loginAttemptRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    // ─── HU-16: Registro de Usuario ────────────────────────────────────────────

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "Ese correo ya tiene una cuenta asociada");
        }

        var user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .build();

        var saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved.getEmail(), saved.getId());

        return AuthResponse.builder()
                .token(token)
                .email(saved.getEmail())
                .fullName(saved.getFullName())
                .message("Registro exitoso")
                .build();
    }

    // ─── HU-17: Autenticación ──────────────────────────────────────────────────

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Verificar si la cuenta está bloqueada
        checkIfBlocked(request.getEmail());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(), request.getPassword()));

            // Login exitoso — limpiar intentos fallidos
            resetFailedAttempts(request.getEmail());

            var user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow();

            String token = jwtUtil.generateToken(user.getEmail(), user.getId());

            return AuthResponse.builder()
                    .token(token)
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .build();

        } catch (BadCredentialsException ex) {
            // Registrar intento fallido
            registerFailedAttempt(request.getEmail());
            throw new BadCredentialsException("Credenciales inválidas");
        }
    }

    // ─── Métodos privados para manejo de bloqueo ──────────────────────────────

    private void checkIfBlocked(String email) {
        loginAttemptRepository.findByEmail(email).ifPresent(attempt -> {
            if (attempt.getBlockedUntil() != null &&
                    attempt.getBlockedUntil().isAfter(LocalDateTime.now())) {
                throw new AccountLockedException(
                        "Bloqueo temporal de 30 minutos por credenciales incorrectas");
            }
        });
    }

    private void registerFailedAttempt(String email) {
        var attempt = loginAttemptRepository.findByEmail(email)
                .orElse(LoginAttempt.builder()
                        .email(email)
                        .failedAttempts(0)
                        .build());

        attempt.setFailedAttempts(attempt.getFailedAttempts() + 1);
        attempt.setLastAttemptAt(LocalDateTime.now());

        if (attempt.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
            attempt.setBlockedUntil(
                    LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
        }

        loginAttemptRepository.save(attempt);
    }

    private void resetFailedAttempts(String email) {
        loginAttemptRepository.findByEmail(email).ifPresent(attempt -> {
            attempt.setFailedAttempts(0);
            attempt.setBlockedUntil(null);
            loginAttemptRepository.save(attempt);
        });
    }
}