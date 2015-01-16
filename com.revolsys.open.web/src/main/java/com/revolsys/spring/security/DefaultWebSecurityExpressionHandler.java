package com.revolsys.spring.security;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.WebSecurityExpressionHandler;

/**
 * Facade which isolates Spring Security's requirements for evaluating
 * web-security expressions from the implementation of the underlying expression
 * objects.
 *
 * @author Luke Taylor
 * @since 3.0
 */
public class DefaultWebSecurityExpressionHandler implements
WebSecurityExpressionHandler {

  private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

  private final ExpressionParser expressionParser = new SpelExpressionParser();

  private RoleHierarchy roleHierarchy;

  @Override
  public EvaluationContext createEvaluationContext(
    final Authentication authentication,
    final FilterInvocation fi) {
    final StandardEvaluationContext ctx = new StandardEvaluationContext();
    final SecurityExpressionRoot root = new WebSecurityExpressionRoot(
      authentication, fi);
    root.setTrustResolver(this.trustResolver);
    root.setRoleHierarchy(this.roleHierarchy);
    ctx.setRootObject(root);

    return ctx;
  }

  @Override
  public ExpressionParser getExpressionParser() {
    return this.expressionParser;
  }

  public void setRoleHierarchy(final RoleHierarchy roleHierarchy) {
    this.roleHierarchy = roleHierarchy;
  }
}
