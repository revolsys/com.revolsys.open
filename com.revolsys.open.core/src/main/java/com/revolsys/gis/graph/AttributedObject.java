package com.revolsys.gis.graph;

import java.util.Map;

public interface AttributedObject {

  <A> A getField(final String name);

  Map<String, Object> getFields();

  void setAttribute(final String name, final Object value);

}
