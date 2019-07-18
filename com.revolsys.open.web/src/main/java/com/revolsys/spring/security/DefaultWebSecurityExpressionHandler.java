package com.revolsys.spring.security;

import org.springframework.security.access.expression.AbstractSecurityExpressionHandler;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.access.expression.SecurityExpressionOperations;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.util.Assert;

/**
 *
 * @author Luke Taylor
 * @since 3.0
 */
public class DefaultWebSecurityExpressionHandler
  extends AbstractSecurityExpressionHandler<FilterInvocation>
  implements SecurityExpressionHandler<FilterInvocation> {

  private String defaultRolePrefix = "ROLE_";

  private AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

  @Override
  protected SecurityExpressionOperations createSecurityExpressionRoot(
    final Authentication authentication, final FilterInvocation fi) {
    final WebSecurityExpressionRoot root = new WebSecurityExpressionRoot(authentication, fi);
    root.setPermissionEvaluator(getPermissionEvaluator());
    root.setTrustResolver(this.trustResolver);
    root.setRoleHierarchy(getRoleHierarchy());
    root.setDefaultRolePrefix(this.defaultRolePrefix);
    return root;
  }

  /**
   * <p>
   * Sets the default prefix to be added to {@link #hasAnyRole(String...)} or
   * {@link #hasRole(String)}. For example, if hasRole("ADMIN") or hasRole("ROLE_ADMIN")
   * is passed in, then the role ROLE_ADMIN will be used when the defaultRolePrefix is
   * "ROLE_" (default).
   * </p>
   *
   * <p>
   * If null or empty, then no default role prefix is used.
   * </p>
   *
   * @param defaultRolePrefix the default prefix to add to roles. Default "ROLE_".
   */
  public void setDefaultRolePrefix(final String defaultRolePrefix) {
    this.defaultRolePrefix = defaultRolePrefix;
  }

  /**
   * Sets the {@link AuthenticationTrustResolver} to be used. The default is
   * {@link AuthenticationTrustResolverImpl}.
   *
   * @param trustResolver the {@link AuthenticationTrustResolver} to use. Cannot be
   * null.
   */
  public void setTrustResolver(final AuthenticationTrustResolver trustResolver) {
    Assert.notNull(trustResolver, "trustResolver cannot be null");
    this.trustResolver = trustResolver;
  }
}
