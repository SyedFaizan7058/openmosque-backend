package com.openmosque.security.annotation;

import java.lang.annotation.*;

/**
 * Injects the authenticated User entity directly into controller method arguments.
 * Eliminates repetitive SecurityContext parsing.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
}
