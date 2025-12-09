package com.neb.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.neb.constants.Role;
import com.neb.dto.user.AuthResponse;
import com.neb.dto.user.LoginRequest;
import com.neb.entity.RefreshToken;
import com.neb.entity.Users;
import com.neb.repo.UsersRepository;
import com.neb.util.AuthUtils;

import jakarta.transaction.Transactional;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenService refreshTokenService;


    // LOGIN --------------------------------------------------------------------
    public AuthResponse login(LoginRequest req) {

        // 1. Validate credentials using AuthenticationManager
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        if (!auth.isAuthenticated()) {
            throw new RuntimeException("Invalid credentials");
        }

        // 2. Fetch user from DB
        Users user = usersRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Convert roles to string set
        Set<String> roles = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        // 4. Create access token
        String accessToken = jwtService.generateToken(user.getEmail());

        // 5. Create refresh token object
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        // 6. Prepare response
        AuthResponse resp = new AuthResponse();
        resp.setAccessToken(accessToken);
        resp.setRefreshToken(refreshToken.getToken());
        resp.setRoles(roles);
        resp.setDashboard(getDashboardForUser(user));

        return resp;
    }


    // REFRESH ACCESS TOKEN ------------------------------------------------------
    public AuthResponse refreshAccessToken(String requestRefreshToken) {

        RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken);

        if (refreshToken == null) {
            throw new RuntimeException("Invalid refresh token!");
        }

        if (refreshTokenService.isTokenExpired(refreshToken)) {
            throw new RuntimeException("Refresh token expired! Please login again.");
        }

        // Create new access token
        String newAccessToken = jwtService.generateToken(refreshToken.getUser().getEmail());

        AuthResponse resp = new AuthResponse();
        resp.setAccessToken(newAccessToken);
        resp.setRefreshToken(requestRefreshToken);

        // roles & dashboard also needed on frontend when refreshing
        Users user = refreshToken.getUser();
        Set<String> roles = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        resp.setRoles(roles);
        resp.setDashboard(getDashboardForUser(user));

        return resp;
    }


    // LOGOUT ---------------------------------------------------------------------

    @Transactional
    public String logout(String refreshTokenStr) {
    	
        if (refreshTokenStr == null || refreshTokenStr.isBlank()) {
            throw new IllegalArgumentException("refreshToken is required");
        }

        RefreshToken tokenEntity = refreshTokenService.findByToken(refreshTokenStr);
        if (tokenEntity == null) {
            throw new RuntimeException("Invalid refresh token");
        }

        // Ensure the current authenticated user is the owner of the refresh token
        String currentEmail = AuthUtils.getCurrentUserEmail();
        if (currentEmail == null) {
            throw new RuntimeException("User not authenticated");
        }

        Users tokenOwner = tokenEntity.getUser();
        if (!currentEmail.equals(tokenOwner.getEmail())) {
            // If you want to be strict: throw an error
            throw new RuntimeException("You are not authorized to logout this token");
        }

        // Delete refresh tokens for this user (logout all sessions for this user).
        refreshTokenService.deleteByUser(tokenOwner);

        return "Logout successful";
    }


    // DASHBOARD DECIDER ----------------------------------------------------------
    private String getDashboardForUser(Users user) {

        if (user.getRoles().contains(Role.ROLE_ADMIN)) return "ADMIN_DASHBOARD";
        if (user.getRoles().contains(Role.ROLE_MANAGER)) return "MANAGER_DASHBOARD";
        if (user.getRoles().contains(Role.ROLE_HR)) return "HR_DASHBOARD";
        if (user.getRoles().contains(Role.ROLE_EMPLOYEE)) return "EMPLOYEE_DASHBOARD";
        if (user.getRoles().contains(Role.ROLE_CLIENT)) return "CLIENT_DASHBOARD";

        return "DEFAULT_DASHBOARD";
    }
}
