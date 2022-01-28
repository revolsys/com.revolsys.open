package com.revolsys.record.io.format.xml.stax;

import com.revolsys.record.io.format.xml.XmlName;
import com.revolsys.record.io.format.xml.XmlNameProxy;
import com.revolsys.record.io.format.xml.XmlSimpleType;

public class StaxAttributeReader implements XmlNameProxy {

  private final XmlName elementName;

  private final String name;

  private final XmlSimpleType type;

  public StaxAttributeReader(final XmlName elementName, final String name,
    final XmlSimpleType type) {
    this.elementName = elementName;
    this.name = name;
    this.type = type;
  }

  public Object getAttributeValue(final StaxReader in, final int attributeIndex) {
    final String value = in.getAttributeValue(attributeIndex);
    if (value == null) {
      return null;
    } else {
      return this.type.toValue(value);
    }
  }

  public String getName() {
    return this.name;
  }

  @Override
  public XmlName getXmlName() {
    return this.elementName;
  }

}
