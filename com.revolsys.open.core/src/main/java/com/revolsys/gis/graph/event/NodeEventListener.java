package com.revolsys.gis.graph.event;

import java.util.EventListener;

public interface NodeEventListener<T> extends EventListener {
  public void nodeEvent(
    NodeEvent<T> nodeEvent);
}
