package com.revolsys.record.io.format.xml.stax;

public interface StaxElementToObject<V> {

  V toObject(final StaxReader in);
}
