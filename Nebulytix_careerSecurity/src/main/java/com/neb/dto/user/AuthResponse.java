package com.neb.dto.user;

import lombok.Data;
import java.util.Set;

@Data
public class AuthResponse {

    private String accessToken;
    private String refreshToken;

    // Required for frontend authorization & menu visibility
    private Set<String> roles;

    // To decide which dashboard to load
    private String dashboard;
}