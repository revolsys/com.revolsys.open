package com.revolsys.record.io.format.xml;

public class XmlElement implements XmlNameProxy {

  private final XmlName xmlName;

  private final XmlType type;

  private final boolean list;

  public XmlElement(final XmlName xmlName, final XmlType type) {
    this(xmlName, type, false);
  }

  public XmlElement(final XmlName xmlName, final XmlType type, final boolean list) {
    this.xmlName = xmlName;
    this.type = type;
    this.list = list;
  }

  public XmlElement(final XmlNamespace namespace, final String localPart, final XmlType type) {
    this(namespace.getName(localPart), type, false);
  }

  public XmlType getType() {
    return this.type;
  }

  @Override
  public XmlName getXmlName() {
    return this.xmlName;
  }

  public boolean isList() {
    return this.list;
  }

  @Override
  public String toString() {
    return this.xmlName.toString();
  }
}
