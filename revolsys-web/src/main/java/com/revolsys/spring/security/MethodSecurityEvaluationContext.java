package com.revolsys.spring.security;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;

/**
 * Internal security-specific EvaluationContext implementation which lazily adds
 * the method parameter values as variables (with the corresponding parameter
 * names) if and when they are required.
 *
 * @author Luke Taylor
 * @since 3.0
 */
public class MethodSecurityEvaluationContext extends StandardEvaluationContext {
  private static Log logger = LogFactory.getLog(MethodSecurityEvaluationContext.class);

  private boolean argumentsAdded;

  private final MethodInvocation mi;

  private ParameterNameDiscoverer parameterNameDiscoverer;

  /**
   * Intended for testing. Don't use in practice as it creates a new parameter
   * resolver for each instance. Use the constructor which takes the resolver,
   * as an argument thus allowing for caching.
   */
  public MethodSecurityEvaluationContext(final Authentication user, final MethodInvocation mi) {
    this(user, mi, new LocalVariableTableParameterNameDiscoverer());
  }

  public MethodSecurityEvaluationContext(final Authentication user, final MethodInvocation mi,
    final ParameterNameDiscoverer parameterNameDiscoverer) {
    this.mi = mi;
    this.parameterNameDiscoverer = parameterNameDiscoverer;
  }

  private void addArgumentsAsVariables() {
    final Object[] args = this.mi.getArguments();

    if (args.length == 0) {
      return;
    }

    final Object targetObject = this.mi.getThis();
    final Method method = AopUtils.getMostSpecificMethod(this.mi.getMethod(),
      targetObject.getClass());
    final String[] paramNames = this.parameterNameDiscoverer.getParameterNames(method);

    if (paramNames == null) {
      logger.warn("Unable to resolve method parameter names for method: " + method
        + ". Debug symbol information is required if you are using parameter names in expressions.");
      return;
    }

    for (int i = 0; i < args.length; i++) {
      super.setVariable(paramNames[i], args[i]);
    }
  }

  @Override
  public Object lookupVariable(final String name) {
    final Object variable = super.lookupVariable(name);
    if (variable != null) {
      return variable;
    }

    if (!this.argumentsAdded) {
      addArgumentsAsVariables();
      this.argumentsAdded = true;
    }

    return super.lookupVariable(name);
  }

  public void setParameterNameDiscoverer(final ParameterNameDiscoverer parameterNameDiscoverer) {
    this.parameterNameDiscoverer = parameterNameDiscoverer;
  }

}
