package com.revolsys.ui.html.serializer.key;

import java.util.LinkedHashMap;
import java.util.Map;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.builder.HtmlUiBuilder;
import com.revolsys.ui.html.builder.HtmlUiBuilderAware;
import com.revolsys.util.JavaBeanUtil;

public class PageLinkKeySerializer extends AbstractKeySerializer implements
  HtmlUiBuilderAware<HtmlUiBuilder<?>> {
  private String pageName;

  private Map<String, String> parameterKeys = new LinkedHashMap<String, String>();

  public Map<String, String> getParameterKeys() {
    return parameterKeys;
  }

  public void setParameterKeys(Map<String, String> parameterKeys) {
    this.parameterKeys = parameterKeys;
  }

  private HtmlUiBuilder<?> uiBuilder;

  public String getPageName() {
    return pageName;
  }

  public HtmlUiBuilder<?> getUiBuilder() {
    return uiBuilder;
  }

  public void serialize(final XmlWriter out, final Object object) {
    HtmlUiBuilder<? extends Object> uiBuilder = this.uiBuilder;
    final String[] parts = getName().split("\\.");
    Object currentObject = object;
    String key = parts[0];
    for (int i = 0; i < parts.length - 1; i++) {
      currentObject = JavaBeanUtil.getValue(currentObject, key);
      if (currentObject == null) {
        uiBuilder.serializeNullLabel(out, key);
        return;
      }

      uiBuilder = uiBuilder.getBuilder(currentObject);
      if (uiBuilder == null) {
        final String message = currentObject.getClass().getName()
          + " does not have a property " + key;
        out.element(HtmlUtil.B, message);
        return;
      }
      key = parts[i + 1];

    }
    uiBuilder.serializeLink(out, currentObject, key, pageName, parameterKeys);
  }

  public void setHtmlUiBuilder(final HtmlUiBuilder<?> uiBuilder) {
    this.uiBuilder = uiBuilder;
  }

  public void setPageName(final String pageName) {
    this.pageName = pageName;
  }

  public void setUiBuilder(final HtmlUiBuilder<?> uiBuilder) {
    this.uiBuilder = uiBuilder;
  }
}
