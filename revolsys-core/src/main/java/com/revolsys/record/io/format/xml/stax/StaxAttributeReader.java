package com.revolsys.record.io.format.xml.stax;

import java.util.function.Function;

import com.revolsys.record.io.format.xml.XmlElementName;
import com.revolsys.record.io.format.xml.XmlElementNameProxy;

public class StaxAttributeReader implements XmlElementNameProxy {

  private final XmlElementName elementName;

  private final String name;

  private final Function<String, ? extends Object> converter;

  public StaxAttributeReader(final XmlElementName elementName, final String name,
    final Function<String, ? extends Object> converter) {
    this.elementName = elementName;
    this.name = name;
    this.converter = converter;
  }

  public Object getAttributeValue(final StaxReader in, final int attributeIndex) {
    final String value = in.getAttributeValue(attributeIndex);
    if (value == null) {
      return null;
    } else {
      return this.converter.apply(value);
    }
  }

  @Override
  public XmlElementName getElementName() {
    return this.elementName;
  }

  public String getName() {
    return this.name;
  }

}
