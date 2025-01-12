package com.github.togrul2.booklet.annotations;

import org.springframework.security.test.context.support.WithMockUser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
public @interface WithMockAdminUser {
}
