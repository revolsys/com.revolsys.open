package com.revolsys.record.io.format.xml.stax;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.xml.namespace.QName;

import com.revolsys.record.io.format.xml.XmlComplexType;
import com.revolsys.record.io.format.xml.XmlElement;
import com.revolsys.record.io.format.xml.XmlName;
import com.revolsys.record.io.format.xml.XmlNameProxy;
import com.revolsys.record.io.format.xml.XmlSchema;
import com.revolsys.record.io.format.xml.XmlSimpleType;
import com.revolsys.record.io.format.xml.XmlType;
import com.revolsys.util.Debug;

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

  public XmlSchema getXmlSchema() {
    return this.xmlSchema;
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

  public <V> V readValue(final StaxReader in, final XmlElement element) {
    final XmlType type = element.getType();
    return readValue(in, type);
  }

  @SuppressWarnings("unchecked")
  public <V> V readValue(final StaxReader in, final XmlType type) {
    if (type instanceof XmlComplexType) {
      final XmlComplexType complexType = (XmlComplexType)type;
      return (V)newJsonObject(complexType).initFromSchema(in, this, complexType);
    } else if (type instanceof XmlSimpleType) {
      final XmlSimpleType simpleType = (XmlSimpleType)type;
      final String text = in.getElementText();
      if (text == null) {
        return null;
      } else {
        return simpleType.toValue(text);
      }
    } else if (type instanceof StaxToObjectType) {
      final StaxToObjectType<?> simpleType = (StaxToObjectType<?>)type;
      return (V)simpleType.read(in);
    } else {
      in.skipSubTree();
      Debug.noOp();
      return null;
    }
  }
}
