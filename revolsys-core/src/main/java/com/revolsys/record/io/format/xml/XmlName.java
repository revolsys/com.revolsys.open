package com.revolsys.record.io.format.xml;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

public class XmlName extends QName implements XmlNameProxy {

  private XmlNamespace namespace;

  public XmlName(final String localPart) {
    this(XMLConstants.NULL_NS_URI, localPart, XMLConstants.DEFAULT_NS_PREFIX);
  }

  public XmlName(final String namespaceURI, final String localPart) {
    this(namespaceURI, localPart, XMLConstants.DEFAULT_NS_PREFIX);
  }

  public XmlName(final String namespaceURI, final String localPart, final String prefix) {
    super(namespaceURI, localPart, prefix);
    if (!XMLConstants.NULL_NS_URI.equals(namespaceURI)) {
      this.namespace = XmlNamespace.forUri(namespaceURI);
    }
  }

  public XmlName(final XmlNamespace namespace, final String name) {
    super(namespace.getNamespaceUri(), name);
    this.namespace = namespace;
  }

  public XmlNamespace getNamespace() {
    return this.namespace;
  }

  @Override
  public XmlName getXmlName() {
    return this;
  }
}
