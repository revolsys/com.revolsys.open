package com.revolsys.gis.graph.event;

import java.util.EventObject;

import javax.xml.namespace.QName;

import com.revolsys.gis.graph.Node;
import com.vividsolutions.jts.geom.Coordinate;

public class NodeEvent<T> extends EventObject {
  public static final String NODE_ADDED = "Added";

  public static final String NODE_CHANGED = "Changed";

  public static final String NODE_REMOVED = "Removed";

  private String action;

  private final Coordinate coordinate;

  private String notes;

  private String ruleName;

  private QName typeName;

  public NodeEvent(
    final Node<T> node) {
    super(node);
    this.coordinate = node.getCoordinate();
  }

  public NodeEvent(
    final Node<T> node,
    final QName typeName,
    final String ruleName,
    final String action,
    final String notes) {
    super(node);
    this.coordinate = node.getCoordinate();
    this.typeName = typeName;
    this.ruleName = ruleName;
    this.action = action;
    this.notes = notes;
  }

  public NodeEvent(
    final Node<T> node,
    final String ruleName,
    final String action) {
    super(node);
    this.coordinate = node.getCoordinate();
    this.ruleName = ruleName;
    this.action = action;
  }

  public String getAction() {
    return action;
  }

  public Coordinate getCoordinate() {
    return coordinate;
  }

  public Node<T> getNode() {
    return (Node<T>)getSource();
  }

  public String getNotes() {
    return notes;
  }

  public String getRuleName() {
    return ruleName;
  }

  public QName getTypeName() {
    return typeName;
  }

}
