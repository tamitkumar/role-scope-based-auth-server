package com.tech.brain.controller;

import com.tech.brain.service.ServiceRegistryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/register/scope")
@RequiredArgsConstructor
public class ScopeController {
    private final ServiceRegistryService serviceService;

    @Operation(
            summary = "Approve a specific scope for a service",
            description = "Marks a specific scope as approved for the given service ID. Only accessible to ADMIN or MANAGER.",
            parameters = {
                    @Parameter(name = "serviceId", description = "ID of the service", required = true, example = "1001"),
                    @Parameter(name = "scope", description = "Scope to approve (e.g., invoice-generator::create)", required = true, example = "invoice-generator::create")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Scope approved successfully"),
                    @ApiResponse(responseCode = "404", description = "Service or scope not found"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - insufficient role")
            }
    )
    @PreAuthorize("hasAuthority('ROLE_ADMIN') || hasAuthority('ROLE_MANAGER')")
    @PutMapping("/approve")
    public ResponseEntity<?> approveScope(@RequestParam Long serviceId, @RequestParam String scope) {
        return serviceService.approveScope(serviceId, scope);
    }

    @Operation(
            summary = "Get approval status of a requested scope",
            description = "Returns the current status (approved, pending, or rejected) of a requested scope for a given service ID. Accessible to all role types.",
            parameters = {
                    @Parameter(name = "serviceId", description = "ID of the service", required = true, example = "1001"),
                    @Parameter(name = "scope", description = "Scope name (e.g., invoice-generator::read)", required = true, example = "invoice-generator::read")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Scope status returned"),
                    @ApiResponse(responseCode = "404", description = "Service or scope not found")
            }
    )
    @PreAuthorize("hasAuthority('ROLE_ADMIN') || hasAuthority('ROLE_MANAGER') || hasAuthority('ROLE_CLIENT') || hasAuthority('ROLE_SERVICE') || hasAuthority('ROLE_HR')")
    @PostMapping("/request/status")
    public ResponseEntity<?> scopeStatus(@RequestParam Long serviceId, @RequestParam String scope) {
        return serviceService.requestedScopeStatus(serviceId, scope);
    }

    @Operation(
            summary = "List all scopes (approved + requested) for a service",
            description = "Returns all scopes (both approved and requested) for the given service ID. Only accessible to ADMIN or MANAGER.",
            parameters = {
                    @Parameter(name = "serviceId", description = "ID of the service", required = true, example = "1001")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of scopes returned"),
                    @ApiResponse(responseCode = "404", description = "Service not found"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - insufficient role")
            }
    )
    @PreAuthorize("hasAuthority('ROLE_ADMIN') || hasAuthority('ROLE_MANAGER')")
    @GetMapping("/list/{serviceId}")
    public ResponseEntity<?> listScopes(@PathVariable Long serviceId) {
        return ResponseEntity.ok(serviceService.getAllScopes(serviceId));
    }

    @Operation(
            summary = "List approved scopes for a service",
            description = "Returns only the approved scopes for a given service ID. Only accessible to ADMIN or MANAGER.",
            parameters = {
                    @Parameter(name = "serviceId", description = "ID of the service", required = true, example = "1001")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of approved scopes returned"),
                    @ApiResponse(responseCode = "404", description = "Service not found"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - insufficient role")
            }
    )
    @PreAuthorize("hasAuthority('ROLE_ADMIN') || hasAuthority('ROLE_MANAGER')")
    @GetMapping("/approve/{serviceId}")
    public ResponseEntity<List<String>> approvedScopes(@PathVariable Long serviceId) {
        return ResponseEntity.ok(serviceService.getApprovedScopes(serviceId));
    }
}
