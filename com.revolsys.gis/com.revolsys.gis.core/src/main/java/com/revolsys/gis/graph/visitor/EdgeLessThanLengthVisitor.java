package com.revolsys.gis.graph.visitor;

import com.revolsys.gis.data.visitor.Visitor;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.event.EdgeEventListenerList;

public class EdgeLessThanLengthVisitor<T> implements Visitor<Edge<T>> {

  private final EdgeEventListenerList<T> listeners = new EdgeEventListenerList<T>();

  private final double minLength;

  private Visitor<Edge<T>> visitor;

  public EdgeLessThanLengthVisitor(
    final double minLength) {
    this.minLength = minLength;
  }

  public EdgeLessThanLengthVisitor(
    final double minLength,
    final Visitor<Edge<T>> visitor) {
    this.minLength = minLength;
    this.visitor = visitor;
  }

  public EdgeEventListenerList<T> getListeners() {
    return listeners;
  }

  public boolean visit(
    final Edge<T> edge) {
    final double length = edge.getLength();
    if (length < minLength) {
      listeners.edgeEvent(edge, "Edge less than length", "Review", length
        + " < " + minLength);
      if (visitor != null) {
        visitor.visit(edge);
      }
    }
    return true;
  }
}
