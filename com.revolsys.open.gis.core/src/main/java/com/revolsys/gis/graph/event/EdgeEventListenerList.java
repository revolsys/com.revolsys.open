package com.revolsys.gis.graph.event;

import java.util.LinkedHashSet;

import com.revolsys.gis.graph.Edge;

public class EdgeEventListenerList<T> extends LinkedHashSet<EdgeEventListener>
  implements EdgeEventListener<T> {

  public void edgeEvent(
    final Edge<T> edge,
    final String ruleName,
    final String action,
    final String notes) {
    if (!isEmpty()) {
      final EdgeEvent<T> edgeEvent = new EdgeEvent<T>(edge, ruleName, action,
        notes);
      edgeEvent(edgeEvent);
    }
  }

  public void edgeEvent(
    final EdgeEvent<T> edgeEvent) {
    for (final EdgeEventListener<T> listener : this) {
      listener.edgeEvent(edgeEvent);
    }
  }

}
