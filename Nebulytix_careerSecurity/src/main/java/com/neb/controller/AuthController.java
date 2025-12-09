package com.neb.controller;


import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neb.dto.ResponseMessage;
import com.neb.dto.user.AuthResponse;
import com.neb.dto.user.LoginRequest;
import com.neb.service.AuthService;

@RestController
@RequestMapping("/api/auth/")
public class AuthController {

    @Autowired
    private AuthService authService;


    // LOGIN ---------------------------------------------------------------------
    @PostMapping("/login")
    public ResponseEntity<ResponseMessage<AuthResponse>> login(@RequestBody LoginRequest req) {

        try {
            AuthResponse response = authService.login(req);

            return ResponseEntity.ok(
                new ResponseMessage<>(200, "SUCCESS", "Login successful", response)
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ResponseMessage<>(401, "FAILED", e.getMessage()));
        }
    }


    // REFRESH TOKEN --------------------------------------------------------------
    @PostMapping("/refresh-token")
    public ResponseEntity<ResponseMessage<AuthResponse>> refreshToken(@RequestBody Map<String, String> request) {

        try {
            String refreshToken = request.get("refreshToken");
            AuthResponse resp = authService.refreshAccessToken(refreshToken);

            return ResponseEntity.ok(
                new ResponseMessage<>(200, "SUCCESS", "Token refreshed successfully", resp)
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ResponseMessage<>(403, "FAILED", e.getMessage()));
        }
    }


    // LOGOUT ----------------------------------------------------------------------
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public ResponseEntity<ResponseMessage<String>> logout(@RequestBody Map<String, String> request) {

        try {
            String refreshToken = request.get("refreshToken");
            String msg = authService.logout(refreshToken);

            return ResponseEntity.ok(
                new ResponseMessage<>(200, "SUCCESS", msg)
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseMessage<>(400, "FAILED", e.getMessage()));
        }
    }
}
