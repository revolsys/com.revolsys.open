package com.revolsys.record.io.format.xml;

import javax.xml.namespace.QName;

public interface XmlNameProxy {
  default boolean equalsXmlName(final QName qName) {
    final XmlName xmlName = getXmlName();
    return xmlName.equals(qName);
  }

  default String getLocalPart() {
    return getXmlName().getLocalPart();
  }

  default XmlNamespace getNamespace() {
    return getXmlName().getNamespace();
  }

  default String getNamespaceURI() {
    return getXmlName().getNamespaceURI();
  }

  XmlName getXmlName();
}
