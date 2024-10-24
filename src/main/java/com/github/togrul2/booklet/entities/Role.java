package com.github.togrul2.booklet.entities;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.HashSet;
import java.util.Set;

public enum Role {
    USER,
    ADMIN;

    public Set<GrantedAuthority> getAuthorities() {
        HashSet<GrantedAuthority> perms = new HashSet<>();

        // All users have ROLE_USER by default, if user is admin he also gets ROLE_ADMIN.
        perms.add(new SimpleGrantedAuthority("ROLE_" + USER.name()));

        if (this == ADMIN) {
            perms.add(new SimpleGrantedAuthority("ROLE_" + ADMIN.name()));
        }

        return perms;
    }
}
