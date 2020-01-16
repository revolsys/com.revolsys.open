package com.revolsys.record.io.format.json;

import java.util.Map;

import com.revolsys.collection.map.LinkedHashMapEx;

public class JsonObject extends LinkedHashMapEx {

  public static final JsonObject EMPTY = new JsonObject() {
    @Override
    public void clear() {
      throw new UnsupportedOperationException("Read only");
    }

    @Override
    public Object put(final String key, final Object value) {
      throw new UnsupportedOperationException("Read only");
    }

    @Override
    public void putAll(final Map<? extends String, ? extends Object> m) {
      throw new UnsupportedOperationException("Read only");
    }

    @Override
    public boolean remove(final Object key, final Object value) {
      throw new UnsupportedOperationException("Read only");
    }

    @Override
    public boolean replace(final String key, final Object oldValue, final Object newValue) {
      throw new UnsupportedOperationException("Read only");
    }
  };

  public JsonObject() {
    super();
  }

  public JsonObject(final Map<? extends String, ? extends Object> m) {
    super(m);
  }

  public JsonObject(final String key, final Object value) {
    add(key, value);
  }

  @Override
  public JsonObject clone() {
    return new JsonObject(this);
  }

  @Override
  public String toString() {
    return Json.toString(this);
  }
}
