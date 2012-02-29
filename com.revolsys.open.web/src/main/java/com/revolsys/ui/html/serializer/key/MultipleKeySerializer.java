package com.revolsys.ui.html.serializer.key;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.builder.HtmlUiBuilder;
import com.revolsys.ui.html.builder.HtmlUiBuilderAware;

public class MultipleKeySerializer extends AbstractKeySerializer implements
  HtmlUiBuilderAware<HtmlUiBuilder<?>> {
  private List<KeySerializer> serializers = new ArrayList<KeySerializer>();

  private HtmlUiBuilder<?> uiBuilder;

  public MultipleKeySerializer() {
  }

  public MultipleKeySerializer(String name, String label) {
    super(name, label);
  }

  public MultipleKeySerializer(String name) {
    super(name);
  }

  public void serialize(XmlWriter out, Object object) {
    for (KeySerializer serializer : serializers) {
      serializer.serialize(out, object);
    }
  }

  public List<KeySerializer> getSerializers() {
    return serializers;
  }

  public void setSerializers(List<KeySerializer> serializers) {
    this.serializers = serializers;
    setHtmlUiBuilder(uiBuilder);
  }

  @SuppressWarnings("unchecked")
  public void setHtmlUiBuilder(HtmlUiBuilder<?> uiBuilder) {
    this.uiBuilder = uiBuilder;
    for (KeySerializer serializer : serializers) {
      if (serializer instanceof HtmlUiBuilderAware) {
        HtmlUiBuilderAware<HtmlUiBuilder<?>> builderAware = (HtmlUiBuilderAware<HtmlUiBuilder<?>>)serializer;
        builderAware.setHtmlUiBuilder(uiBuilder);
      }
    }
  }
}
