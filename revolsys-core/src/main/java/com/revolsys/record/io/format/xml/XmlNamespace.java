package com.revolsys.record.io.format.xml;

import java.util.HashMap;
import java.util.Map;

public class XmlNamespace {

  private static Map<String, XmlNamespace> namespaceByUri = new HashMap<>();

  public static XmlNamespace forUri(final String namespaceUri) {
    synchronized (namespaceByUri) {

      XmlNamespace namespace = namespaceByUri.get(namespaceUri);
      if (namespace == null) {
        namespace = new XmlNamespace(namespaceUri);
        namespaceByUri.put(namespaceUri, namespace);
      }
      return namespace;
    }
  }

  private final String namespaceUri;

  private final Map<String, XmlName> nameByLocalPart = new HashMap<>();

  public XmlNamespace(final String namespaceUri) {
    this.namespaceUri = namespaceUri;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof XmlNamespace) {
      final XmlNamespace namespace = (XmlNamespace)o;
      return namespace.namespaceUri.equals(this.namespaceUri);
    } else {
      return false;
    }
  }

  public synchronized XmlName getName(final String name) {
    XmlName element = this.nameByLocalPart.get(name);
    if (element == null) {
      element = new XmlName(this, name);
      this.nameByLocalPart.put(name, element);
    }
    return element;
  }

  public String getNamespaceUri() {
    return this.namespaceUri;
  }

  @Override
  public String toString() {
    return this.namespaceUri;
  }
}
