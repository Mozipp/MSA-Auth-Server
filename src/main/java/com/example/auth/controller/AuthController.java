// AuthController.java
package com.example.auth.controller;

import com.example.auth.dto.AuthResponseDto;
import com.example.auth.dto.LoginRequest;
import com.example.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

//    @PostMapping("/login")
//    public ResponseEntity<Void> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
//        try {
//            authService.login(loginRequest, response);
//            return ResponseEntity.ok().build();
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        }
//    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        try {
            AuthResponseDto authResponse = authService.login(loginRequest, response);
            return ResponseEntity.ok(authResponse);  // 응답 바디로 토큰 정보 전달
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<Void> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        try {
            authService.refreshAccessToken(request, response);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.ok().build();
    }
}