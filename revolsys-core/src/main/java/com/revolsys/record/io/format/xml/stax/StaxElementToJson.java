package com.revolsys.record.io.format.xml.stax;

import com.revolsys.record.io.format.json.JsonWriter;

public class StaxElementToJson implements StaxElementCallback, StaxElementFunctionFactory {

  private final JsonWriter out;

  public StaxElementToJson(final JsonWriter out) {
    this.out = out;
  }

  @Override
  public StaxElementToObject<?> getFunction(final StaxElementHandler<?> element) {
    return (in) -> {
      element.handleElement(in, this);
      return null;
    };
  }

  @Override
  public void handleAttributeValue(final StaxReader in, final int i,
    final StaxAttributeReader handler, final Object value) {
    final String name = handler.getName();
    this.out.labelValue(name, value);
  }

  @Override
  public void handleElement(final StaxReader in, final StaxProperty handler,
    final StaxElementCallback callback) {
    this.out.startObject();
    final String name = handler.getName();
    this.out.labelValue("$t", name);
    handler.handleElementValue(in, callback);
    this.out.endObject();
  }

}
