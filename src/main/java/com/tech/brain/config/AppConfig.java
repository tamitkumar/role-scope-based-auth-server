package com.tech.brain.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

@Slf4j
@Configuration
@OpenAPIDefinition(
        info = @io.swagger.v3.oas.annotations.info.Info(title = "Role Based Auth Server API", version = "1.0"),
        security = @SecurityRequirement(name = "BearerAuth")
)
@SecurityScheme(
        name = "BearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class AppConfig {

    private KeyPair keyPair;

    @Bean
    public ConfigurableServletWebServerFactory webServerFactory() {
        log.info("AppConfig ===> init web server");
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.setPort(8088); // MUST match NGINX config
        return factory;
    }

    @Bean
    public OpenAPI openAPI() {
        log.info("AppConfig ===> init OpenAPI");
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("Role Based Authentication Server API")
                        .description("API for generating and using JWT tokens")
                        .version("1.0"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new io.swagger.v3.oas.models.security.SecurityScheme()
                                .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement().addList("BearerAuth"));
    }

    @Bean
    public ObjectMapper objectMapper() {
        log.info("AppConfig ===> init ObjectMapper");
        ObjectMapper mapper = new ObjectMapper();
        // 1. Don't fail on unknown fields in incoming JSON
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 2. Pretty print JSON output
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        // 3. Include non-null fields only
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 4. Support Java 8 date/time types (LocalDate, LocalDateTime)
        mapper.registerModule(new JavaTimeModule());
        // 5. Prevent timestamps for dates (write ISO strings instead)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 6. Optional: Change naming strategy (e.g., camelCase → snake_case) user_name maps to userName (because of SNAKE_CASE).
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return mapper;
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userService){
        log.info("DialectConfig ===> authenticationProvider");
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("DialectConfig ===> passwordEncoder");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public KeyPair generateRsaKey() {
        log.info("JwtAuthFilter ===> generateRsaKey");
        if (this.keyPair == null) {
            log.info("JwtAuthFilter ===> generating RsaKey");
            try {
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
                generator.initialize(2048);
                this.keyPair = generator.generateKeyPair();
            } catch (Exception e) {
                throw new RuntimeException("RSA key generation failed", e);
            }
        }
        return this.keyPair;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        log.info("SecurityConfig ===> authenticationManager");
        return config.getAuthenticationManager();
    }
}
