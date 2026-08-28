package com.finbank.service;

import com.finbank.dto.LoginRequestDto;
import com.finbank.dto.LoginResponseDto;
import com.finbank.exception.InvalidCredentialsException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public LoginResponseDto login(LoginRequestDto request) {

        try {

            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.getEmail().trim().toLowerCase(),
                                    request.getPassword()
                            )
                    );

            String role = authentication.getAuthorities()
                    .stream()
                    .findFirst()
                    .orElseThrow(() ->
                            new InvalidCredentialsException(
                                    "User role not found"
                            )
                    )
                    .getAuthority();

            String token = jwtService.generateToken(
                    authentication.getName(),
                    role
            );

            return new LoginResponseDto(token);

        } catch (AuthenticationException ex) {

            throw new InvalidCredentialsException(
                    "Invalid email or password"
            );
        }
    }
}