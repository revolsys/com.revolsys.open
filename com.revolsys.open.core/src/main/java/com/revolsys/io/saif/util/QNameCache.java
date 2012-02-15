package com.revolsys.io.saif.util;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

public final class QNameCache {

  private static final Map<String, QName> NAME_MAP = new HashMap<String, QName>();

  public static synchronized QName getName(final String name) {
    if (name != null) {
      QName qName = NAME_MAP.get(name);
      if (qName == null) {
        final int index = name.indexOf("::");
        if (index != -1) {
          final String localPart = name.substring(0, index);
          final String namespace = name.substring(index + 2);
          qName = new QName(namespace, localPart);
        } else {
          qName = new QName(name);
        }
        NAME_MAP.put(name, qName);
      }
      return qName;
    } else {
      return null;
    }
  }

  private QNameCache() {
  }
}
