package com.revolsys.record.io.format.json;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.revolsys.collection.map.MapEx;

public interface JsonObject extends MapEx, JsonType {

  public static final JsonObject EMPTY = new JsonObject() {
    @Override
    public JsonObject clone() {
      return this;
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
      return MapEx.EMPTY.entrySet();
    }

    @Override
    public boolean equals(final Object object) {
      if (object instanceof Map<?, ?>) {
        final Map<?, ?> map = (Map<?, ?>)object;
        return map.isEmpty();
      } else {
        return false;
      }
    }

    @Override
    public boolean equals(final Object object,
      final Collection<? extends CharSequence> excludeFieldNames) {
      return equals(object);
    }
  };

  public static JsonObject newItems(final List<?> items) {
    return new JsonObjectHash("items", items);
  }

  @Override
  default JsonObject add(final String key, final Object value) {
    MapEx.super.add(key, value);
    return this;
  }

}
