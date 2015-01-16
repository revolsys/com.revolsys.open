package com.revolsys.gis.graph.visitor;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.graph.event.EdgeEventListener;
import com.revolsys.gis.graph.event.EdgeEventListenerList;
import com.revolsys.gis.graph.event.NodeEventListener;
import com.revolsys.gis.graph.event.NodeEventListenerList;

public abstract class AbstractNodeListenerVisitor<T> implements
Visitor<Node<T>> {

  private final EdgeEventListenerList<T> edgeListeners = new EdgeEventListenerList<T>();

  private final NodeEventListenerList<T> nodeListeners = new NodeEventListenerList<T>();

  public void addEdgeListener(final EdgeEventListener<T> listener) {
    this.edgeListeners.add(listener);
  }

  public void addNodeListener(final NodeEventListener<T> listener) {
    this.nodeListeners.add(listener);
  }

  public void edgeEvent(final Edge<T> edge, final String path,
    final String ruleName, final String action, final String notes) {
    this.edgeListeners.edgeEvent(edge, ruleName, action, notes);
  }

  public EdgeEventListenerList<T> getEdgeListeners() {
    return this.edgeListeners;
  }

  public NodeEventListenerList<T> getNodeListeners() {
    return this.nodeListeners;
  }

  public void nodeEvent(final Node<T> node, final String typePath,
    final String ruleName, final String action, final String notes) {
    this.nodeListeners.nodeEvent(node, typePath, ruleName, action, notes);
  }
}
