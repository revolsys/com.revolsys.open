package com.revolsys.spring.security;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.ExpressionUtils;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;

public class RsMethodSecurityExpressionHandler implements
  MethodSecurityExpressionHandler {

  protected final Log logger = LogFactory.getLog(getClass());

  private ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

  private PermissionEvaluator permissionEvaluator = new DenyAllPermissionEvaluator();

  private AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

  private final ExpressionParser expressionParser = new SpelExpressionParser();

  private RoleHierarchy roleHierarchy;

  public RsMethodSecurityExpressionHandler() {
  }

  /**
   * Uses a {@link MethodSecurityEvaluationContext} as the
   * <tt>EvaluationContext</tt> implementation and configures it with a
   * {@link MethodSecurityExpressionRoot} instance as the expression root
   * object.
   */
  public EvaluationContext createEvaluationContext(
    final Authentication auth,
    final MethodInvocation mi) {
    final MethodSecurityEvaluationContext ctx = new MethodSecurityEvaluationContext(
      auth, mi, parameterNameDiscoverer);
    final MethodSecurityExpressionRoot root = new MethodSecurityExpressionRoot(
      auth);
    root.setTrustResolver(trustResolver);
    root.setPermissionEvaluator(permissionEvaluator);
    root.setRoleHierarchy(roleHierarchy);
    ctx.setRootObject(root);

    return ctx;
  }

  @SuppressWarnings("unchecked")
  public Object filter(
    final Object filterTarget,
    final Expression filterExpression,
    final EvaluationContext ctx) {
    final MethodSecurityExpressionRoot rootObject = (MethodSecurityExpressionRoot)ctx.getRootObject()
      .getValue();
    List retainList;

    if (logger.isDebugEnabled()) {
      logger.debug("Filtering with expression: "
        + filterExpression.getExpressionString());
    }

    if (filterTarget instanceof Collection) {
      final Collection collection = (Collection)filterTarget;
      retainList = new ArrayList(collection.size());

      if (logger.isDebugEnabled()) {
        logger.debug("Filtering collection with " + collection.size()
          + " elements");
      }
      for (final Object filterObject : (Collection)filterTarget) {
        rootObject.setFilterObject(filterObject);

        if (ExpressionUtils.evaluateAsBoolean(filterExpression, ctx)) {
          retainList.add(filterObject);
        }
      }

      if (logger.isDebugEnabled()) {
        logger.debug("Retaining elements: " + retainList);
      }

      collection.clear();
      collection.addAll(retainList);

      return filterTarget;
    }

    if (filterTarget.getClass().isArray()) {
      final Object[] array = (Object[])filterTarget;
      retainList = new ArrayList(array.length);

      if (logger.isDebugEnabled()) {
        logger.debug("Filtering collection with " + array.length + " elements");
      }

      for (int i = 0; i < array.length; i++) {
        rootObject.setFilterObject(array[i]);

        if (ExpressionUtils.evaluateAsBoolean(filterExpression, ctx)) {
          retainList.add(array[i]);
        }
      }

      if (logger.isDebugEnabled()) {
        logger.debug("Retaining elements: " + retainList);
      }

      final Object[] filtered = (Object[])Array.newInstance(
        filterTarget.getClass().getComponentType(), retainList.size());
      for (int i = 0; i < retainList.size(); i++) {
        filtered[i] = retainList.get(i);
      }

      return filtered;
    }

    throw new IllegalArgumentException(
      "Filter target must be a collection or array type, but was "
        + filterTarget);
  }

  public ExpressionParser getExpressionParser() {
    return expressionParser;
  }

  public void setParameterNameDiscoverer(
    final ParameterNameDiscoverer parameterNameDiscoverer) {
    this.parameterNameDiscoverer = parameterNameDiscoverer;
  }

  public void setPermissionEvaluator(
    final PermissionEvaluator permissionEvaluator) {
    this.permissionEvaluator = permissionEvaluator;
  }

  public void setReturnObject(
    final Object returnObject,
    final EvaluationContext ctx) {
    ((MethodSecurityExpressionRoot)ctx.getRootObject().getValue()).setReturnObject(returnObject);
  }

  public void setRoleHierarchy(final RoleHierarchy roleHierarchy) {
    this.roleHierarchy = roleHierarchy;
  }

  public void setTrustResolver(final AuthenticationTrustResolver trustResolver) {
    this.trustResolver = trustResolver;
  }
}
