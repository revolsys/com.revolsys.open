package com.revolsys.ui.html.serializer.key;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.jeometry.common.logging.Logs;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.access.expression.ExpressionUtils;

import com.revolsys.record.io.format.html.Aria;
import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.builder.HtmlUiBuilder;
import com.revolsys.ui.html.builder.HtmlUiBuilderAware;
import com.revolsys.ui.html.view.BootstrapUtil;
import com.revolsys.ui.web.utils.HttpServletUtils;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;
import com.revolsys.util.HtmlUtil;
import com.revolsys.util.Property;

public class ActionFormKeySerializer extends AbstractKeySerializer
  implements HtmlUiBuilderAware<HtmlUiBuilder<?>> {
  private String cssClass;

  private Expression enabledExpression;

  private String iconName;

  private Map<String, String> parameterNameMap = new LinkedHashMap<>();

  private List<String> parameterNames = new ArrayList<>();

  private String target;

  private HtmlUiBuilder<?> uiBuilder;

  public ActionFormKeySerializer() {
    setProperties(null);
    setLabel("Actions");
  }

  public ActionFormKeySerializer(final String name, final String label, final String iconName) {
    super(name, label);
    setProperties(null);
    setIconName(iconName);
  }

  public ActionFormKeySerializer addParameterName(final String name, final String key) {
    this.parameterNameMap.put(name, key);
    return this;
  }

  public String getCssClass() {
    return this.cssClass;
  }

  public String getIconName() {
    return this.iconName;
  }

  public Map<String, String> getParameterNameMap() {
    return this.parameterNameMap;
  }

  public List<String> getParameterNames() {
    return this.parameterNames;
  }

  public String getTarget() {
    return this.target;
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
    serialize(out, object, uiBuilder, parameterNames, parameterNameMap, target, label,
      enabledExpression, name, cssClass);
  }

  public void serialize(final XmlWriter out, final Object object, final HtmlUiBuilder<?> uiBuilder,
    final List<String> parameterNames, final Map<String, String> parameterNameMap,
    final String target, final String label, final Expression enabledExpression, final String name,
    String cssClass) {
    try {
      final Map<String, Object> parameters = new HashMap<>();
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
        if (!ExpressionUtils.evaluateAsBoolean(enabledExpression, evaluationContext)) {
          return;
        }
      }
      final Object id = uiBuilder.getIdValue(object);
      parameters.put(uiBuilder.getIdParameterName(), id);

      final String actionUrl = uiBuilder.getPageUrl(name, parameters);
      if (actionUrl != null) {
        out.startTag(HtmlElem.FORM);
        out.attribute(HtmlAttr.ACTION, actionUrl);
        out.attribute(HtmlAttr.METHOD, "post");
        out.attribute(HtmlAttr.TARGET, target);
        final String lowerLabel = label.toLowerCase();
        final HttpServletRequest request = HttpServletUtils.getRequest();
        for (final String parameterName : Arrays.asList("plain", "htmlCss")) {
          HtmlUtil.serializeHiddenInput(out, parameterName, request.getParameter(parameterName));
        }
        if (!Property.hasValue(cssClass)) {
          cssClass = lowerLabel;
        }

        out.startTag(HtmlElem.BUTTON);
        out.attribute(HtmlAttr.CLASS, "btn btn-default btn-xs");
        out.attribute(HtmlAttr.TYPE, "submit");
        out.attribute(HtmlAttr.NAME, lowerLabel);
        Aria.label(out, label);
        if (Property.hasValue(this.iconName)) {
          BootstrapUtil.icon(out, this.iconName);
          HtmlUtil.serializeSpan(out, "sr-only", label);
        } else {
          out.text(label);
        }
        out.endTag(HtmlElem.BUTTON);

        out.endTag(HtmlElem.FORM);
      }
    } catch (final Throwable t) {
      Logs.error(ActionFormKeySerializer.class, "Unable to serialize", t);
    }
  }

  public ActionFormKeySerializer setCssClass(final String cssClass) {
    this.cssClass = cssClass;
    return this;
  }

  public ActionFormKeySerializer setEnabledExpression(final String enabledExpression) {
    this.enabledExpression = new SpelExpressionParser().parseExpression(enabledExpression);
    return this;
  }

  @Override
  public void setHtmlUiBuilder(final HtmlUiBuilder<?> uiBuilder) {
    this.uiBuilder = uiBuilder;
  }

  public ActionFormKeySerializer setIconName(final String iconName) {
    this.iconName = iconName;
    return this;
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

  public ActionFormKeySerializer setTarget(final String target) {
    this.target = target;
    return this;
  }
}
