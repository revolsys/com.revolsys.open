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
import com.revolsys.ui.html.builder.HtmlUiBuilder;
import com.revolsys.ui.html.builder.HtmlUiBuilderAware;
import com.revolsys.ui.web.utils.HttpServletUtils;
import com.revolsys.util.HtmlUtil;
import com.revolsys.util.Property;

public class ActionFormKeySerializer extends AbstractKeySerializer implements
  HtmlUiBuilderAware<HtmlUiBuilder<?>> {
  public static void serialize(final XmlWriter out, final Object object,
    final HtmlUiBuilder<?> uiBuilder, final List<String> parameterNames,
    final Map<String, String> parameterNameMap, final String target,
    final String label, final Expression enabledExpression, final String name,
    String cssClass) {
    try {
      final Map<String, Object> parameters = new HashMap<String, Object>();
      for (final String parameterName : parameterNames) {
        final Object value = Property.get(object, parameterName);
        parameters.put(parameterName, value);
      }
      for (final Entry<String, String> entry : parameterNameMap.entrySet()) {
        final String parameterName = entry.getKey();
        final String keyName = entry.getValue();
        final Object value = uiBuilder.getProperty(object, keyName);
        parameters.put(parameterName, value);
      }
      if (enabledExpression != null) {
        final StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
        if (object instanceof Map) {
          @SuppressWarnings("unchecked")
          final Map<String, Object> map = (Map<String, Object>)object;
          evaluationContext.setVariables(map);
        } else {
          evaluationContext.setRootObject(object);
        }
        if (!ExpressionUtils.evaluateAsBoolean(enabledExpression,
          evaluationContext)) {
          return;
        }
      }
      final Object id = uiBuilder.getIdValue(object);
      parameters.put(uiBuilder.getIdParameterName(), id);

      final String actionUrl = uiBuilder.getPageUrl(name, parameters);
      if (actionUrl != null) {
        out.startTag(HtmlUtil.FORM);
        out.attribute(HtmlUtil.ATTR_ACTION, actionUrl);
        out.attribute(HtmlUtil.ATTR_METHOD, "post");
        out.attribute(HtmlUtil.ATTR_TARGET, target);
        final String lowerLabel = label.toLowerCase();
        final HttpServletRequest request = HttpServletUtils.getRequest();
        for (final String parameterName : Arrays.asList("plain", "htmlCss")) {
          HtmlUtil.serializeHiddenInput(out, parameterName,
            request.getParameter(parameterName));
        }
        if (!Property.hasValue(cssClass)) {
          cssClass = lowerLabel;
        }

        HtmlUtil.serializeButton(out, lowerLabel, "submit", label, label,
          cssClass);
        out.endTag(HtmlUtil.FORM);
      }
    } catch (final Throwable t) {
      LoggerFactory.getLogger(ActionFormKeySerializer.class).error(
        "Unable to serialize", t);
    }
  }

  private String cssClass;

  private Expression enabledExpression;

  private Map<String, String> parameterNameMap = new LinkedHashMap<String, String>();

  private List<String> parameterNames = new ArrayList<String>();

  private HtmlUiBuilder<?> uiBuilder;

  private String target;

  public ActionFormKeySerializer() {
    setProperties(null);
  }

  public String getCssClass() {
    return cssClass;
  }

  public Map<String, String> getParameterNameMap() {
    return parameterNameMap;
  }

  public List<String> getParameterNames() {
    return parameterNames;
  }

  public String getTarget() {
    return target;
  }

  @Override
  public void serialize(final XmlWriter out, final Object object) {
    final HtmlUiBuilder<?> uiBuilder = this.uiBuilder;
    final List<String> parameterNames = getParameterNames();
    final Map<String, String> parameterNameMap = getParameterNameMap();
    final String target = getTarget();
    final String label = getLabel();
    final Expression enabledExpression = this.enabledExpression;
    final String name = getName();
    final String cssClass = getCssClass();
    serialize(out, object, uiBuilder, parameterNames, parameterNameMap, target,
      label, enabledExpression, name, cssClass);
  }

  public void setCssClass(final String cssClass) {
    this.cssClass = cssClass;
  }

  public void setEnabledExpression(final String enabledExpression) {
    this.enabledExpression = new SpelExpressionParser().parseExpression(enabledExpression);
  }

  @Override
  public void setHtmlUiBuilder(final HtmlUiBuilder<?> uiBuilder) {
    this.uiBuilder = uiBuilder;
  }

  public void setParameterNameMap(final Map<String, String> parameterNameMap) {
    this.parameterNameMap = parameterNameMap;
  }

  public void setParameterNames(final List<String> parameterNames) {
    this.parameterNames = parameterNames;
  }

  @Override
  public void setProperties(final Map<String, ? extends Object> properties) {
    getProperties().clear();
    if (properties != null) {
      getProperties().putAll(properties);
    }
    setProperty("sortable", false);
    setProperty("searchable", false);
  }

  public void setTarget(final String target) {
    this.target = target;
  }
}
