package com.revolsys.gis.graph.visitor;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.visitor.AbstractVisitor;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.graph.event.EdgeEventListener;
import com.revolsys.gis.graph.event.EdgeEventListenerList;
import com.revolsys.gis.graph.event.NodeEventListener;
import com.revolsys.gis.graph.event.NodeEventListenerList;

public abstract class AbstractEdgeListenerVisitor<T> extends
  AbstractVisitor<Edge<T>> {

  private final EdgeEventListenerList<T> edgeListeners = new EdgeEventListenerList<T>();

  private final NodeEventListenerList<T> nodeListeners = new NodeEventListenerList<T>();

  public void addEdgeListener(final EdgeEventListener<T> listener) {
    edgeListeners.add(listener);
  }

  public void addNodeListener(final NodeEventListener<T> listener) {
    nodeListeners.add(listener);
  }

  public void edgeEvent(final Edge<T> edge, final String ruleName,
    final String action, final String notes) {
    edgeListeners.edgeEvent(edge, ruleName, action, notes);
  }

  public EdgeEventListenerList<T> getEdgeListeners() {
    return edgeListeners;
  }

  public NodeEventListenerList<T> getNodeListeners() {
    return nodeListeners;
  }

  public void nodeEvent(final Node<T> node, final QName typeName,
    final String ruleName, final String action, final String notes) {
    nodeListeners.nodeEvent(node, typeName, ruleName, action, notes);
  }
}
