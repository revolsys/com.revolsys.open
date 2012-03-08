package com.revolsys.ui.html.serializer;

import java.io.IOException;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.builder.HtmlUiBuilder;
import com.revolsys.ui.html.builder.HtmlUiBuilderAware;
import com.revolsys.ui.html.serializer.key.AbstractKeySerializer;
import com.revolsys.ui.html.serializer.type.TypeSerializer;

public class BuilderSerializer extends AbstractKeySerializer implements
  TypeSerializer, HtmlUiBuilderAware<HtmlUiBuilder<?>> {
  /** The HTML UI Builder */
  private HtmlUiBuilder<?> builder;

  public BuilderSerializer() {
  }

  /**
   * Construt a new HtmlUiBuilderMethodSerializer.
   * 
   * @param builder The HTML UI Builder the method is on.
   */
  public BuilderSerializer(final String name, final HtmlUiBuilder<?> builder) {
    super(name, builder.getLabel(name));
    this.builder = builder;
  }

  public void setHtmlUiBuilder(HtmlUiBuilder<?> uiBuilder) {
    if (this.builder != null) {
      this.builder = uiBuilder;
    }
  }

  /**
   * Serialize the value to the XML writer.
   * 
   * @param out The XML writer to serialize to.
   * @param value The object to get the value from.
   * @throws IOException If there was an I/O error serializing the value.
   */
  public void serialize(final XmlWriter out, final Object value) {
    builder.serialize(out, value, getName());
  }
}
