package com.finanzas.app.service;

import com.finanzas.app.domain.model.LoginAttempt;
import com.finanzas.app.domain.model.User;
import com.finanzas.app.domain.repository.LoginAttemptRepository;
import com.finanzas.app.domain.repository.UserRepository;
import com.finanzas.app.infrastructure.security.JwtUtil;
import com.finanzas.app.presentation.dto.request.LoginRequest;
import com.finanzas.app.presentation.dto.request.RegisterRequest;
import com.finanzas.app.presentation.exception.AccountLockedException;
import com.finanzas.app.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private LoginAttemptRepository loginAttemptRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFullName("Jonathan Test");
        registerRequest.setEmail("jonathan@test.com");
        registerRequest.setPassword("Passw0rd!");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("jonathan@test.com");
        loginRequest.setPassword("Passw0rd!");

        testUser = User.builder()
                .id(1L)
                .email("jonathan@test.com")
                .fullName("Jonathan Test")
                .password("encoded")
                .build();
    }

    // ─── HU-16: Registro de Usuario ──────────────────────────────────────────

    @Test
    void register_shouldReturnSuccessMessage_whenDataIsValid() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(testUser);
        when(jwtUtil.generateToken(any(), any())).thenReturn("token123");

        var response = authService.register(registerRequest);

        assertThat(response.getMessage()).isEqualTo("Registro exitoso");
        assertThat(response.getToken()).isEqualTo("token123");
        assertThat(response.getEmail()).isEqualTo("jonathan@test.com");
    }

    @Test
    void register_shouldFail_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail("jonathan@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya tiene una cuenta asociada");
    }

    // ─── HU-17: Autenticación ────────────────────────────────────────────────

    @Test
    void login_shouldReturnToken_whenCredentialsAreCorrect() {
        when(loginAttemptRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateToken(any(), any())).thenReturn("token123");

        var response = authService.login(loginRequest);

        assertThat(response.getToken()).isEqualTo("token123");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_shouldThrowBadCredentials_whenPasswordIsWrong() {
        when(loginAttemptRepository.findByEmail(any())).thenReturn(Optional.empty());
        doThrow(new BadCredentialsException("bad"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Credenciales inválidas");
    }

    @Test
    void login_shouldThrowAccountLocked_whenExceeds3FailedAttempts() {
        var blockedAttempt = LoginAttempt.builder()
                .email("jonathan@test.com")
                .failedAttempts(3)
                .blockedUntil(LocalDateTime.now().plusMinutes(25))
                .build();

        when(loginAttemptRepository.findByEmail("jonathan@test.com"))
                .thenReturn(Optional.of(blockedAttempt));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(AccountLockedException.class)
                .hasMessageContaining("Bloqueo temporal de 30 minutos");
    }

    @Test
    void login_shouldBlockAccount_afterThirdFailedAttempt() {
        var attempt = LoginAttempt.builder()
                .email("jonathan@test.com")
                .failedAttempts(2)
                .build();

        when(loginAttemptRepository.findByEmail(any())).thenReturn(Optional.of(attempt));
        doThrow(new BadCredentialsException("bad"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);

        // Verificar que se guardó el bloqueo
        verify(loginAttemptRepository).save(argThat(a ->
                a.getBlockedUntil() != null && a.getFailedAttempts() == 3));
    }
}