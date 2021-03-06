package com.revolsys.geometry.graph.visitor;

import java.util.function.Consumer;

import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.graph.event.EdgeEventListener;
import com.revolsys.geometry.graph.event.EdgeEventListenerList;
import com.revolsys.geometry.graph.event.NodeEventListener;
import com.revolsys.geometry.graph.event.NodeEventListenerList;

public abstract class AbstractEdgeListenerVisitor<T> implements Consumer<Edge<T>> {

  private final EdgeEventListenerList<T> edgeListeners = new EdgeEventListenerList<>();

  private final NodeEventListenerList<T> nodeListeners = new NodeEventListenerList<>();

  public void addEdgeListener(final EdgeEventListener<T> listener) {
    this.edgeListeners.add(listener);
  }

  public void addNodeListener(final NodeEventListener<T> listener) {
    this.nodeListeners.add(listener);
  }

  public void edgeEvent(final Edge<T> edge, final String ruleName, final String action,
    final String notes) {
    this.edgeListeners.edgeEvent(edge, ruleName, action, notes);
  }

  public EdgeEventListenerList<T> getEdgeListeners() {
    return this.edgeListeners;
  }

  public NodeEventListenerList<T> getNodeListeners() {
    return this.nodeListeners;
  }

  public void nodeEvent(final Node<T> node, final String typePath, final String ruleName,
    final String action, final String notes) {
    this.nodeListeners.nodeEvent(node, typePath, ruleName, action, notes);
  }
}
