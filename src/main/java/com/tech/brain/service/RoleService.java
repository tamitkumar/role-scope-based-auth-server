package com.tech.brain.service;

import com.tech.brain.entity.RoleEntity;
import com.tech.brain.entity.UserInfoEntity;
import com.tech.brain.exception.AuthException;
import com.tech.brain.exception.ErrorCode;
import com.tech.brain.model.Product;
import com.tech.brain.model.RoleName;
import com.tech.brain.model.UserInfo;
import com.tech.brain.repository.RoleRepository;
import com.tech.brain.repository.UserInfoRepository;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {
    List<Product> productList = null;
    private final UserInfoRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepo;

    @PostConstruct
    public void loadProductsFromDB() {
        productList = IntStream.rangeClosed(1, 100)
                .mapToObj(i -> Product.builder()
                        .productId(i)
                        .name("product " + i)
                        .qty(new Random().nextInt(10))
                        .price(new Random().nextInt(5000)).build()
                ).collect(Collectors.toList());
    }

    public ResponseEntity<?> assignRole(Integer userId, RoleName roleName) {
        UserInfoEntity user = repository.findById(userId)
                .orElseThrow(() -> new AuthException(ErrorCode.ERR000.getErrorCode(), "User not found to assign role"));
        RoleEntity role = roleRepo.findByName(roleName).orElseGet(() -> {
            // Save role only if not exists
            RoleEntity newRole = new RoleEntity();
            newRole.setName(roleName);
            return roleRepo.save(newRole);
        });

        // Add role if not already assigned
        user.getRoles().add(role);

        UserInfoEntity savedUser = repository.save(user);

        Set<String> assignedRoles = savedUser.getRoles()
                .stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());

        return ResponseEntity.accepted().body("User: " + savedUser.getName() + " Assigned Roles: " + assignedRoles);
    }

    public ResponseEntity<?> revokeRoleForUser(Integer userId, RoleName roleName) {
        log.info("Revoking role for user: {}", userId);
        UserInfoEntity user = repository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        RoleEntity role = roleRepo.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        if (user.getRoles().remove(role)) {
            repository.save(user);
            return ResponseEntity.ok("Role " + roleName + " removed from user " + user.getName());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body("User doesn't have role: " + roleName);
        }
    }

    public List<Product> getProducts() {
        return productList;
    }

    public Product getProduct(int id) {
        return productList.stream()
                .filter(product -> product.getProductId() == id)
                .findAny()
                .orElseThrow(() -> new RuntimeException("product " + id + " not found"));
    }


    public String addUser(UserInfo userInfo) {
        log.info("RoleService ===> addUser {}", userInfo.getName());
        if (repository.findByName(userInfo.getName()).isPresent()) {
            log.info("RoleService ===> User {} already exists", userInfo.getName());
            return "User already exists";
        }
        AtomicReference<String> result = new AtomicReference<>("");
        roleRepo.findByName(userInfo.getRoles())
                .ifPresentOrElse(roles -> {
                    log.info("RoleService ===> roles present {}", roles);
                    UserInfoEntity newUser = new UserInfoEntity();
                    newUser.setName(userInfo.getName());
                    newUser.setEmail(userInfo.getEmail());
                    newUser.setPassword(passwordEncoder.encode(userInfo.getPassword()));
                    result.set(buildRole(roles, newUser));
                }, () -> {
                    log.info("RoleService ===> roles not present {}", userInfo.getName());
                    UserInfoEntity newUser = new UserInfoEntity();
                    newUser.setName(userInfo.getName());
                    newUser.setPassword(passwordEncoder.encode(userInfo.getPassword()));
                    RoleEntity newRole = new RoleEntity();
                    newRole.setName(userInfo.getRoles());
                    newUser.setEmail(userInfo.getEmail());
                    RoleEntity saveRole = roleRepo.save(newRole);
                    result.set(buildRole(saveRole, newUser));
                });
        return result.get();
    }

    private String buildRole(@NonNull RoleEntity roles, UserInfoEntity newUser) {
        log.info("RoleService ===> buildRole {}", roles.getName());
        newUser.setRoles(Set.of(roles));
        UserInfoEntity savedUser = repository.save(newUser);
        Set<String> assignedRoles = savedUser.getRoles()
                .stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());
        return "User: " + savedUser.getName() + " Assigned Roles: " + assignedRoles;
    }

    public List<Map<String, Object>> getAllUser() {
        log.info("RoleService ===> getAllUser");
        List<UserInfoEntity> users = repository.findAll();
        return users.stream().map(user -> {
            log.info("RoleService ===> getUser {}", user.getName());
            Map<String, Object> data = new HashMap<>();
            data.put("username", user.getName());
            data.put("roles", user.getRoles().stream()
                    .map(role -> role.getName().name())
                    .collect(Collectors.toSet()));
            return data;
        }).collect(Collectors.toList());
    }
}
