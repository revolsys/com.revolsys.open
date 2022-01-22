package com.revolsys.record.io.format.xml.stax;

public interface StaxElementFunctionFactory {

  StaxElementFunction<?> getFunction(StaxElementHandler<?> element);
}
