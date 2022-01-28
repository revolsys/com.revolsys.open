package com.revolsys.record.io.format.xml;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

public class XmlComplexType extends XmlType {

  private final Map<QName, XmlAttribute> attributes = new HashMap<>();

  private final Map<QName, XmlElement> elements = new HashMap<>();

  public XmlComplexType(final XmlName xmlName) {
    super(xmlName);
  }

  public XmlComplexType(final XmlNamespace namespace, final String localPart) {
    super(namespace, localPart);
  }

  public final XmlComplexType addAttribute(final String localPart) {
    final XmlNameProxy xmlName = getNamespace().getName(localPart);
    return addAttribute(xmlName);
  }

  public final XmlComplexType addAttribute(final String localPart, final XmlSimpleType type) {
    final XmlNameProxy xmlName = getNamespace().getName(localPart);
    return addAttribute(xmlName, type);
  }

  public final XmlComplexType addAttribute(final XmlNameProxy xmlName) {
    return addAttribute(xmlName, XSD.STRING);
  }

  public final XmlComplexType addAttribute(final XmlNameProxy element, final XmlSimpleType type) {
    final XmlName xmlName = element.getXmlName();
    final XmlAttribute attribute = new XmlAttribute(xmlName, type);
    this.attributes.put(xmlName, attribute);
    return this;
  }

  public final XmlComplexType addAttribute(final XmlNamespace namespace, final String localPart,
    final XmlSimpleType type) {
    final XmlNameProxy xmlName = namespace.getName(localPart);
    return addAttribute(xmlName, type);
  }

  public final XmlComplexType addElement(final String localPart, final XmlType type) {
    final XmlNameProxy xmlName = getNamespace().getName(localPart);
    return addElement(xmlName, type);
  }

  public final XmlComplexType addElement(final XmlNameProxy xmlName, final XmlType type) {
    return addElement(xmlName, type, false);
  }

  private XmlComplexType addElement(final XmlNameProxy element, final XmlType type,
    final boolean list) {
    final XmlName xmlName = element.getXmlName();
    final XmlElement property = new XmlElement(xmlName, type, list);
    this.elements.put(xmlName, property);
    return this;
  }

  public final XmlComplexType addElement(final XmlNamespace namespace, final String localPart,
    final XmlType type) {
    final XmlNameProxy xmlName = namespace.getName(localPart);
    return addElement(xmlName, type);
  }

  public final XmlComplexType addElementList(final String localPart, final XmlType type) {
    final XmlNameProxy xmlName = getNamespace().getName(localPart);
    return addElementList(xmlName, type);
  }

  public final XmlComplexType addElementList(final XmlNameProxy xmlName, final XmlType type) {
    return addElement(xmlName, type, true);
  }

  public final XmlComplexType addElementList(final XmlNamespace namespace, final String localPart,
    final XmlType type) {
    final XmlNameProxy xmlName = namespace.getName(localPart);
    return addElementList(xmlName, type);
  }

  public XmlAttribute getAttribute(final QName attributeName) {
    return this.attributes.get(attributeName);
  }

  public XmlElement getElement(final QName xmlName) {
    return this.elements.get(xmlName);
  }

}
