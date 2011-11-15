package com.revolsys.gis.graph.event;

import java.util.LinkedHashSet;

import javax.xml.namespace.QName;

import com.revolsys.gis.graph.Node;

public class NodeEventListenerList<T> extends LinkedHashSet<NodeEventListener>
  implements NodeEventListener<T> {

  /**
   * 
   */
  private static final long serialVersionUID = 491848000001273343L;

  public void nodeEvent(
    final Node<T> node,
    final QName typeName,
    final String ruleName,
    final String action,
    final String notes) {
    if (!isEmpty()) {
      nodeEvent(new NodeEvent<T>(node, typeName, ruleName, action, notes));
    }
  }

  public void nodeEvent(
    final NodeEvent<T> nodeEvent) {
    for (final NodeEventListener<T> listener : this) {
      listener.nodeEvent(nodeEvent);
    }
  }
}
