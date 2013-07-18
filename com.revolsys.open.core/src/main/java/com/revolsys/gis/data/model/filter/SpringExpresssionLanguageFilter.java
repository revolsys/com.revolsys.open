package com.revolsys.gis.data.model.filter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.expression.Expression;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.revolsys.filter.Filter;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.io.map.MapSerializer;

public class SpringExpresssionLanguageFilter implements Filter<DataObject>,
  MapSerializer {
  private static SpelParserConfiguration EXPRESSION_CONFIGURATION = new SpelParserConfiguration(
    true, true);

  private static final SpelExpressionParser EXPRESSION_PARSER = new SpelExpressionParser(
    EXPRESSION_CONFIGURATION);

  private final Expression expression;

  private final StandardEvaluationContext context = new StandardEvaluationContext();

  private final String query;

  public SpringExpresssionLanguageFilter(final String query) {
    this(query, Collections.<String, Object> emptyMap());
  }

  @SuppressWarnings("unchecked")
  public SpringExpresssionLanguageFilter(final String query,
    final Map<String, ? extends Object> variables) {
    this.query = query;
    this.expression = EXPRESSION_PARSER.parseExpression(query);
    context.addPropertyAccessor(new DataObjectAccessor());
    context.setVariables((Map<String, Object>)variables);
  }

  @Override
  public boolean accept(final DataObject object) {
    try {
      final Boolean value = expression.getValue(context, object, Boolean.class);
      return value;
    } catch (final Throwable e) {
      return false;
    }
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("type", "queryFilter");
    map.put("query", query);
    return map;
  }

  @Override
  public String toString() {
    return expression.getExpressionString();
  }
}
