package com.revolsys.record.io.format.xml.stax;

import java.util.Set;
import java.util.UUID;

import com.revolsys.record.io.format.json.JsonList;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.Jsonable;
import com.revolsys.util.Uuid;
import com.revolsys.util.UuidNamespace;

public class StaxJsonObject implements Jsonable {

  private static final UuidNamespace UUID_NAMESPACE = Uuid
    .sha1("65bd15b3-647d-474e-8eb3-e1441dd73bba");

  protected final JsonObject properties = JsonObject.tree();

  private UUID id;

  public StaxJsonObject() {
  }

  public StaxJsonObject(final StaxReader in, final StaxElementHandler<?> elementHandler) {
    super();
    initFromElementHandler(in, elementHandler);
  }

  private void addProperty(final String name, final Object value) {
    this.properties.addValue(name, value);
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

  public boolean isTrue(final String name) {
    return getValue(name) == Boolean.TRUE;
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

}
