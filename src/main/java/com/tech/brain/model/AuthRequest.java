package com.tech.brain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthRequest {

    @NotBlank(message = "Username cannot be blank")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    private String password;

    @JsonProperty("service_name")
    @Pattern(
            regexp = "^[A-Za-z0-9]+(-[A-Za-z0-9]+)*::(read|write|update|create)$",
            message = "If provided, service name must follow 'a-b-c::read|write|update|create' format"
    )
    @Schema(example = "invoice-generator::create", description = "Format: 'a-b-c::read|write|update|create'")
    private String serviceName;

    @JsonIgnore
    private String service;

    @JsonIgnore
    private String scope;

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;

        if (serviceName != null && serviceName.contains("::")) {
            String[] parts = serviceName.split("::", 2);
            this.service = parts[0];
            this.scope = parts[1];
        }
    }
}
