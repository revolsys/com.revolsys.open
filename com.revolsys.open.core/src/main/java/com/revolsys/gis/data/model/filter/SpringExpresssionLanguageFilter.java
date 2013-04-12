package com.revolsys.gis.data.model.filter;

import java.util.Collections;
import java.util.Map;

import org.springframework.expression.Expression;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.revolsys.filter.Filter;
import com.revolsys.gis.data.model.DataObject;

public class SpringExpresssionLanguageFilter implements Filter<DataObject> {
  private static SpelParserConfiguration EXPRESSION_CONFIGURATION = new SpelParserConfiguration(
    true, true);

  private static final SpelExpressionParser EXPRESSION_PARSER = new SpelExpressionParser(
    EXPRESSION_CONFIGURATION);

  private final Expression expression;

  private final StandardEvaluationContext context = new StandardEvaluationContext();

  public SpringExpresssionLanguageFilter(final String expression) {
    this(expression, Collections.<String, Object> emptyMap());
  }

  @SuppressWarnings("unchecked")
  public SpringExpresssionLanguageFilter(final String expression,
    final Map<String, ? extends Object> variables) {
    this.expression = EXPRESSION_PARSER.parseExpression(expression);
    context.addPropertyAccessor(new DataObjectAccessor());
    context.setVariables((Map<String, Object>)variables);
  }

  @Override
  public boolean accept(final DataObject object) {
    return expression.getValue(context, object, Boolean.class);
  }
}
