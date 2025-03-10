package com.github.togrul2.booklet.annotations;


import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A custom annotation to check if the user is authenticated.
 * This annotation uses the {@link PreAuthorize} annotation to check user roles and can be used on methods and classes.
 *
 * @see PreAuthorize
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("isAuthenticated()")
public @interface IsAuthenticated {
}
