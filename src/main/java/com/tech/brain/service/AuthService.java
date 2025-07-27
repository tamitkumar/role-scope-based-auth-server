package com.tech.brain.service;

import com.tech.brain.entity.UserInfoEntity;
import com.tech.brain.model.TokenResponse;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

public interface AuthService {
    UserInfoEntity validateUser(String username, String password);
    String generateToken(UserDetails userDetails, String service, String scope);
    String generateToken(Map<String, Object> extraClaims, UserDetails userDetails);
    TokenResponse generateAccessAndRefreshToken(UserDetails userDetails, String service, String scope);
}
