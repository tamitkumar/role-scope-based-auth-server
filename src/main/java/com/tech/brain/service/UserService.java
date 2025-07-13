package com.tech.brain.service;

import com.tech.brain.config.UserInfoUserDetails;
import com.tech.brain.entity.RoleEntity;
import com.tech.brain.entity.UserInfoEntity;
import com.tech.brain.exception.AuthException;
import com.tech.brain.model.RoleName;
import com.tech.brain.repository.RoleRepository;
import com.tech.brain.repository.UserInfoRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service("userService")
@Transactional
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    private final PasswordEncoder passwordEncoder;
    private final UserInfoRepository repository;
    private final RoleRepository roleRepo;

    @Override
    public UserDetails loadUserByUsername(String username) {
        log.info("UserService loadUserByUsername init controller");
        UserInfoEntity userInfo = repository.findByName(username)
                .orElseThrow(() -> new AuthException(new UsernameNotFoundException("User not found: " + username)));
        log.info("User Roles: {}", userInfo.getRoles());
        return new UserInfoUserDetails(userInfo);
    }

    @PostConstruct
    public void initAdminIfMissing() {
        log.info("AuthService ===> initAdminIfMissing {}", adminUsername);
        repository.findByName(adminUsername).orElseGet(() -> {
            log.info("AuthService ===> Not Available {}", adminUsername);
            UserInfoEntity admin = new UserInfoEntity();
            admin.setName(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setEmail("tamitkumar16@gmail.com");
            // Assign admin role (you may need to create/find it first)
            RoleEntity adminRole = new RoleEntity();
            adminRole.setName(RoleName.ADMIN);
            admin.setRoles(Set.of(roleRepo.save(adminRole)));
            return repository.save(admin);
        });
    }
}
