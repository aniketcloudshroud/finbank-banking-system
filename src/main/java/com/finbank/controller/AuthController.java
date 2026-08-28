package com.finbank.controller;

import com.finbank.dto.LoginRequestDto;
import com.finbank.dto.LoginResponseDto;
import com.finbank.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(
            AuthService authService
    ) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponseDto login(
            @Valid @RequestBody LoginRequestDto request
    ) {

        return authService.login(request);
    }
}