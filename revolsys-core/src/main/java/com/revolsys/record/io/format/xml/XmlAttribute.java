package com.revolsys.record.io.format.xml;

public class XmlAttribute implements XmlNameProxy {

  private final XmlName xmlName;

  private final XmlSimpleType type;

  public XmlAttribute(final XmlName xmlName, final XmlSimpleType type) {
    this.xmlName = xmlName;
    this.type = type;
  }

  public XmlSimpleType getType() {
    return this.type;
  }

  @Override
  public XmlName getXmlName() {
    return this.xmlName;
  }
}
