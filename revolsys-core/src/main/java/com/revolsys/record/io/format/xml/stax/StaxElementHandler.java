package com.revolsys.record.io.format.xml.stax;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.xml.namespace.QName;

import com.revolsys.record.io.format.xml.XmlElementName;
import com.revolsys.record.io.format.xml.XmlElementNameProxy;
import com.revolsys.record.io.format.xml.XmlNamespace;
import com.revolsys.util.Debug;

public class StaxElementHandler<V> implements XmlElementNameProxy {

  public static Function<String, ? extends Object> TO_STRING = value -> value;

  private final Map<QName, StaxProperty> elements = new HashMap<>();

  private final Map<QName, StaxAttributeReader> attributes = new HashMap<>();

  private final StaxElementFunction<?> reader;

  private String propertyName;

  private XmlNamespace namespace;

  private XmlElementName elementName;

  private final Set<String> unhandled = new HashSet<>();

  public StaxElementHandler(final XmlNamespace namespace, final String localPart,
    final StaxElementFunction<V> reader) {
    this(namespace, localPart, localPart, reader);
  }

  public StaxElementHandler(final XmlNamespace namespace, final String localPart,
    final StaxElementFunctionFactory reader) {
    this(namespace, localPart, localPart, reader);
  }

  public StaxElementHandler(final XmlNamespace namespace, final String localPart,
    final String propertyName, final Function<StaxReader, V> reader) {
    this.elementName = namespace.getElementName(localPart);
    this.namespace = namespace;
    this.propertyName = propertyName;
    this.reader = (in, callback) -> reader.apply(in);
  }

  public StaxElementHandler(final XmlNamespace namespace, final String localPart,
    final String propertyName, final StaxElementFunction<V> reader) {
    this.elementName = namespace.getElementName(localPart);
    this.namespace = namespace;
    this.propertyName = propertyName;
    this.reader = reader;
  }

  public StaxElementHandler(final XmlNamespace namespace, final String localPart,
    final String propertyName, final StaxElementFunctionFactory factory) {
    this.elementName = namespace.getElementName(localPart);
    this.namespace = namespace;
    this.propertyName = propertyName;
    this.reader = factory.getFunction(this);
  }

  public final StaxElementHandler<V> addAttribute(final String localPart) {
    final XmlElementNameProxy elementName = this.namespace.getElementName(localPart);
    return addAttribute(elementName);
  }

  public final StaxElementHandler<V> addAttribute(final String localPart,
    final Function<String, ? extends Object> handler) {
    final XmlElementNameProxy elementName = this.namespace.getElementName(localPart);
    return addAttribute(elementName, handler);
  }

  public final StaxElementHandler<V> addAttribute(final String localPart, final String name) {
    final XmlElementNameProxy elementName = this.namespace.getElementName(localPart);
    return addAttribute(elementName, name);
  }

  public final StaxElementHandler<V> addAttribute(final String localPart, final String name,
    final Function<String, ? extends Object> handler) {
    final XmlElementNameProxy elementName = this.namespace.getElementName(localPart);
    return addAttribute(elementName, name, handler);
  }

  public final StaxElementHandler<V> addAttribute(final XmlElementNameProxy elementName) {
    return addAttribute(elementName, TO_STRING);
  }

  public final StaxElementHandler<V> addAttribute(final XmlElementNameProxy elementName,
    final Function<String, ? extends Object> handler) {
    final String name = elementName.getLocalPart();
    return addAttribute(elementName, name, handler);
  }

  public final StaxElementHandler<V> addAttribute(final XmlElementNameProxy elementName,
    final String name) {
    return addAttribute(elementName, name, TO_STRING);
  }

  public final StaxElementHandler<V> addAttribute(final XmlElementNameProxy element,
    final String name, final Function<String, ? extends Object> handler) {
    final XmlElementName elementName = element.getElementName();
    final StaxAttributeReader attribute = new StaxAttributeReader(elementName, name, handler);
    this.attributes.put(elementName, attribute);
    return this;
  }

