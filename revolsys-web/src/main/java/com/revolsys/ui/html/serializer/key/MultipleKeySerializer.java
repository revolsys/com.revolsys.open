package com.revolsys.ui.html.serializer.key;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.builder.HtmlUiBuilder;
import com.revolsys.ui.html.builder.HtmlUiBuilderAware;

public class MultipleKeySerializer extends AbstractKeySerializer
  implements HtmlUiBuilderAware<HtmlUiBuilder<?>> {
  private List<KeySerializer> serializers = new ArrayList<>();

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

  public MultipleKeySerializer addSerializer(final KeySerializer serializer) {
    this.serializers.add(serializer);
    return this;
  }

  public List<KeySerializer> getSerializers() {
    return this.serializers;
  }

  @Override
  public void serialize(final XmlWriter out, final Object object) {
    for (final KeySerializer serializer : this.serializers) {
      serializer.serialize(out, object);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void setHtmlUiBuilder(final HtmlUiBuilder<?> uiBuilder) {
    this.uiBuilder = uiBuilder;
    for (final KeySerializer serializer : this.serializers) {
      if (serializer instanceof HtmlUiBuilderAware) {
        final HtmlUiBuilderAware<HtmlUiBuilder<?>> builderAware = (HtmlUiBuilderAware<HtmlUiBuilder<?>>)serializer;
        builderAware.setHtmlUiBuilder(uiBuilder);
      }
    }
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

  public void setSerializers(final List<KeySerializer> serializers) {
    this.serializers = serializers;
    setHtmlUiBuilder(this.uiBuilder);
  }
}
