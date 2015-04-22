package com.revolsys.format.xml;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

public class XmlUtil {

  public static QName getXmlQName(final NamespaceContext context,
    final String value) {
    if (value == null) {
      return null;
    } else {
      final int colonIndex = value.indexOf(':');
      if (colonIndex == -1) {
        return new QName(value);
      } else {
        final String prefix = value.substring(0, colonIndex);
        final String name = value.substring(colonIndex + 1);
        final String namespaceUri = context.getNamespaceURI(prefix);
        return new QName(namespaceUri, name, prefix);
      }
    }
  }
}
