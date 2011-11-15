package com.revolsys.io.xml;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

public class QNameFactory {

  private String namespaceUri;

  private Map<String, QName> qNames = new HashMap<String, QName>();

  public QNameFactory(
    String namespaceUri) {
    this.namespaceUri = namespaceUri;
  }

  public QName getQName(
    final String localPart) {
    QName qName = qNames.get(localPart);
    if (qName == null) {
      qName = new QName(namespaceUri, localPart);
      qNames.put(localPart, qName);
    }
    return qName;
  }
}
