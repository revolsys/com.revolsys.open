package com.revolsys.spring.security;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

public class SecurityExpressionRoot
  extends org.springframework.security.access.expression.SecurityExpressionRoot {
  private PermissionEvaluator permissionEvaluator;

  private RoleHierarchy roleHierarchy;

  private Set<String> roles;

  public SecurityExpressionRoot(final Authentication a) {
    super(a);
  }

  public Set<String> getAuthoritySet() {
    if (this.roles == null) {
      this.roles = new HashSet<>();
      Collection<? extends GrantedAuthority> userAuthorities = this.authentication.getAuthorities();

      if (this.roleHierarchy != null) {
        userAuthorities = this.roleHierarchy.getReachableGrantedAuthorities(userAuthorities);
      }

      this.roles = AuthorityUtils.authorityListToSet(userAuthorities);
    }

    return this.roles;
  }

  @Override
  public boolean hasPermission(final Object target, final Object permission) {
    return this.permissionEvaluator.hasPermission(this.authentication, target, permission);
  }

  @Override
  public boolean hasPermission(final Object targetId, final String targetType,
    final Object permission) {
    return this.permissionEvaluator.hasPermission(this.authentication, (Serializable)targetId,
      targetType, permission);
  }

  public boolean hasRoleRegex(final String regex) {
    final Pattern pattern = Pattern.compile(regex);
    for (final String role : getAuthoritySet()) {
      final Matcher matcher = pattern.matcher(role);
      final boolean matches = matcher.matches();
      if (matches) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void setPermissionEvaluator(final PermissionEvaluator permissionEvaluator) {
    this.permissionEvaluator = permissionEvaluator;
  }

  @Override
  public void setRoleHierarchy(final RoleHierarchy roleHierarchy) {
    super.setRoleHierarchy(roleHierarchy);
    this.roleHierarchy = roleHierarchy;
  }

}
