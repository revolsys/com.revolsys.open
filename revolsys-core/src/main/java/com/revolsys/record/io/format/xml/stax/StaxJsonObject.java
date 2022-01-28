package com.revolsys.record.io.format.xml.stax;

import java.util.Set;
import java.util.UUID;

import javax.xml.namespace.QName;

import com.revolsys.record.io.format.json.JsonList;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.Jsonable;
import com.revolsys.record.io.format.xml.XmlAttribute;
import com.revolsys.record.io.format.xml.XmlComplexType;
import com.revolsys.record.io.format.xml.XmlElement;
import com.revolsys.record.io.format.xml.XmlSchema;
import com.revolsys.record.io.format.xml.XmlSimpleType;
import com.revolsys.record.io.format.xml.XmlType;
import com.revolsys.util.Debug;
import com.revolsys.util.Uuid;
import com.revolsys.util.UuidNamespace;

public class StaxJsonObject implements Jsonable {

  private static final UuidNamespace UUID_NAMESPACE = Uuid
    .sha1("65bd15b3-647d-474e-8eb3-e1441dd73bba");

  @SuppressWarnings("unchecked")
  public static <V> V readValue(final StaxReader in, final StaxJsonObjectSchema jsonSchema,
    final XmlElement element) {
    final XmlType type = element.getType();
    if (type instanceof XmlComplexType) {
      final XmlComplexType complexType = (XmlComplexType)type;
      return (V)jsonSchema.newJsonObject(complexType).initFromSchema(in, jsonSchema, complexType);
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

  public static <V> V readValue(final StaxReader in, final XmlSchema schema,
    final XmlElement element) {
    return readValue(in, new StaxJsonObjectSchema(schema), element);
  }

  protected final JsonObject properties = JsonObject.tree();

  private UUID id;

  public StaxJsonObject() {
  }

  public StaxJsonObject(final StaxReader in, final StaxElementHandler<?> elementHandler) {
    super();
    initFromElementHandler(in, elementHandler);
  }

  public StaxJsonObject(final StaxReader in, final XmlComplexType type) {
    super();
  }

  private void addProperty(final String name, final Object value) {
    if (value == null) {
      this.properties.remove(name);
    } else {
      this.properties.addValue(name, value);
    }
  }

  @Override
  public boolean equals(final Object o) {
    if (getClass() == o.getClass()) {
      final StaxJsonObject j = (StaxJsonObject)o;
      return j.id.equals(this.id);
    }
    return false;
  }

  public UUID getId() {
    if (this.id == null) {
      this.id = UUID_NAMESPACE.uuid(toJsonString(false));
    }
    return this.id;
  }

  public <V> V getValue(final String name) {
    return this.properties.getValue(name);
  }

  public <V> V getValue(final String name, final V defaultValue) {
    final V value = this.properties.getValue(name);
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  @Override
  public int hashCode() {
    return getId().hashCode();
  }

  public void initFromElementHandler(final StaxReader in,
    final StaxElementHandler<?> elementHandler) {
    elementHandler.handleElement(in, new StaxElementCallback() {
      @Override
      public void handleAttributeValue(final StaxReader in, final int i,
        final StaxAttributeReader handler, final Object value) {
        final String name = handler.getName();
        addProperty(name, value);
      }

      @Override
      public void handleElement(final StaxReader in, final StaxProperty handler,
        final StaxElementCallback callback) {
        final String name = handler.getName();
        final Object value = handler.handleElementValue(in, callback);
        if (value != null) {
          if (handler.isList()) {
            JsonList list = StaxJsonObject.this.properties.getValue(name);
            if (list == null) {
              list = JsonList.array();
              addProperty(name, list);
            }
            list.add(value);
          } else {
            addProperty(name, value);
          }
        }
      }
    });
  }

  public StaxJsonObject initFromSchema(final StaxReader in, final StaxJsonObjectSchema jsonSchema,
    final XmlComplexType type) {
    for (int i = 0; i < in.getAttributeCount(); i++) {
      final String text = in.getAttributeValue(i);
      if (text != null) {
        final QName xmlName = in.getAttributeName(i);
        final XmlAttribute attribute = type.getAttribute(xmlName);
        if (attribute != null) {
          final Object value = attribute.getType().toValue(text);
          if (value != null) {
            addProperty(xmlName.getLocalPart(), value);
          }
        } else {
          final String key = type + "." + xmlName.getLocalPart();
          // if (this.unhandled.add(key)) {
          Debug.println(key);
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
        final Object childValue = readValue(in, jsonSchema, childElement);
        if (childElement.isList()) {
          JsonList list = getValue(childName);
          if (list == null) {
            list = JsonList.array();
            addProperty(childName, list);
          }
          list.add(childValue);
        } else {
          addProperty(childName, childValue);
        }
      } else {
        final String key = type.getLocalPart() + "." + xmlName.getLocalPart();
        Debug.println(key);
        in.skipSubTree();
      }
    }
    return this;
  }

  public boolean isTrue(final String name) {
    final Object value = getValue(name);
    if (value != null) {
      return value == Boolean.TRUE;
    }
    return false;
  }

  public Set<String> keySet() {
    return this.properties.keySet();
  }

  @Override
  public JsonObject toJson() {
    return JsonObject.hash()
      // .addValue("$id", getId())
      .addValues(this.properties);
  }

  @Override
  public String toString() {
    return toJsonString(true).toString();
  }

}
