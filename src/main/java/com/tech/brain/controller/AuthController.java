package com.tech.brain.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.tech.brain.entity.UserInfoEntity;
import com.tech.brain.model.AuthRequest;
import com.tech.brain.model.AuthResponse;
import com.tech.brain.model.TokenResponse;
import com.tech.brain.repository.RefreshTokenRepository;
import com.tech.brain.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserDetailsService userDetailsService;
    private final RefreshTokenRepository tokenRepository;
    private final KeyPair keyPair;

    @Operation(
            summary = "Generate JWT Access Token",
            description = "Authenticates user credentials and issues a signed JWT access token.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "User credentials and target service details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthRequest.class),
                            examples = @ExampleObject(
                                    name = "AuthRequest Example",
                                    value = """
                    {
                      "username": "john_doe",
                      "password": "password123",
                      "service_name": "invoice-generator::read"
                    }
                """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Token generated successfully",
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials")
            }
    )
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
        TokenResponse token = authService.generateAccessAndRefreshToken(userDetails, request.getService(), request.getScope());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildCookies(token.getRefreshToken()).toString())
                .body(new AuthResponse(token.getAccessToken()));
    }

    @Operation(
            summary = "Get JWKS Public Key Set",
            description = "Returns the JSON Web Key Set (JWKS) containing the public RSA keys used to verify JWTs.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "JWKS key set returned",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "JWKS Example",
                                            value = """
                        {
                          "keys": [
                            {
                              "kty": "RSA",
                              "kid": "auth-key",
                              "n": "base64url-encoded-modulus",
                              "e": "AQAB"
                            }
                          ]
                        }
                    """
                                    )
                            )
                    )
            }
    )
    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> keys() {
        log.info("AuthController ===> keys called");
        RSAKey jwk = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .keyID("auth-key")
                .build();
        return new JWKSet(jwk).toJSONObject();
    }

    @Operation(
            summary = "Refresh Access Token using Refresh Token",
            description = "Validates the refresh token and issues a new JWT access token.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Refresh token, service name, and scope",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    example = """
                    {
                      "refreshToken": "abc123-uuid",
                      "service": "invoice-generator",
                      "scope": "read"
                    }
                """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "New access token issued",
                            content = @Content(schema = @Schema(implementation = TokenResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Expired or invalid refresh token")
            }
    )
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshAccessToken(@RequestBody Map<String, String> request) {
        log.info("AuthController ===> refreshAccessToken called");
        String refreshToken = request.get("refreshToken");
        AtomicReference<ResponseEntity<?>> response = new AtomicReference<>();
        tokenRepository.findById(refreshToken).ifPresentOrElse(info -> {
            log.info("AuthController ===> Refresh Token found for user '{}' from DB", info.getUser());
            if (info.getExpiry().isBefore(Instant.now())) {
                log.error("AuthController ===> Refresh Token Expired for user '{}'", info.getUser());
                response.set(ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("Expired refresh token"));
            } else {
                UserDetails userDetails = userDetailsService.loadUserByUsername(info.getUser());
                log.info("AuthController ===> Refresh Token for user '{}'", info.getUser());
                TokenResponse accessToken = authService
                        .generateAccessAndRefreshToken(userDetails, request.get("service"), request.get("scope"));
                response.set(ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, buildCookies(accessToken.getRefreshToken()).toString())
                        .body(new TokenResponse(accessToken.getAccessToken(), null)));
            }
        }, () -> response.set(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token")));
        return response.get();
    }

    @Operation(
            summary = "Logout User (Invalidate Refresh Token)",
            description = "Deletes the provided refresh token to effectively log out the user.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Refresh token to invalidate",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    example = """
                    {
                      "refreshToken": "abc123-uuid"
                    }
                """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Logged out successfully"),
                    @ApiResponse(responseCode = "404", description = "Refresh token not found")
            }
    )
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> request) {
        log.info("AuthController ===> logout called'");
        AtomicReference<ResponseEntity<?>> response = new AtomicReference<>();
        String refreshToken = request.get("refreshToken");
        tokenRepository.findById(refreshToken).ifPresentOrElse(info -> {
            log.info("AuthController ===> deleting token for user '{}'", info.getUser());
            tokenRepository.delete(info);
            response.set(ResponseEntity.ok("Logged out successfully"));
        }, () -> response.set(ResponseEntity.notFound().build()));
        return response.get();
    }

    private ResponseCookie buildCookies(String refreshToken) {
        log.info("AuthController ===> buildCookies Called");
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("None")
                .maxAge(Duration.ofHours(1))
                .build();
    }
}
