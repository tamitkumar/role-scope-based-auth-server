package com.tech.brain.controller;

import com.tech.brain.model.RoleName;
import com.tech.brain.model.ServiceRegistrationRequest;
import com.tech.brain.model.UserInfo;
import com.tech.brain.service.RoleService;
import com.tech.brain.service.ServiceRegistryService;
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

    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome this endpoint is not secure";
    }

    @PostMapping("/new/role")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') || hasAuthority('ROLE_MANAGER')")
    public String addNewUser(@RequestBody UserInfo userInfo){
        return service.addUser(userInfo);
    }

    @GetMapping("/service/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') || hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<?> listServices() {
        return ResponseEntity.ok(registryService.getAllServices());
    }

    @PostMapping("/new/service")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') || hasAuthority('ROLE_MANAGER') || hasAuthority('ROLE_CLIENT') || hasAuthority('ROLE_SERVICE') || hasAuthority('ROLE_HR')")
    public ResponseEntity<?> registerService(@Valid @RequestBody ServiceRegistrationRequest request) {
        return registryService.registerService(request);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') || hasAuthority('ROLE_MANAGER')")
    @PutMapping("/approve/{scopeId}")
    public ResponseEntity<?> approveService(@PathVariable Long scopeId) {
        return registryService.approveScope(scopeId);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/promote/{userId}")
    public ResponseEntity<?> promoteToAdmin(@PathVariable Integer userId, @RequestParam RoleName roleName) {
        return service.assignRole(userId, roleName);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') || hasAuthority('ROLE_MANAGER')")
    @DeleteMapping("/demote/{userId}")
    public ResponseEntity<?> removeRoleFromUser(@PathVariable Integer userId, @RequestParam RoleName roleName) {
        return service.revokeRoleForUser(userId, roleName);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') || hasAuthority('ROLE_MANAGER') || hasAuthority('ROLE_CLIENT') || hasAuthority('ROLE_SERVICE') || hasAuthority('ROLE_HR')")
    @GetMapping("/users-with-roles")
    public ResponseEntity<List<Map<String, Object>>> listUsersWithRoles() {
        return ResponseEntity.ok(service.getAllUser());
    }
}
