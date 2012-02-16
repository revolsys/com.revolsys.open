package com.revolsys.ui.html.serializer.key;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.builder.HtmlUiBuilder;
import com.revolsys.ui.html.builder.HtmlUiBuilderAware;
import com.revolsys.util.JavaBeanUtil;

public class PageLinkKeySerializer extends AbstractKeySerializer implements
  HtmlUiBuilderAware<HtmlUiBuilder<?>> {
  private String pageName;

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
    for (int i = 0; i < parts.length - 1; i++) {
      final String keyName = parts[i];
      currentObject = JavaBeanUtil.getValue(currentObject, keyName);
      if (currentObject == null) {
        uiBuilder.serializeNullLabel(out, keyName);
        return;
      }

      uiBuilder = uiBuilder.getBuilder(currentObject);
      if (uiBuilder == null) {
        final String message = currentObject.getClass().getName()
          + " does not have a property " + keyName;
        out.element(HtmlUtil.B, message);
      }
    }
    uiBuilder.serializeLink(out, pageName, currentObject);
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
