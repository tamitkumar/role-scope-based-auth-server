package com.tech.brain.config;

import com.tech.brain.entity.UserInfoEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class UserInfoUserDetails implements UserDetails {

    private final String name;
    private final String password;
    private final List<GrantedAuthority> authorities;

    public UserInfoUserDetails(UserInfoEntity userInfo) {
        log.info("UserInfoUserDetails constructor called");
        name=userInfo.getName();
        password=userInfo.getPassword();
        authorities= userInfo.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                .collect(Collectors.toList());
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        log.info("getAuthorities called");
        return authorities;
    }

    @Override
    public String getPassword() {
        log.info("UserInfoUserDetails ====> getPassword called");
        return password;
    }

    @Override
    public String getUsername() {
        log.info("UserInfoUserDetails ====> getUsername called");
        return name;
    }

    @Override
    public boolean isAccountNonExpired() {
        log.info("UserInfoUserDetails ====> getAccountNonExpired called");
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        log.info("UserInfoUserDetails ====> getAccountNonLocked called");
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        log.info("UserInfoUserDetails ====> getCredentialsNonExpired called");
        return true;
    }

    @Override
    public boolean isEnabled() {
        log.info("UserInfoUserDetails ====> getEnabled called");
        return true;
    }
}
