package com.tech.brain.service;

import com.tech.brain.entity.ScopeRequestEntity;
import com.tech.brain.entity.ServiceEntity;
import com.tech.brain.model.ServiceRegistrationRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ServiceRegistryService {
    ResponseEntity<?> registerService(ServiceRegistrationRequest request);
    ResponseEntity<?> approveScope(Long serviceId);
    List<ServiceEntity> getAllServices();
    ResponseEntity<?> approveScope(Long serviceId, String scope);
    List<String> getApprovedScopes(Long serviceId);
    List<ScopeRequestEntity> getAllScopes(Long serviceId);
    ResponseEntity<?> requestedScopeStatus(Long serviceId, String scope);
}
