package com.tech.brain.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.tech.brain.entity.UserInfoEntity;
import com.tech.brain.model.AuthRequest;
import com.tech.brain.model.AuthResponse;
import com.tech.brain.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserDetailsService userDetailsService;
    private final KeyPair keyPair;

    @PostMapping("/token")
    public ResponseEntity<?> getToken(@Valid @RequestBody AuthRequest request) {
        log.info("AuthController ===> getToken called {}", request.getUsername());
        log.info("Request received for: {}", request.getService());
        log.info("Parsed service: {}, scope: {}", request.getService(), request.getScope());
        UserInfoEntity user = authService.validateUser(request.getUsername(), request.getPassword());
        if (user == null) {
            log.error("AuthController ===> getToken called {} failed", request.getUsername());
            return ResponseEntity.status(401).body("Invalid credentials");
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getName());
        String token = authService.generateToken(userDetails, request.getService(), request.getScope());
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> keys() {
        RSAKey jwk = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .keyID("auth-key")
                .build();
        return new JWKSet(jwk).toJSONObject();
    }
}
