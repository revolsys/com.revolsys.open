package com.revolsys.io.xml;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DomUtil {
  public static void appendChildText(final StringBuffer text, final Node node) {
    if (node != null) {
      for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
        final short type = child.getNodeType();
        if (type == Node.TEXT_NODE) {
          final String childText = child.getNodeValue();
          text.append(childText);
        } else if (type == Node.CDATA_SECTION_NODE) {
          appendChildText(text, child);
        }
      }
    }

  }

  public static String getChildText(final Node node) {
    if (node == null) {
      return null;
    } else {
      final StringBuffer text = new StringBuffer();
      appendChildText(text, node);
      return text.toString();
    }
  }

  public static Double getDouble(final Node node) {
    final String text = getChildText(node);
    if (StringUtils.hasText(text)) {
      return Double.valueOf(text);
    } else {
      return null;
    }
  }

  public static List<Double> getDoubleList(final Document doc,
    final String elemName) {
    final List<Double> values = new ArrayList<Double>();
    final NodeList nodes = doc.getElementsByTagName(elemName);
    for (int i = 0; i < nodes.getLength(); i++) {
      final Node node = nodes.item(i);
      for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
        if (child.getNodeType() == Node.ELEMENT_NODE) {
          final Double value = DomUtil.getDouble(child);
          values.add(value);
        }
      }
    }
    return values;
  }

  public static Element getFirstChildElement(final Node parent,
    final String elemName) {
    for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
      final short nodeType = child.getNodeType();
      if (nodeType == Node.ELEMENT_NODE) {
        final String nodeName = child.getNodeName();
        if (nodeName.equals(elemName)) {
          return (Element)child;
        }
      }
    }
    return null;
  }

  public static Integer getInteger(final Node node) {
    final String text = getChildText(node);
    if (StringUtils.hasText(text)) {
      return Integer.valueOf(text);
    } else {
      return null;
    }
  }
}
