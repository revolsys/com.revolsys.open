package com.revolsys.record.io.format.xml.stax;

public interface StaxElementCallback {
  default void handleAttributeValue(final StaxReader in, final int i,
    final StaxAttributeReader handler, final Object value) {
  }

  default void handleElement(final StaxReader in, final StaxProperty handler,
    final StaxElementCallback callback) {
    handler.handleElementValue(in, callback);
  }
}
