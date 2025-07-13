## 🏛️ Class Diagram
### Core Components

#### AuthController

1. Responsibilities: handles /token and /.well-known/jwks.json HTTP endpoints.

2. Collaborates with AuthService, UserDetailsService, and KeyPair.

#### AuthService (interface)

1. Defines method(s) like String generateToken(UserDetails, String service, String scope).

#### AuthServiceImpl

1. Implements AuthService, uses Jwts.builder() to create JWT with RSA private key.

#### JwtAuthFilter

1. Spring Security filter that extracts and validates JWT on incoming requests.

#### AppConfig

1. Spring @Configuration class that provides beans:

    A. KeyPair (RSA)

    B. JwtDecoder (pointing at JWKS URI)

    C. Security config (PasswordEncoder, AuthenticationProvider, OpenAPI, etc.)

#### Data and Utility Classes

1. AuthRequest

   1.  DTO for login: typically username, password, service, scope.

2. AuthResponse

   1.  DTO wrapping generated JWT: e.g., String token.

3. UserInfoEntity

   1.  JPA entity with user credentials and authorities.

4. UserInfoUserDetails

    1.  Implements Spring UserDetails, built from UserInfoEntity.

5. ErrorCode, ErrorSeverity, AuthException

   1. Error handling enums/exceptions.

#### Relationships

1. AuthController depends on AuthService, UserDetailsService, and KeyPair.

2. AuthServiceImpl implements AuthService.

3. JwtAuthFilter uses KeyPair to validate incoming tokens.

4. AppConfig wires up all dependencies including the RSA KeyPair and JWKS/JWK setup.

#### 🔄 Sequence Diagram: Login → Token Generation → Token Validation
1. Client → AuthController

    1.  Sends POST /api/auth/token with JSON body containing username, password, service, scope.

2. AuthController → AuthService.validateUser()

    1.  Validates user credentials against DB (via JPA repository).

3. AuthController → UserDetailsService.loadUserByUsername()

    1.  Loads UserDetails (authorities, username).

4. AuthController → AuthService.generateToken(...)

    1.  Generates JWT using RSA private key from KeyPair.

    2.  Embeds kid = auth-key, claims including authorities, service, scope, iss.

5. AuthController → Client

    1. Returns AuthResponse containing JWT token.

6. Client → Resource (e.g., Product Microservice)

    1.  Makes secured request with Authorization: Bearer <JWT> header.

7. ResourceService → JwtDecoder (Nimbus configured via JWKS URI in AppConfig)

    1.  Decodes and validates JWT using the JWKS endpoint (AuthController/.well-known/jwks.json), fetching RSA public key.

8. JwtDecoder → AuthController/jwks.json

    1.  GET request to retrieve RSA public key.

9. ResourceService proceeds

    1.  If JWT is valid, processes request with assigned authority/scope.

10. If invalid

    1.  Filter throws JwtException, request rejected.