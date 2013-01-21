package com.revolsys.ui.html.serializer.key;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.builder.HtmlUiBuilder;
import com.revolsys.ui.html.builder.HtmlUiBuilderAware;

public class MultipleKeySerializer extends AbstractKeySerializer implements
  HtmlUiBuilderAware<HtmlUiBuilder<?>> {
  private List<KeySerializer> serializers = new ArrayList<KeySerializer>();

  private HtmlUiBuilder<?> uiBuilder;

  public MultipleKeySerializer() {
    setProperties(null);
  }

  public MultipleKeySerializer(final String name) {
    super(name);
    setProperties(null);
  }

  public MultipleKeySerializer(final String name, final String label) {
    super(name, label);
    setProperties(null);
  }

  public List<KeySerializer> getSerializers() {
    return serializers;
  }

  public void serialize(final XmlWriter out, final Object object) {
    for (final KeySerializer serializer : serializers) {
      serializer.serialize(out, object);
    }
  }

  @Override
  public void setProperties(Map<String, ? extends Object> properties) {
    getProperties().clear();
    if (properties != null) {
      getProperties().putAll(properties);
    }
    setProperty("sortable", false);
    setProperty("searchable", false);
  }

  @SuppressWarnings("unchecked")
  public void setHtmlUiBuilder(final HtmlUiBuilder<?> uiBuilder) {
    this.uiBuilder = uiBuilder;
    for (final KeySerializer serializer : serializers) {
      if (serializer instanceof HtmlUiBuilderAware) {
        final HtmlUiBuilderAware<HtmlUiBuilder<?>> builderAware = (HtmlUiBuilderAware<HtmlUiBuilder<?>>)serializer;
        builderAware.setHtmlUiBuilder(uiBuilder);
      }
    }
  }

  public void setSerializers(final List<KeySerializer> serializers) {
    this.serializers = serializers;
    setHtmlUiBuilder(uiBuilder);
  }
}
