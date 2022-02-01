package com.revolsys.record.io.format.xml.stax;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import com.revolsys.record.io.format.json.JsonList;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.Jsonable;
import com.revolsys.record.io.format.xml.XmlComplexType;
import com.revolsys.record.io.format.xml.XmlElement;
import com.revolsys.record.io.format.xml.XmlSchema;
import com.revolsys.util.Uuid;
import com.revolsys.util.UuidBuilder;
import com.revolsys.util.UuidNamespace;

public class StaxJsonObject implements Jsonable {

  private static final UuidNamespace UUID_NAMESPACE = Uuid
    .sha1("65bd15b3-647d-474e-8eb3-e1441dd73bba");

  public static <V> V readValue(final StaxReader in, final XmlSchema schema,
    final XmlElement element) {
    return new StaxJsonObjectSchema(schema).readElement(in, element);
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

  public void addValue(final String name, final Object value) {
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

  public UUID getId(final Collection<String> ignoreFieldNames) {
    boolean hadValue = false;
    final UuidBuilder builder = UUID_NAMESPACE.builder();
    builder.append(getClass().getName());
    final JsonObject properties = this.properties;
    for (final String name : properties.keySet()) {
      if (!ignoreFieldNames.contains(name)) {
        hadValue = true;
        final Object value = properties.getValue(name);
        builder.append(name);
        builder.append(value);
      }
    }
    if (hadValue) {
      return builder.build();
    } else {
      return null;
    }
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

  public boolean hasValue(final String childName) {
    return this.properties.hasValue(childName);
  }

  public void initFromElementHandler(final StaxReader in,
    final StaxElementHandler<?> elementHandler) {
    elementHandler.handleElement(in, new StaxElementCallback() {
      @Override
      public void handleAttributeValue(final StaxReader in, final int i,
        final StaxAttributeReader handler, final Object value) {
        final String name = handler.getName();
        addValue(name, value);
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
              addValue(name, list);
            }
            list.add(value);
          } else {
            addValue(name, value);
          }
        }
      }
    });
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
