package com.revolsys.record.io.format.xml;

public abstract class XmlSimpleType extends XmlType {

  public XmlSimpleType(final XmlName xmlName) {
    super(xmlName);
  }

  public XmlSimpleType(final XmlNamespace namespace, final String localPart) {
    this(namespace.getName(localPart));
  }

  public abstract <V> V toValue(String text);
}
