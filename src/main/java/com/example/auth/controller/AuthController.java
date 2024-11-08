// AuthController.java
package com.example.auth.controller;

import com.example.auth.dto.LoginRequest;
import com.example.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Mono<ResponseEntity<Void>> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        return authService.login(loginRequest, response)
                .thenReturn(ResponseEntity.ok().<Void>build())
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).<Void>build()));
    }

    @PostMapping("/token/refresh")
    public Mono<ResponseEntity<Void>> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        return authService.refreshAccessToken(request, response)
                .thenReturn(ResponseEntity.ok().<Void>build())
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).<Void>build()));
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(HttpServletRequest request, HttpServletResponse response) {
        return authService.logout(request, response)
                .thenReturn(ResponseEntity.ok().<Void>build());
    }
}