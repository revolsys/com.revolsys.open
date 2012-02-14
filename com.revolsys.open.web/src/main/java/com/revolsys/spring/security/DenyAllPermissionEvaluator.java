package com.revolsys.spring.security;

import java.io.Serializable;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;


public class DenyAllPermissionEvaluator implements PermissionEvaluator {
  public boolean hasPermission(
    Authentication authentication,
    Object target,
    Object permission) {
    return false;
  }

  public boolean hasPermission(
    Authentication authentication,
    Serializable targetId,
    String targetType,
    Object permission) {
    return false;
  }

}
