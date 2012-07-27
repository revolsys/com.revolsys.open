package com.revolsys.ui.html.serializer.key;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.builder.HtmlUiBuilder;
import com.revolsys.ui.html.builder.HtmlUiBuilderAware;

public class MultipleKeySerializer extends AbstractKeySerializer implements
  HtmlUiBuilderAware<HtmlUiBuilder<?>> {
  private List<KeySerializer> serializers = new ArrayList<KeySerializer>();

  private HtmlUiBuilder<?> uiBuilder;

  public MultipleKeySerializer() {
    setProperty("sortable", false);
    setProperty("searchable", false);
  }

  public MultipleKeySerializer(final String name) {
    super(name);
    setProperty("sortable", false);
    setProperty("searchable", false);
  }

  public MultipleKeySerializer(final String name, final String label) {
    super(name, label);
    setProperty("sortable", false);
    setProperty("searchable", false);
  }

  public List<KeySerializer> getSerializers() {
    return serializers;
  }

  public void serialize(final XmlWriter out, final Object object) {
    for (final KeySerializer serializer : serializers) {
      serializer.serialize(out, object);
    }
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
