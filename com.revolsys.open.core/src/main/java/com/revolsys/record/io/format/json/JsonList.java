package com.revolsys.record.io.format.json;

import java.util.ArrayList;
import java.util.Collection;

public class JsonList extends ArrayList<Object> {

  public JsonList() {
  }

  public JsonList(final Collection<? extends Object> c) {
    super(c);
  }

  public JsonList(final int initialCapacity) {
    super(initialCapacity);
  }

  @Override
  public String toString() {
    return Json.toString(this);
  }
}
