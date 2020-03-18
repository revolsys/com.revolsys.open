package com.revolsys.ui.html.serializer.key;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jeometry.common.data.type.DataTypes;
import org.slf4j.LoggerFactory;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.builder.HtmlUiBuilder;
import com.revolsys.ui.html.builder.HtmlUiBuilderAware;

public class PageLinkKeySerializer extends AbstractKeySerializer
  implements HtmlUiBuilderAware<HtmlUiBuilder<?>> {
  private String pageName;

  private final Map<String, String> parameterKeys = new LinkedHashMap<>();

  private HtmlUiBuilder<?> uiBuilder;

  public PageLinkKeySerializer() {
  }

  public PageLinkKeySerializer(final String name, final String key, final String label,
    final String pageName) {
    super(name, label);
    setKey(key);
    setPageName(pageName);
  }

  public PageLinkKeySerializer addParameterKey(final String name, final String key) {
    this.parameterKeys.put(name, key);
    return this;
  }

  public String getPageName() {
    return this.pageName;
  }

  public Map<String, String> getParameterKeys() {
    return this.parameterKeys;
  }

  public HtmlUiBuilder<?> getUiBuilder() {
    return this.uiBuilder;
  }

  @Override
  public void serialize(final XmlWriter out, final Object object) {
    try {
      HtmlUiBuilder<? extends Object> uiBuilder = this.uiBuilder;
      final String[] parts = getKey().split("\\.");
      Object currentObject = object;
      String key = parts[0];
      for (int i = 0; i < parts.length - 1; i++) {
        currentObject = uiBuilder.getProperty(currentObject, key);
        if (currentObject == null) {
          uiBuilder.serializeNullLabel(out, key);
          return;
        }

        uiBuilder = uiBuilder.getBuilder(currentObject);
        if (uiBuilder == null) {
          final Object value = currentObject;
          out.write(DataTypes.toString(value));
          return;
        }
        key = parts[i + 1];

      }
      uiBuilder.serializeLink(out, currentObject, key, this.pageName, this.parameterKeys);
    } catch (final Throwable e) {
      LoggerFactory.getLogger(getClass()).error("Unable to serialize " + this.pageName, e);
    }
  }

  public void serializeValue(final XmlWriter out) {
    out.text(getLabel());
  }

  @Override
  public void setHtmlUiBuilder(final HtmlUiBuilder<?> uiBuilder) {
    if (this.uiBuilder == null) {
      this.uiBuilder = uiBuilder;
    }
  }

  public void setPageName(final String pageName) {
    this.pageName = pageName;
  }

  public PageLinkKeySerializer setParameterKeys(final Map<String, String> parameterKeys) {
    this.parameterKeys.putAll(parameterKeys);
    return this;
  }

  public PageLinkKeySerializer setUiBuilder(final HtmlUiBuilder<?> uiBuilder) {
    this.uiBuilder = uiBuilder;
    return this;
  }
}
