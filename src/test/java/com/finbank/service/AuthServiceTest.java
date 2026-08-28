package com.finbank.service;

import com.finbank.dto.*;
import com.finbank.exception.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private Authentication authentication;

    @InjectMocks private AuthService authService;

    @Test
    void login_shouldAuthenticateAndReturnJwt() {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail(" ANIKET@EXAMPLE.COM ");
        request.setPassword("password123");

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(authentication.getName())
                .thenReturn("aniket@example.com");

        doReturn(List.of(
                new SimpleGrantedAuthority("ROLE_CUSTOMER")
        )).when(authentication).getAuthorities();

        when(jwtService.generateToken(
                "aniket@example.com",
                "ROLE_CUSTOMER"
        )).thenReturn("jwt-token");

        LoginResponseDto result = authService.login(request);

        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());

        verify(authenticationManager).authenticate(any());

        verify(jwtService).generateToken(
                "aniket@example.com",
                "ROLE_CUSTOMER"
        );
    }

    @Test
    void login_shouldRejectInvalidCredentials() {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("aniket@example.com");
        request.setPassword("wrong-password");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(request));

        verifyNoInteractions(jwtService);
    }

    @Test
    void login_shouldRejectAuthenticationWithoutRole() {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("aniket@example.com");
        request.setPassword("password123");

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getAuthorities()).thenReturn(List.of());

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(request));

        verifyNoInteractions(jwtService);
    }
}
