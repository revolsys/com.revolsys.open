package com.revolsys.spring.security;

import java.util.Map;
import java.util.Map.Entry;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.revolsys.ui.web.utils.HttpServletUtils;

public class SpringExpressionUtil {

  public static EvaluationContext newEvaluationContext(final Object object) {
    final EvaluationContext evaluationContext = new StandardEvaluationContext(object);
    final Map<String, String> pathVariables = HttpServletUtils.getPathVariables();
    setVariables(evaluationContext, pathVariables);
    return evaluationContext;
  }

  public static EvaluationContext newSecurityEvaluationContext() {
    final SecurityContext securityContext = SecurityContextHolder.getContext();
    final Authentication authentication = securityContext.getAuthentication();
    final MethodSecurityExpressionRoot root = new MethodSecurityExpressionRoot(authentication);
    final EvaluationContext evaluationContext = newEvaluationContext(root);
    return evaluationContext;
  }

  public static void setVariables(final EvaluationContext evaluationContext,
    final Map<String, String> variables) {
    for (final Entry<String, String> variable : variables.entrySet()) {
      final String name = variable.getKey();
      final String value = variable.getValue();
      evaluationContext.setVariable(name, value);
    }
  }
}
