package com.tech.brain.controller;

import com.tech.brain.model.RoleName;
import com.tech.brain.model.ServiceRegistrationRequest;
import com.tech.brain.model.UserInfo;
import com.tech.brain.service.RoleService;
import com.tech.brain.service.ServiceRegistryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/register")
@RequiredArgsConstructor
public class RegistrationController {

    private final RoleService service;
    private final ServiceRegistryService registryService;

    @Operation(
            summary = "Public welcome endpoint",
            description = "Returns a simple welcome message. This endpoint is not secured.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Welcome message returned")
            }
    )
    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome this endpoint is not secure";
    }

    @Operation(
            summary = "Register a new user with role",
            description = "Creates a new user with a default role. Only ADMIN or MANAGER can access.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "User info including name, password, and role",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserInfo.class),
                            examples = @ExampleObject(
                                    name = "New User",
                                    value = """
                    {
                      "name": "jane_doe",
                      "password": "securePassword",
                      "roles": "CLIENT"
                    }
                """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "User created successfully"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - insufficient role")
            }
    )
    @PostMapping("/new/role")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') || hasAuthority('ROLE_MANAGER')")
    public String addNewUser(@RequestBody UserInfo userInfo){
        return service.addUser(userInfo);
    }

    @Operation(
            summary = "List all registered services",
            description = "Returns a list of all services registered in the system. Accessible to ADMIN and MANAGER.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of services returned"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - insufficient role")
            }
    )
    @GetMapping("/service/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') || hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<?> listServices() {
        return ResponseEntity.ok(registryService.getAllServices());
    }

    @Operation(
            summary = "Register a new service",
            description = "Registers a new microservice with requested scopes. Can be accessed by many roles.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Service registration request with name and scope details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ServiceRegistrationRequest.class),
                            examples = @ExampleObject(
                                    name = "Register Service",
                                    value = """
                    {
                      "service_name": "invoice-generator",
                      "description": "Handles invoice operations"
                    }
                """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Service registered successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request")
            }
    )
    @PostMapping("/new/service")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') || hasAuthority('ROLE_MANAGER') || hasAuthority('ROLE_CLIENT') || hasAuthority('ROLE_SERVICE') || hasAuthority('ROLE_HR')")
    public ResponseEntity<?> registerService(@Valid @RequestBody ServiceRegistrationRequest request) {
        return registryService.registerService(request);
    }

    @Operation(
            summary = "Approve scope access for a service",
            description = "Marks the requested scope as approved. Only accessible to ADMIN or MANAGER.",
            parameters = {
                    @Parameter(name = "scopeId", description = "Scope ID to approve", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Scope approved"),
                    @ApiResponse(responseCode = "404", description = "Scope not found")
            }
    )
    @PreAuthorize("hasAuthority('ROLE_ADMIN') || hasAuthority('ROLE_MANAGER')")
    @PutMapping("/approve/{scopeId}")
    public ResponseEntity<?> approveService(@PathVariable Long scopeId) {
        return registryService.approveScope(scopeId);
    }

    @Operation(
            summary = "Promote user to higher role",
            description = "Assigns a new role to a user. Only ADMIN can access.",
            parameters = {
                    @Parameter(name = "userId", description = "ID of the user to promote", required = true),
                    @Parameter(name = "roleName", description = "Role to assign (e.g., ROLE_MANAGER, ROLE_ADMIN)", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "User promoted successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid role or user"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - only ADMIN allowed")
            }
    )
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/promote/{userId}")
    public ResponseEntity<?> promoteToAdmin(@PathVariable Integer userId, @RequestParam RoleName roleName) {
        return service.assignRole(userId, roleName);
    }

    @Operation(
            summary = "Remove role from user",
            description = "Removes a role from a user. Accessible to ADMIN or MANAGER.",
            parameters = {
                    @Parameter(name = "userId", description = "ID of the user to demote", required = true),
                    @Parameter(name = "roleName", description = "Role to remove", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Role removed from user"),
                    @ApiResponse(responseCode = "404", description = "User or role not found")
            }
    )
    @PreAuthorize("hasAuthority('ROLE_ADMIN') || hasAuthority('ROLE_MANAGER')")
    @DeleteMapping("/demote/{userId}")
    public ResponseEntity<?> removeRoleFromUser(@PathVariable Integer userId, @RequestParam RoleName roleName) {
        return service.revokeRoleForUser(userId, roleName);
    }

    @Operation(
            summary = "List all users with their roles",
            description = "Returns a list of all users along with their assigned roles. Accessible to most roles.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Users with roles returned"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - role not allowed")
            }
    )
    @PreAuthorize("hasAuthority('ROLE_ADMIN') || hasAuthority('ROLE_MANAGER') || hasAuthority('ROLE_CLIENT') || hasAuthority('ROLE_SERVICE') || hasAuthority('ROLE_HR')")
    @GetMapping("/users-with-roles")
    public ResponseEntity<List<Map<String, Object>>> listUsersWithRoles() {
        return ResponseEntity.ok(service.getAllUser());
    }
}
