package com.revolsys.spring.security;

import java.util.Map;
import java.util.Map.Entry;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.revolsys.ui.web.utils.HttpRequestUtils;

public class SpringExpressionUtil {

  public static EvaluationContext createEvaluationContext(Object object) {
    final EvaluationContext evaluationContext = new StandardEvaluationContext(
      object);
    Map<String, String> pathVariables = HttpRequestUtils.getPathVariables();
    setVariables(evaluationContext, pathVariables);
    return evaluationContext;
  }

  public static EvaluationContext createSecurityEvaluationContext() {
    final SecurityContext securityContext = SecurityContextHolder.getContext();
    final Authentication authentication = securityContext.getAuthentication();
    final MethodSecurityExpressionRoot root = new MethodSecurityExpressionRoot(
      authentication);
    final EvaluationContext evaluationContext = createEvaluationContext(root);
    return evaluationContext;
  }

  public static void setVariables(
    final EvaluationContext evaluationContext,
    Map<String, String> variables) {
    for (Entry<String, String> variable : variables.entrySet()) {
      String name = variable.getKey();
      String value = variable.getValue();
      evaluationContext.setVariable(name, value);
    }
  }
}
