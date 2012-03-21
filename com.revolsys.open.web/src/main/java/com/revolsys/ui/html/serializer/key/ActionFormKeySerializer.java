package com.revolsys.ui.html.serializer.key;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.access.expression.ExpressionUtils;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.builder.HtmlUiBuilder;
import com.revolsys.ui.html.builder.HtmlUiBuilderAware;
import com.revolsys.ui.web.utils.HttpRequestUtils;
import com.revolsys.util.JavaBeanUtil;

public class ActionFormKeySerializer extends AbstractKeySerializer implements
  HtmlUiBuilderAware<HtmlUiBuilder<?>> {
  private List<String> parameterNames = new ArrayList<String>();

  private Map<String, String> parameterNameMap = new LinkedHashMap<String, String>();

  private HtmlUiBuilder<?> uiBuilder;

  public void serialize(final XmlWriter out, final Object object) {
    try {
      Map<String, Object> parameters = new HashMap<String, Object>();
      for (String name : parameterNames) {
        Object value = JavaBeanUtil.getValue(object, name);
        parameters.put(name, value);
      }
      for (Entry<String, String> entry : parameterNameMap.entrySet()) {
        String parameterName = entry.getKey();
        Object value = JavaBeanUtil.getValue(object, entry.getValue());
        parameters.put(parameterName, value);
      }
      if (enabledExpression != null) {
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext(
          object);
        if (!ExpressionUtils.evaluateAsBoolean(enabledExpression,
          evaluationContext)) {
          return;
        }
      }
      final Object id = uiBuilder.getIdValue(object);
      parameters.put(uiBuilder.getIdParameterName(), id);

      String actionUrl = uiBuilder.getPageUrl(getName(), parameters);
      if (actionUrl != null) {
        out.startTag(HtmlUtil.FORM);
        out.attribute(HtmlUtil.ATTR_ACTION, actionUrl);
        out.attribute(HtmlUtil.ATTR_METHOD, "post");
        String lowerLabel = getLabel().toLowerCase();
        HttpServletRequest request = HttpRequestUtils.getHttpServletRequest();
        for (String parameterName : Arrays.asList("plain", "htmlCss")) {
          HtmlUtil.serializeHiddenInput(out, parameterName,
            request.getParameter(parameterName));
        }
        HtmlUtil.serializeButton(out, lowerLabel, "submit", getLabel(),
          getLabel(), lowerLabel);
        out.endTag(HtmlUtil.FORM);
      }
    } catch (Throwable t) {
      LoggerFactory.getLogger(getClass()).error("Unable to serialize", t);
    }
  }

  public void setHtmlUiBuilder(final HtmlUiBuilder<?> uiBuilder) {
    this.uiBuilder = uiBuilder;
  }

  public List<String> getParameterNames() {
    return parameterNames;
  }

  public void setParameterNames(List<String> parameterNames) {
    this.parameterNames = parameterNames;
  }

  public Map<String, String> getParameterNameMap() {
    return parameterNameMap;
  }

  public void setParameterNameMap(Map<String, String> parameterNameMap) {
    this.parameterNameMap = parameterNameMap;
  }

  private Expression enabledExpression;

  public void setEnabledExpression(String enabledExpression) {
    this.enabledExpression = new SpelExpressionParser().parseExpression(enabledExpression);
  }
}
