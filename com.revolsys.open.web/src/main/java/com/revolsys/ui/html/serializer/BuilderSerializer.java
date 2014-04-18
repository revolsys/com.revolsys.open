package com.revolsys.ui.html.serializer;

import java.io.IOException;

import org.springframework.util.StringUtils;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.builder.HtmlUiBuilder;
import com.revolsys.ui.html.builder.HtmlUiBuilderAware;
import com.revolsys.ui.html.serializer.key.AbstractKeySerializer;
import com.revolsys.ui.html.serializer.type.TypeSerializer;

public class BuilderSerializer extends AbstractKeySerializer implements
  TypeSerializer, HtmlUiBuilderAware<HtmlUiBuilder<?>> {
  /** The HTML UI Builder */
  private HtmlUiBuilder<?> builder;

  private String methodName;

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

  public String getMethodName() {
    if (StringUtils.hasText(methodName)) {
      return methodName;
    } else {
      return getName();
    }
  }

  /**
   * Serialize the value to the XML writer.
   * 
   * @param out The XML writer to serialize to.
   * @param value The object to get the value from.
   * @throws IOException If there was an I/O error serializing the value.
   */
  @Override
  public void serialize(final XmlWriter out, final Object value) {
    if (builder == null) {
      out.text("-");
    } else {
      builder.serialize(out, value, getMethodName());
    }
  }

  @Override
  public void setHtmlUiBuilder(final HtmlUiBuilder<?> uiBuilder) {
    if (uiBuilder != null) {
      this.builder = uiBuilder;
    }
  }

  public void setMethodName(final String methodName) {
    this.methodName = methodName;
  }
}