  public final StaxElementHandler<V> addAttribute(final XmlNamespace namespace,
    final String localPart, final Function<String, ? extends Object> handler) {
    final XmlElementNameProxy elementName = namespace.getElementName(localPart);
    return addAttribute(elementName, handler);
  }

  public final StaxElementHandler<V> addElement(final StaxElementHandler<?> element) {
    final XmlElementNameProxy elementName = element.getElementName();
    final String propertyName = element.propertyName;
    final StaxElementFunction<?> handler = element.reader;
    return addElement(elementName, propertyName, handler, false);
  }

  public final StaxElementHandler<V> addElement(final String localPart,
    final StaxElementFunction<?> handler) {
    final XmlElementNameProxy elementName = this.namespace.getElementName(localPart);
    return addElement(elementName, handler);
  }

  public final StaxElementHandler<V> addElement(final String localPart, final String name,
    final StaxElementFunction<?> handler) {
    final XmlElementNameProxy elementName = this.namespace.getElementName(localPart);
    return addElement(elementName, name, handler);
  }

  public final StaxElementHandler<V> addElement(final XmlElementNameProxy element,
    final StaxElementFunction<?> handler) {
    final XmlElementName elementName = element.getElementName();
    final String name = elementName.getLocalPart();
    return addElement(elementName, name, handler);
  }

  public final StaxElementHandler<V> addElement(final XmlElementNameProxy elementName,
    final String name, final StaxElementFunction<? extends Object> handler) {
    return addElement(elementName, name, handler, false);
  }

  private StaxElementHandler<V> addElement(final XmlElementNameProxy element, final String name,
    final StaxElementFunction<? extends Object> handler, final boolean list) {
    final XmlElementName elementName = element.getElementName();
    final StaxProperty property = new StaxProperty(elementName, name, handler, list);
    this.elements.put(elementName, property);
    return this;
  }

  public final StaxElementHandler<V> addElement(final XmlNamespace namespace,
    final String localPart, final StaxElementFunction<?> handler) {
    final XmlElementNameProxy elementName = namespace.getElementName(localPart);
    return addElement(elementName, handler);
  }

  public final StaxElementHandler<V> addElement(final XmlNamespace namespace,
    final String localPart, final String name, final StaxElementFunction<?> handler) {
    final XmlElementNameProxy elementName = namespace.getElementName(localPart);
    return addElement(elementName, name, handler);
  }

  public final StaxElementHandler<V> addElementList(final StaxElementHandler<?> element) {
    final XmlElementNameProxy elementName = element.getElementName();
    final String propertyName = element.propertyName;
    final StaxElementFunction<?> handler = element.reader;
    return addElement(elementName, propertyName, handler, true);
  }

  public final StaxElementHandler<V> addElementList(final StaxElementHandler<?> element,
    final String name) {
    final XmlElementNameProxy elementName = element.getElementName();
    final StaxElementFunction<?> handler = element.reader;
    return addElementList(elementName, name, handler);
  }

  public final StaxElementHandler<V> addElementList(final XmlElementNameProxy elementName,
    final StaxElementFunction<? extends Object> handler) {
    final String name = elementName.getLocalPart();
    return addElementList(elementName, name, handler);
  }

  public final StaxElementHandler<V> addElementList(final XmlElementNameProxy elementName,
    final String name, final StaxElementFunction<? extends Object> handler) {
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
  public XmlElementName getElementName() {
    return this.elementName;
  }

  @Override
  public String getLocalPart() {
    return this.elementName.getLocalPart();
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
        final String key = getLocalPart() + "." + attributeName.getLocalPart();
        if (this.unhandled.add(key)) {
          Debug.println(key);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  public V handleElement(final StaxReader in) {
    return (V)this.reader.handle(in, null);
  }

  public void handleElement(final StaxReader in, final StaxElementCallback callback) {
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
        final String key = getLocalPart() + "." + elementName.getLocalPart();
        if (this.unhandled.add(key)) {
          Debug.println(key);
        }
      }
    }
  }

  public <J extends StaxJsonObject> StaxElementFunction<J> jsonFactory(
    final Supplier<J> constructor) {
    return (in, callback) -> {
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
