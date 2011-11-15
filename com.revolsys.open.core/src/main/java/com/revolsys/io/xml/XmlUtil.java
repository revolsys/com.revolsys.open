package com.revolsys.io.xml;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

public class XmlUtil {

  public static QName getXmlQName(NamespaceContext context, String value) {
    if (value == null) {
      return null;
    } else {
      int colonIndex = value.indexOf(':');
      if (colonIndex == -1) {
        return new QName(value);
      } else {
        String prefix = value.substring(0, colonIndex);
        String name = value.substring(colonIndex + 1);
        String namespaceUri = context.getNamespaceURI(prefix);
        return new QName(namespaceUri, name, prefix);
      }
    }
  }
}
