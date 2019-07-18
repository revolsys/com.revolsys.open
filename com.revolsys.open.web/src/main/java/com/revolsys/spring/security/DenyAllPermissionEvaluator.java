package com.revolsys.spring.security;

import java.io.Serializable;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

public class DenyAllPermissionEvaluator implements PermissionEvaluator {
  @Override
  public boolean hasPermission(final Authentication authentication, final Object target,
    final Object permission) {
    return false;
  }

  @Override
  public boolean hasPermission(final Authentication authentication, final Serializable targetId,
    final String targetType, final Object permission) {
    return false;
  }

}
