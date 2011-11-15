package com.revolsys.io.xml;

import java.util.Comparator;

import javax.xml.namespace.QName;

public class QNameComparator implements Comparator<QName> {
  public int compare(
    final QName name1,
    final QName name2) {
    final String s1 = name1.toString();
    final String s2 = name2.toString();
    return s1.compareTo(s2);
  }
}
