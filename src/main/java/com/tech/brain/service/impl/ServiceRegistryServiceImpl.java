package com.tech.brain.service.impl;

import com.tech.brain.entity.ScopeRequestEntity;
import com.tech.brain.entity.ServiceEntity;
import com.tech.brain.exception.AuthException;
import com.tech.brain.exception.ErrorCode;
import com.tech.brain.model.ServiceRegistrationRequest;
import com.tech.brain.repository.ScopeRequestRepository;
import com.tech.brain.repository.ServiceRepository;
import com.tech.brain.service.ServiceRegistryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceRegistryServiceImpl implements ServiceRegistryService {

    private final ServiceRepository serviceRepo;
    private final ScopeRequestRepository scopeRepo;

    @Override
    public ResponseEntity<?> registerService(ServiceRegistrationRequest request) {
        log.info("ServiceRegistryService ===> registerService={}", request.getServiceName());
        AtomicReference<ResponseEntity<?>> response = new AtomicReference<>();
        serviceRepo.findByServiceName(request.getService()).ifPresentOrElse(entity -> {
            log.info("ServiceRegistryService ===> Service already registered={}", request.getServiceName());
            boolean scopeExists = entity.getRequestedScopes().stream()
                    .anyMatch(en -> en.getScope().equalsIgnoreCase(request.getScope()));
            if (scopeExists) {
                log.info("ServiceRegistryService ===> Scope '{}' already exists for service '{}'", request.getScope(), request.getServiceName());
                response.set(ResponseEntity.ok("Service already registered with this scope."));
                return;
            }

            entity.getRequestedScopes().add(ScopeRequestEntity.builder()
                    .scope(request.getScope())
                    .approved(false)
                    .service(entity)
                    .build());
            ServiceEntity updatedEntity = serviceRepo.save(entity);
            response.set(ResponseEntity.ok(updatedEntity));
        }, () -> {
            log.info("ServiceRegistryService ===> Service not registered");
            ServiceEntity service = new ServiceEntity();
            service.setServiceName(request.getService());

            ScopeRequestEntity scope = ScopeRequestEntity.builder()
                    .scope(request.getScope())
                    .approved(false)
                    .service(service)
                    .build();

            service.setRequestedScopes(List.of(scope));
            ServiceEntity saved = serviceRepo.save(service);

            response.set(ResponseEntity.ok(saved));
        });
        return response.get();
    }

    @Override
    public ResponseEntity<?> approveScope(Long scopeId) {
        log.info("ServiceRegistryService ===> approveService={}", scopeId);
        ScopeRequestEntity service = scopeRepo.findById(scopeId)
                .orElseThrow(() -> new AuthException(ErrorCode.ERR000.getErrorCode(), "Service not found"));
        service.setApproved(true);
        service = scopeRepo.save(service);
        return ResponseEntity.ok("Service approved: " + service.getService().getServiceName());
    }

    @Override
    public List<ServiceEntity> getAllServices() {
        log.info("ServiceRegistryService ===> getAllServices");
        return serviceRepo.findAll();
    }

    @Override
    public ResponseEntity<?> approveScope(Long serviceId, String scope) {
        log.info("ServiceRegistryService ===> approveScope={}", serviceId);
        ScopeRequestEntity scopeEntity = scopeRepo.findByScopeAndServiceId(scope, serviceId)
                .orElseThrow(() -> new AuthException(ErrorCode.ERR000.getErrorCode(), "Scope request not found"));
        scopeEntity.setApproved(true);
        return ResponseEntity.ok(scopeRepo.save(scopeEntity));
    }

    @Override
    public List<String> getApprovedScopes(Long serviceId) {
        log.info("ServiceRegistryService ===> getApprovedScopes={}", serviceId);
        return scopeRepo.findByServiceId(serviceId).stream()
                .filter(ScopeRequestEntity::isApproved)
                .map(ScopeRequestEntity::getScope)
                .toList();
    }

    @Override
    public List<ScopeRequestEntity> getAllScopes(Long serviceId) {
        log.info("ServiceRegistryService ===> getAllScopes={}", serviceId);
        return scopeRepo.findByServiceId(serviceId);
    }

    @Override
    public ResponseEntity<?> requestedScopeStatus(Long serviceId, String scope) {
        log.info("ServiceRegistryService ===> requestScope={}", serviceId);
        ServiceEntity service = serviceRepo.findById(serviceId)
                .orElseThrow(() -> new AuthException(ErrorCode.ERR000.getErrorCode(), "Service not found"));
        List<ScopeRequestEntity> getRequestedScopes = service.getRequestedScopes()
                .stream()
                .filter(s -> s.getScope().equalsIgnoreCase(scope) && s.isApproved())
                .toList();
        if (getRequestedScopes.isEmpty())
            return ResponseEntity.ok().body("Service not yet approved");
        else
            return ResponseEntity.ok("Service Scope approved");
    }
}
