package com.revolsys.record.io.format.json;

import java.util.LinkedHashMap;
import java.util.Map;

public class JsonObjectHash extends LinkedHashMap<String, Object> implements JsonObject {

  private static final long serialVersionUID = 1L;

  public JsonObjectHash() {
    super();
  }

  public JsonObjectHash(final Map<? extends String, ? extends Object> m) {
    super(m);
  }

  public JsonObjectHash(final String key, final Object value) {
    add(key, value);
  }

  @Override
  public JsonObjectHash clone() {
    return (JsonObjectHash)new JsonObjectHash()//
      .addValuesClone(this);
  }

  @Override
  public String toString() {
    return Json.toString(this, false);
  }
}
