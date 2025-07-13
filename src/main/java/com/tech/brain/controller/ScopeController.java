package com.tech.brain.controller;

import com.tech.brain.service.ServiceRegistryService;
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

    @PreAuthorize("hasAuthority('ROLE_ADMIN') || hasAuthority('ROLE_MANAGER')")
    @PutMapping("/approve")
    public ResponseEntity<?> approveScope(@RequestParam Long serviceId, @RequestParam String scope) {
        return serviceService.approveScope(serviceId, scope);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') || hasAuthority('ROLE_MANAGER') || hasAuthority('ROLE_CLIENT') || hasAuthority('ROLE_SERVICE') || hasAuthority('ROLE_HR')")
    @PostMapping("/request/status")
    public ResponseEntity<?> scopeStatus(@RequestParam Long serviceId, @RequestParam String scope) {
        return serviceService.requestedScopeStatus(serviceId, scope);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') || hasAuthority('ROLE_MANAGER')")
    @GetMapping("/list/{serviceId}")
    public ResponseEntity<?> listScopes(@PathVariable Long serviceId) {
        return ResponseEntity.ok(serviceService.getAllScopes(serviceId));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') || hasAuthority('ROLE_MANAGER')")
    @GetMapping("/approve/{serviceId}")
    public ResponseEntity<List<String>> approvedScopes(@PathVariable Long serviceId) {
        return ResponseEntity.ok(serviceService.getApprovedScopes(serviceId));
    }
}
