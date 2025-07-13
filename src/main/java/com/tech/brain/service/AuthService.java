package com.tech.brain.service;

import com.tech.brain.entity.UserInfoEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

public interface AuthService {
    UserInfoEntity validateUser(String username, String password);
    String generateToken(UserDetails userDetails, String service, String scope);
    String generateToken(Map<String, Object> extraClaims, UserDetails userDetails);

}
