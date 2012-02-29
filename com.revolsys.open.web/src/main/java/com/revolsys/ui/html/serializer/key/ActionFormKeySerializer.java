package com.revolsys.ui.html.serializer.key;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.builder.HtmlUiBuilder;
import com.revolsys.ui.html.builder.HtmlUiBuilderAware;
import com.revolsys.util.JavaBeanUtil;

public class ActionFormKeySerializer extends AbstractKeySerializer implements
  HtmlUiBuilderAware<HtmlUiBuilder<?>> {
  private List<String> parameterNames = new ArrayList<String>();

  private HtmlUiBuilder<?> uiBuilder;

  public void serialize(final XmlWriter out, final Object object) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    for (String name : parameterNames) {
      Object value = JavaBeanUtil.getValue(object, name);
      parameters.put(name, value);
    }
    String actionUrl = uiBuilder.getPageUrl(getName(), parameters);
    out.startTag(HtmlUtil.FORM);
    out.attribute(HtmlUtil.ATTR_ACTION, actionUrl);
    out.attribute(HtmlUtil.ATTR_METHOD, "post");
    String lowerLabel = getLabel().toLowerCase();
    HtmlUtil.serializeButton(out, lowerLabel, "submit", getLabel(), getLabel(),
      lowerLabel);
    out.endTag(HtmlUtil.FORM);
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

}
