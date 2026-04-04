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

    private static final int MAX_FAILED_ATTEMPTS  = 3;
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
            throw new IllegalArgumentException("Ese correo ya tiene una cuenta asociada");
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
        checkIfBlocked(request.getEmail());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(), request.getPassword()));

            resetFailedAttempts(request.getEmail());

            var user = userRepository.findByEmail(request.getEmail()).orElseThrow();
            String token = jwtUtil.generateToken(user.getEmail(), user.getId());

            return AuthResponse.builder()
                    .token(token)
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .build();

        } catch (BadCredentialsException ex) {
            registerFailedAttempt(request.getEmail());
            throw new BadCredentialsException("Credenciales inválidas");
        }
    }

    // ─── Métodos privados para manejo de bloqueo ──────────────────────────────

    private void checkIfBlocked(String email) {
        loginAttemptRepository.findByEmail(email).ifPresent(attempt -> {
            if (attempt.getLockedUntil() != null &&             // ← antes: getBlockedUntil()
                    attempt.getLockedUntil().isAfter(LocalDateTime.now())) {
                throw new AccountLockedException(
                        "Bloqueo temporal de 30 minutos por credenciales incorrectas");
            }
        });
    }

    private void registerFailedAttempt(String email) {
        var attempt = loginAttemptRepository.findByEmail(email)
        .orElseGet(() -> LoginAttempt.builder()
                .email(email)
                .attempts(0)
                .build());

        attempt.setAttempts(attempt.getAttempts() + 1);         // ← antes: setFailedAttempts
        attempt.setLastAttempt(LocalDateTime.now());             // ← antes: setLastAttemptAt

        if (attempt.getAttempts() >= MAX_FAILED_ATTEMPTS) {     // ← antes: getFailedAttempts
            attempt.setLockedUntil(                             // ← antes: setBlockedUntil
                    LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
        }

        loginAttemptRepository.save(attempt);
    }

    private void resetFailedAttempts(String email) {
        loginAttemptRepository.findByEmail(email).ifPresent(attempt -> {
            attempt.setAttempts(0);                             // ← antes: setFailedAttempts
            attempt.setLockedUntil(null);                       // ← antes: setBlockedUntil
            loginAttemptRepository.save(attempt);
        });
    }
}