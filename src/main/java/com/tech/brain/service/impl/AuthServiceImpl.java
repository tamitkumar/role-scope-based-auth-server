package com.tech.brain.service.impl;

import com.tech.brain.entity.UserInfoEntity;
import com.tech.brain.exception.AuthException;
import com.tech.brain.exception.ErrorCode;
import com.tech.brain.exception.ErrorSeverity;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserInfoRepository repository;
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
}
