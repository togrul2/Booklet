package com.github.togrul2.booklet.entities;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum Role {
    USER(
            Permission.USER_WRITE,
            Permission.USER_READ
    ),
    ADMIN(
            Permission.ADMIN_WRITE,
            Permission.ADMIN_READ,
            Permission.USER_READ,
            Permission.USER_WRITE
    );

    private final Permission[] permissions;

    Role(Permission... permissions) {
        this.permissions = permissions;
    }

    public Set<GrantedAuthority> getAuthorities() {
        return Arrays
                .stream(permissions)
                .map(a -> new SimpleGrantedAuthority(a.name()))
                .collect(Collectors.toSet());

    }
}
