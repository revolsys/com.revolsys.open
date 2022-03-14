package com.revolsys.record.io.format.xml.stax;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.record.io.format.xml.XmlConstants;
import com.revolsys.record.io.format.xml.XmlName;
import com.revolsys.record.io.format.xml.XmlNameProxy;
import com.revolsys.record.io.format.xml.XmlNamespace;
import com.revolsys.record.io.format.xml.XmlSimpleType;
import com.revolsys.record.io.format.xml.XmlSimpleTypeDataType;

public class StaxElementHandler<V> implements XmlNameProxy {

  public static XmlSimpleType XSD_STRING = new XmlSimpleTypeDataType(XmlConstants.XSD, "string",
    DataTypes.STRING);

  public static XmlSimpleType XSD_DATE_TIME = new XmlSimpleTypeDataType(XmlConstants.XSD,
    "dateTime", DataTypes.INSTANT);

  public static XmlSimpleType XSD_INTEGER = new XmlSimpleTypeDataType(XmlConstants.XSD, "dateTime",
    DataTypes.INT);

  public static XmlSimpleType XSD_UNSIGNED_LONG = new XmlSimpleTypeDataType(XmlConstants.XSD,
    "dateTime", DataTypes.LONG);

  // TODO hexBinary

  private final Map<QName, StaxProperty> elements = new HashMap<>();

  private final Map<QName, StaxAttributeReader> attributes = new HashMap<>();

  private final StaxElementToObject<?> reader;

  private String propertyName;

  private XmlNamespace namespace;

  private XmlName elementName;

  private final Set<String> unhandled = new HashSet<>();

  public StaxElementHandler(final XmlNamespace namespace, final String localPart,
    final StaxElementFunctionFactory reader) {
    this(namespace, localPart, localPart, reader);
  }

  public StaxElementHandler(final XmlNamespace namespace, final String localPart,
    final StaxElementToObject<V> reader) {
    this(namespace, localPart, localPart, reader);
  }

  public StaxElementHandler(final XmlNamespace namespace, final String localPart,
    final String propertyName, final StaxElementFunctionFactory factory) {
    this.elementName = namespace.getName(localPart);
    this.namespace = namespace;
    this.propertyName = propertyName;
    this.reader = factory.getFunction(this);
  }

  public StaxElementHandler(final XmlNamespace namespace, final String localPart,
    final String propertyName, final StaxElementToObject<V> reader) {
    this.elementName = namespace.getName(localPart);
    this.namespace = namespace;
    this.propertyName = propertyName;
    this.reader = reader;
  }

  public final StaxElementHandler<V> addAttribute(final String localPart) {
    final XmlNameProxy elementName = this.namespace.getName(localPart);
    return addAttribute(elementName);
  }

  public final StaxElementHandler<V> addAttribute(final String localPart, final String name) {
    final XmlNameProxy elementName = this.namespace.getName(localPart);
    return addAttribute(elementName, name);
  }

  public final StaxElementHandler<V> addAttribute(final String localPart, final String name,
    final XmlSimpleType type) {
    final XmlNameProxy elementName = this.namespace.getName(localPart);
    return addAttribute(elementName, name, type);
  }

  public final StaxElementHandler<V> addAttribute(final String localPart,
    final XmlSimpleType type) {
    final XmlNameProxy elementName = this.namespace.getName(localPart);
    return addAttribute(elementName, type);
  }

  public final StaxElementHandler<V> addAttribute(final XmlNameProxy elementName) {
    return addAttribute(elementName, XSD_STRING);
  }

  public final StaxElementHandler<V> addAttribute(final XmlNameProxy elementName,
    final String name) {
    return addAttribute(elementName, name, XSD_STRING);
  }

  public final StaxElementHandler<V> addAttribute(final XmlNameProxy element, final String name,
    final XmlSimpleType type) {
    final XmlName elementName = element.getXmlName();
    final StaxAttributeReader attribute = new StaxAttributeReader(elementName, name, type);
    this.attributes.put(elementName, attribute);
    return this;
  }

  public final StaxElementHandler<V> addAttribute(final XmlNameProxy elementName,
    final XmlSimpleType type) {
    final String name = elementName.getLocalPart();
    return addAttribute(elementName, name, type);
  }

  public final StaxElementHandler<V> addAttribute(final XmlNamespace namespace,
    final String localPart, final XmlSimpleType type) {
    final XmlNameProxy elementName = namespace.getName(localPart);
    return addAttribute(elementName, type);
  }

  public final StaxElementHandler<V> addElement(final StaxElementHandler<?> element) {
    final XmlNameProxy elementName = element.getXmlName();
    final String propertyName = element.propertyName;
    final StaxElementToObject<?> handler = element.reader;
    return addElement(elementName, propertyName, handler, false);
  }

  public final StaxElementHandler<V> addElement(final String localPart,
    final StaxElementToObject<?> handler) {
    final XmlNameProxy elementName = this.namespace.getName(localPart);
    return addElement(elementName, handler);
  }

  public final StaxElementHandler<V> addElement(final String localPart, final String name,
    final StaxElementToObject<?> handler) {
    final XmlNameProxy elementName = this.namespace.getName(localPart);
    return addElement(elementName, name, handler);
  }

