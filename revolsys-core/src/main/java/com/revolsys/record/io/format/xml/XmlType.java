package com.revolsys.record.io.format.xml;

public abstract class XmlType implements XmlNameProxy {

  private final XmlName xmlName;

  public XmlType(final XmlName xmlName) {
    this.xmlName = xmlName;
  }

  public XmlType(final XmlNamespace namespace, final String localPart) {
    this(namespace.getName(localPart));
  }

  @Override
  public XmlName getXmlName() {
    return this.xmlName;
  }

  @Override
  public String toString() {
    return this.xmlName.toString();
  }
}
