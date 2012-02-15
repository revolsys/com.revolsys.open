package com.revolsys.spring.security;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

/**
 * Extended expression root object which contains extra method-specific
 * functionality.
 * 
 * @author Luke Taylor
 * @since 3.0
 */
public class MethodSecurityExpressionRoot extends SecurityExpressionRoot {
  private PermissionEvaluator permissionEvaluator;

  private Object filterObject;

  private Object returnObject;

  public final String read = "read";

  public final String write = "write";

  public final String create = "create";

  public final String delete = "delete";

  public final String admin = "administration";

  private RoleHierarchy roleHierarchy;

  private Set<String> roles;

  MethodSecurityExpressionRoot(final Authentication a) {
    super(a);
  }

  public Set<String> getAuthoritySet() {
    if (roles == null) {
      roles = new HashSet<String>();
      Collection<GrantedAuthority> userAuthorities = authentication.getAuthorities();

      if (getReturnObject() != null) {
        userAuthorities = roleHierarchy.getReachableGrantedAuthorities(userAuthorities);
      }

      roles = AuthorityUtils.authorityListToSet(userAuthorities);
    }

    return roles;
  }

  public Object getFilterObject() {
    return filterObject;
  }

  public Object getReturnObject() {
    return returnObject;
  }

  public boolean hasPermission(final Object target, final Object permission) {
    return permissionEvaluator.hasPermission(authentication, target, permission);
  }

  public boolean hasPermission(
    final Object targetId,
    final String targetType,
    final Object permission) {
    return permissionEvaluator.hasPermission(authentication,
      (Serializable)targetId, targetType, permission);
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

  public void setFilterObject(final Object filterObject) {
    this.filterObject = filterObject;
  }

  public void setPermissionEvaluator(
    final PermissionEvaluator permissionEvaluator) {
    this.permissionEvaluator = permissionEvaluator;
  }

  public void setReturnObject(final Object returnObject) {
    this.returnObject = returnObject;
  }

  @Override
  public void setRoleHierarchy(final RoleHierarchy roleHierarchy) {
    super.setRoleHierarchy(roleHierarchy);
    this.roleHierarchy = roleHierarchy;
  }

}
