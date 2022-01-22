package com.revolsys.record.io.format.xml;

import javax.xml.namespace.QName;

public class XmlElementName extends QName implements XmlElementNameProxy {

  public XmlElementName(final String localPart) {
    super(localPart);
  }

  public XmlElementName(final String namespaceURI, final String localPart) {
    super(namespaceURI, localPart);
  }

  public XmlElementName(final String namespaceURI, final String localPart, final String prefix) {
    super(namespaceURI, localPart, prefix);
  }

  @Override
  public XmlElementName getElementName() {
    return this;
  }
}
