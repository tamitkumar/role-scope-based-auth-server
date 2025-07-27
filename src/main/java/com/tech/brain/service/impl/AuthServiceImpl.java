package com.tech.brain.service.impl;

import com.nimbusds.jwt.SignedJWT;
import com.tech.brain.config.JWTCacheConfig;
import com.tech.brain.entity.UserInfoEntity;
import com.tech.brain.exception.AuthException;
import com.tech.brain.exception.ErrorCode;
import com.tech.brain.exception.ErrorSeverity;
import com.tech.brain.entity.RefreshToken;
import com.tech.brain.model.TokenResponse;
import com.tech.brain.repository.RefreshTokenRepository;
import com.tech.brain.repository.UserInfoRepository;
import com.tech.brain.service.AuthService;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final JWTCacheConfig tokenCache;
    private final UserInfoRepository repository;
    private final RefreshTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final KeyPair keyPair;

    @Override
    public UserInfoEntity validateUser(String username, String password) {
        log.info("AuthService ===> validateUser {}", username);
        UserInfoEntity userInfoEntity = repository.findByName(username)
                .orElseThrow(() -> new AuthException(new UsernameNotFoundException("User not found: " + username)));
        if (passwordEncoder.matches(password, userInfoEntity.getPassword())) {
            log.info("AuthService ===> validateUser {}", username);
            return userInfoEntity;
        } else {
            log.error("AuthService ===> validateUser {}", username);
            throw new AuthException(ErrorCode.ERR000.getErrorCode(), ErrorSeverity.FATAL, ErrorCode.ERR000.getErrorMessage());
        }
    }

    @Override
    public String generateToken(UserDetails userDetails, String service, String scope) {
        log.info("AuthService ===> generateToken {}", userDetails.getUsername());
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("authorities", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        if(StringUtils.isNotEmpty(service) && StringUtils.isNotEmpty(scope)) {
            extraClaims.put("scope", scope);
            extraClaims.put("service-name", service);
        }
        extraClaims.put("iss", "https://auth.tiwarytech.site");
        return generateToken(extraClaims, userDetails);
    }

    @Override
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        log.info("AuthService ===> Authorities in token: {}", extraClaims.get("authorities"));
        userDetails.getAuthorities()
                .forEach(a ->
                                log.info("AuthService ===> GrantedAuthority: {}", a.getAuthority()));
        return Jwts.builder()
                .header()
                    .add("kid", "auth-key")
                    .and()
                .claims()
                    .add(extraClaims)
                    .subject(userDetails.getUsername())
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour
                .and() // closes the claims() block
                .signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();
    }

    @Override
    public TokenResponse generateAccessAndRefreshToken(UserDetails userDetails, String service, String scope) {
        log.info("AuthService ===> generateAccessAndRefreshToken service {}, scope {}", service, scope);
        String cacheKey = userDetails.getUsername() + "::" + service + "::" + scope;
        AtomicReference<String> accessToken = new AtomicReference<>();
        AtomicReference<String> refreshToken = new AtomicReference<>();
        tokenRepository.findByUser(userDetails.getUsername()).ifPresentOrElse(token -> {
            log.info("AuthService ===> Refresh Token Already Exist for service {}, scope {}", service, scope);
            if(token.getExpiry().isBefore(Instant.now())) {
                log.info("AuthService ===> Refresh Token Expired for service {}, scope {}", service, scope);
                accessToken.set(generateToken(userDetails, service, scope));
                try {
                    SignedJWT signedJWT = SignedJWT.parse(accessToken.get());
                    Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
                    token.setExpiry(expirationTime.toInstant());
                    tokenRepository.save(token);
                    tokenCache.put(cacheKey, accessToken.get(), expirationTime.toInstant());
                } catch (ParseException e) {
                    throw new AuthException(e);
                }
            } else {
                refreshToken.set(token.getTokenId());
                tokenCache.get(cacheKey).ifPresentOrElse(
                        accessToken::set,
                        () -> {
                            // Fallback: generate again if not in cache
                            accessToken.set(generateToken(userDetails, service, scope));
                            try {
                                SignedJWT signedJWT = SignedJWT.parse(accessToken.get());
                                Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
                                tokenCache.put(cacheKey, accessToken.get(), expirationTime.toInstant());
                            } catch (ParseException e) {
                                throw new AuthException(e);
                            }
                        }
                );
            }

        }, () -> {
            log.info("AuthService ===> Adding Refresh Token in DB for service {}, scope {}", service, scope);
            refreshToken.set(UUID.randomUUID().toString());
            accessToken.set(generateToken(userDetails, service, scope));
            try {
                SignedJWT signedJWT = SignedJWT.parse(accessToken.get());
                Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
                tokenRepository.save(
                        new RefreshToken(refreshToken.get(),
                                userDetails.getUsername(),
                                expirationTime.toInstant()));
                tokenCache.put(cacheKey, accessToken.get(), expirationTime.toInstant());
            } catch (ParseException e) {
                throw new AuthException(e);
            }
        });
        return new TokenResponse(accessToken.get(), refreshToken.get());
    }
}
