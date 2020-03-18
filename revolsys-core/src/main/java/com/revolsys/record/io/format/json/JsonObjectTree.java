package com.revolsys.record.io.format.json;

import java.util.Map;

import com.revolsys.collection.map.TreeMapEx;

public class JsonObjectTree extends TreeMapEx implements JsonObject {

   JsonObjectTree() {
    super();
  }

   JsonObjectTree(final Map<? extends String, ? extends Object> m) {
    super(m);
  }

   JsonObjectTree(final String key, final Object value) {
    add(key, value);
  }

  @Override
  public JsonObjectTree clone() {
    return new JsonObjectTree(this);
  }

  @Override
  public String toString() {
    return Json.toString(this, false);
  }
}
