package com.revolsys.gis.graph;

import java.util.Map;

public interface AttributedObject {

  <A> A getAttribute(final String name);

  Map<String, Object> getAttributes();

  void setAttribute(final String name, final Object value);

}
