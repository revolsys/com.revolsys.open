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

public class RsMethodSecurityExpressionHandler implements MethodSecurityExpressionHandler {

  private final ExpressionParser expressionParser = new SpelExpressionParser();

  protected final Log logger = LogFactory.getLog(getClass());

  private ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

  private PermissionEvaluator permissionEvaluator = new DenyAllPermissionEvaluator();

  private RoleHierarchy roleHierarchy;

  private AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

  public RsMethodSecurityExpressionHandler() {
  }

  /**
   * Uses a {@link MethodSecurityEvaluationContext} as the
   * <tt>EvaluationContext</tt> implementation and configures it with a
   * {@link MethodSecurityExpressionRoot} instance as the expression root
   * object.
   */
  @Override
  public EvaluationContext createEvaluationContext(final Authentication auth,
    final MethodInvocation mi) {
    final MethodSecurityEvaluationContext ctx = new MethodSecurityEvaluationContext(auth, mi,
      this.parameterNameDiscoverer);
    final MethodSecurityExpressionRoot root = new MethodSecurityExpressionRoot(auth);
    root.setTrustResolver(this.trustResolver);
    root.setPermissionEvaluator(this.permissionEvaluator);
    root.setRoleHierarchy(this.roleHierarchy);
    ctx.setRootObject(root);

    return ctx;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object filter(final Object filterTarget, final Expression filterExpression,
    final EvaluationContext ctx) {
    final MethodSecurityExpressionRoot rootObject = (MethodSecurityExpressionRoot)ctx
      .getRootObject()
      .getValue();
    List retainList;

    if (this.logger.isDebugEnabled()) {
      this.logger.debug("Filtering with expression: " + filterExpression.getExpressionString());
    }

    if (filterTarget instanceof Collection) {
      final Collection collection = (Collection)filterTarget;
      retainList = new ArrayList(collection.size());

      if (this.logger.isDebugEnabled()) {
        this.logger.debug("Filtering collection with " + collection.size() + " elements");
      }
      for (final Object filterObject : (Collection)filterTarget) {
        rootObject.setFilterObject(filterObject);

        if (ExpressionUtils.evaluateAsBoolean(filterExpression, ctx)) {
          retainList.add(filterObject);
        }
      }

      if (this.logger.isDebugEnabled()) {
        this.logger.debug("Retaining elements: " + retainList);
      }

      collection.clear();
      collection.addAll(retainList);

      return filterTarget;
    }

    if (filterTarget.getClass().isArray()) {
      final Object[] array = (Object[])filterTarget;
      retainList = new ArrayList(array.length);

      if (this.logger.isDebugEnabled()) {
        this.logger.debug("Filtering collection with " + array.length + " elements");
      }

      for (final Object element : array) {
        rootObject.setFilterObject(element);

        if (ExpressionUtils.evaluateAsBoolean(filterExpression, ctx)) {
          retainList.add(element);
        }
      }

      if (this.logger.isDebugEnabled()) {
        this.logger.debug("Retaining elements: " + retainList);
      }

      final Object[] filtered = (Object[])Array
        .newInstance(filterTarget.getClass().getComponentType(), retainList.size());
      for (int i = 0; i < retainList.size(); i++) {
        filtered[i] = retainList.get(i);
      }

      return filtered;
    }

    throw new IllegalArgumentException(
      "Filter target must be a collection or array type, but was " + filterTarget);
  }

  @Override
  public ExpressionParser getExpressionParser() {
    return this.expressionParser;
  }

  public void setParameterNameDiscoverer(final ParameterNameDiscoverer parameterNameDiscoverer) {
    this.parameterNameDiscoverer = parameterNameDiscoverer;
  }

  public void setPermissionEvaluator(final PermissionEvaluator permissionEvaluator) {
    this.permissionEvaluator = permissionEvaluator;
  }

  @Override
  public void setReturnObject(final Object returnObject, final EvaluationContext ctx) {
    ((MethodSecurityExpressionRoot)ctx.getRootObject().getValue()).setReturnObject(returnObject);
  }

  public void setRoleHierarchy(final RoleHierarchy roleHierarchy) {
    this.roleHierarchy = roleHierarchy;
  }

  public void setTrustResolver(final AuthenticationTrustResolver trustResolver) {
    this.trustResolver = trustResolver;
  }
}
