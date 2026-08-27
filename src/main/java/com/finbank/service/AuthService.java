package com.finbank.service;

import com.finbank.dto.*;
import com.finbank.exception.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.stereotype.*;

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
                                    request.getEmail(),
                                    request.getPassword()
                            )
                    );

            String role = authentication.getAuthorities()
                    .iterator()
                    .next()
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
