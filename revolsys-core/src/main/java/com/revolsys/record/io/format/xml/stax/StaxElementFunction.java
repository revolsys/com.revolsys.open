package com.revolsys.record.io.format.xml.stax;

public interface StaxElementFunction<V> {

  V handle(final StaxReader in, final StaxElementCallback callback);
}
