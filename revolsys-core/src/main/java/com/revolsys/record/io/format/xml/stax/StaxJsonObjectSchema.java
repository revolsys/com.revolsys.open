package com.revolsys.record.io.format.xml.stax;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.xml.namespace.QName;

import com.revolsys.record.io.format.json.JsonList;
import com.revolsys.record.io.format.xml.XmlAttribute;
import com.revolsys.record.io.format.xml.XmlComplexType;
import com.revolsys.record.io.format.xml.XmlElement;
import com.revolsys.record.io.format.xml.XmlName;
import com.revolsys.record.io.format.xml.XmlNameProxy;
import com.revolsys.record.io.format.xml.XmlSchema;
import com.revolsys.record.io.format.xml.XmlSimpleType;
import com.revolsys.record.io.format.xml.XmlType;

public class StaxJsonObjectSchema {

  private final XmlSchema xmlSchema;

  private final Map<QName, Supplier<? extends StaxJsonObject>> constructorByName = new HashMap<>();

  public StaxJsonObjectSchema(final XmlSchema xmlSchema) {
    this.xmlSchema = xmlSchema;
  }

  public StaxJsonObjectSchema addJsonConstructor(final XmlNameProxy typeName,
    final Supplier<? extends StaxJsonObject> constructor) {
    this.constructorByName.put(typeName.getXmlName(), constructor);
    return this;
  }

  public XmlElement getElement(final QName qName) {
    return this.xmlSchema.getElement(qName);
  }

  public XmlType getType(final QName qName) {
    return this.xmlSchema.getType(qName);
  }

  public XmlSchema getXmlSchema() {
    return this.xmlSchema;
  }

  public StaxJsonObject newJsonObject(final QName xmlName) {
    final Supplier<? extends StaxJsonObject> constructor = this.constructorByName.get(xmlName);
    if (constructor == null) {
      return new StaxJsonObject();
    } else {
      return constructor.get();
    }
  }

  public StaxJsonObject newJsonObject(final XmlComplexType type) {
    final XmlName xmlName = type.getXmlName();
    final Supplier<? extends StaxJsonObject> constructor = this.constructorByName.get(xmlName);
    if (constructor == null) {
      return new StaxJsonObject();
    } else {
      return constructor.get();
    }
  }

  @SuppressWarnings("unchecked")
  public <V> V readComplexType(final StaxReader in, final XmlComplexType type) {
    final StaxJsonObject object = newJsonObject(type);
    for (int i = 0; i < in.getAttributeCount(); i++) {
      final String text = in.getAttributeValue(i);
      if (text != null) {
        final QName xmlName = in.getAttributeName(i);
        final XmlAttribute attribute = type.getAttribute(xmlName);
        if (attribute != null) {
          final Object value = attribute.getType().toValue(text);
          if (value != null) {
            object.addValue(xmlName.getLocalPart(), value);
          }
        } else {
          final String key = type + "." + xmlName.getLocalPart();
          // if (this.unhandled.add(key)) {
          // Debug.println(key);
          // }
        }
      }
    }
    final int depth = in.getDepth();
    while (in.skipToStartElement(depth)) {
      final QName xmlName = in.getName();
      final String childName = xmlName.getLocalPart();
      final XmlElement childElement = type.getElement(xmlName);
      if (childElement != null) {
        final Object childValue = readElement(in, childElement);
        if (childElement.isList()) {
          JsonList list = object.getValue(childName);
          if (list == null) {
            list = JsonList.array();
            object.addValue(childName, list);
          }
          list.add(childValue);
        } else {
          object.addValue(childName, childValue);
        }
      } else {
        final String key = type.getLocalPart() + "." + xmlName.getLocalPart();
        // Debug.println(key);
        in.skipSubTree();
      }
    }
    return (V)object;
  }

  public <V> V readElement(final StaxReader in, final XmlElement element) {
    final XmlType type = element.getType();
    return readType(in, type);
  }

  public <V> V readSimpleType(final StaxReader in, final XmlSimpleType simpleType) {
    final String text = in.getElementText();
    if (text == null) {
      return null;
    } else {
      return simpleType.toValue(text);
    }
  }

  @SuppressWarnings("unchecked")
  public <V> V readType(final StaxReader in, final XmlType type) {
    if (type instanceof XmlComplexType) {
      return readComplexType(in, (XmlComplexType)type);
    } else if (type instanceof XmlSimpleType) {
      return readSimpleType(in, (XmlSimpleType)type);
    } else if (type instanceof StaxToObjectType) {
      final StaxToObjectType<?> simpleType = (StaxToObjectType<?>)type;
      return (V)simpleType.read(in);
    } else {
      return readValue(in);
    }
  }

  @SuppressWarnings("unchecked")
  private <V> V readUnkownType(final StaxReader in, final QName name) {
    final StaxJsonObject object = newJsonObject(name);
    for (int i = 0; i < in.getAttributeCount(); i++) {
      final String text = in.getAttributeValue(i);
      if (text != null) {
        final QName xmlName = in.getAttributeName(i);
        object.addValue(xmlName.getLocalPart(), text);
      }
    }
    final int depth = in.getDepth();
    while (in.skipToStartElement(depth)) {
      final QName xmlName = in.getName();
      final String childName = xmlName.getLocalPart();
      final Object childValue = readValue(in);
      if (object.hasValue(childName)) {
        final Object oldValue = object.getValue(childName);
        if (oldValue instanceof List<?>) {
          final List<Object> list = (List<Object>)oldValue;
          list.add(childValue);
        } else {
          final JsonList list = JsonList.array(oldValue, childValue);
          object.addValue(childName, list);
        }
      } else {
        object.addValue(childName, childValue);
      }
    }
    return (V)object;
  }

  public <V> V readValue(final StaxReader in) {
    final QName name = in.getName();
    final XmlElement element = getElement(name);
    if (element != null) {
      return readElement(in, element);
    }

    final XmlType type = getType(name);
    if (type != null) {
      return readType(in, type);
    }

    return readUnkownType(in, name);
  }

}
