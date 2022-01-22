package com.revolsys.record.io.format.xml;

public interface XmlElementNameProxy {
  XmlElementName getElementName();

  default String getLocalPart() {
    return getElementName().getLocalPart();
  }

  default String getNamespaceURI() {
    return getElementName().getNamespaceURI();
  }
}
