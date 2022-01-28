package com.revolsys.record.io.format.xml.stax;

public interface StaxElementFunctionFactory {

  StaxElementToObject<?> getFunction(StaxElementHandler<?> element);
}