  public final StaxElementHandler<V> addElement(final XmlNameProxy element,
    final StaxElementToObject<?> handler) {
    final XmlName elementName = element.getXmlName();
    final String name = elementName.getLocalPart();
    return addElement(elementName, name, handler);
  }

  public final StaxElementHandler<V> addElement(final XmlNameProxy elementName, final String name,
    final StaxElementToObject<? extends Object> handler) {
    return addElement(elementName, name, handler, false);
  }

  private StaxElementHandler<V> addElement(final XmlNameProxy element, final String name,
    final StaxElementToObject<? extends Object> handler, final boolean list) {
    final XmlName elementName = element.getXmlName();
    final StaxProperty property = new StaxProperty(elementName, name, handler, list);
    this.elements.put(elementName, property);
    return this;
  }

  public final StaxElementHandler<V> addElement(final XmlNamespace namespace,
    final String localPart, final StaxElementToObject<?> handler) {
    final XmlNameProxy elementName = namespace.getName(localPart);
    return addElement(elementName, handler);
  }

  public final StaxElementHandler<V> addElement(final XmlNamespace namespace,
    final String localPart, final String name, final StaxElementToObject<?> handler) {
    final XmlNameProxy elementName = namespace.getName(localPart);
    return addElement(elementName, name, handler);
  }

  public final StaxElementHandler<V> addElementList(final StaxElementHandler<?> element) {
    final XmlNameProxy elementName = element.getXmlName();
    final String propertyName = element.propertyName;
    final StaxElementToObject<?> handler = element.reader;
    return addElement(elementName, propertyName, handler, true);
  }

  public final StaxElementHandler<V> addElementList(final StaxElementHandler<?> element,
    final String name) {
    final XmlNameProxy elementName = element.getXmlName();
    final StaxElementToObject<?> handler = element.reader;
    return addElementList(elementName, name, handler);
  }

  public final StaxElementHandler<V> addElementList(final XmlNameProxy elementName,
    final StaxElementToObject<? extends Object> handler) {
    final String name = elementName.getLocalPart();
    return addElementList(elementName, name, handler);
  }

  public final StaxElementHandler<V> addElementList(final XmlNameProxy elementName,
    final String name, final StaxElementToObject<? extends Object> handler) {
    return addElement(elementName, name, handler, true);
  }

  public StaxAttributeReader getAttribute(final QName attributeName) {
    return this.attributes.get(attributeName);
  }

  public Object getAttributeValue(final StaxReader in, final int index) {
    final QName elementName = in.getAttributeName(index);
    return getAttributeValue(in, elementName, index);
  }

  public Object getAttributeValue(final StaxReader in, final QName elementName, final int index) {
    final StaxAttributeReader handler = this.attributes.get(elementName);
    if (handler == null) {
      return null;
    } else {
      return handler.getAttributeValue(in, index);
    }
  }

  public StaxProperty getElement(final QName elementName) {
    return this.elements.get(elementName);
  }

  @Override
  public String getLocalPart() {
    return this.elementName.getLocalPart();
  }

  @Override
  public XmlName getXmlName() {
    return this.elementName;
  }

  private void handleAttributes(final StaxReader in, final StaxElementCallback callback) {
    for (int i = 0; i < in.getAttributeCount(); i++) {
      final QName attributeName = in.getAttributeName(i);
      final StaxAttributeReader handler = getAttribute(attributeName);
      if (handler != null) {
        final Object value = handler.getAttributeValue(in, i);
        if (value != null) {
          callback.handleAttributeValue(in, i, handler, value);
        }
      } else {
        // final String key = getLocalPart() + "." +
        // attributeName.getLocalPart();
        // if (this.unhandled.add(key)) {
        // Debug.println(key);
        // }
      }
    }
  }

  @SuppressWarnings("unchecked")
  public V handleElement(final StaxReader in) {
    return (V)this.reader.toObject(in);
  }

  public void handleElement(final StaxReader in, final StaxElementCallback callback) {
    if (in.getEventType() != XMLStreamConstants.START_ELEMENT) {
      in.skipToStartElement();
      if (in.getEventType() != XMLStreamConstants.START_ELEMENT) {
        return;
      }
    }
    handleAttributes(in, callback);
    handleElements(in, callback);
  }

  protected void handleElements(final StaxReader in, final StaxElementCallback callback) {
    final int depth = in.getDepth();
    while (in.skipToStartElement(depth)) {
      final QName elementName = in.getName();
      final StaxProperty handler = getElement(elementName);
      if (handler != null) {
        handler.handleElement(in, callback);
      } else {
        // final String key = getLocalPart() + "." + elementName.getLocalPart();
        // if (this.unhandled.add(key)) {
        // Debug.println(key);
        // }
      }
    }
  }

  public <J extends StaxJsonObject> StaxElementToObject<J> jsonFactory(
    final Supplier<J> constructor) {
    return in -> {
      final J json = constructor.get();
      json.initFromElementHandler(in, this);
      return json;
    };

  }

  @Override
  public String toString() {
    return this.elementName.toString();
  }

}
