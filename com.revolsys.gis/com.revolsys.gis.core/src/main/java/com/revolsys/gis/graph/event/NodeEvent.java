package com.revolsys.gis.graph.event;

import java.util.EventObject;

import javax.xml.namespace.QName;

import com.revolsys.gis.graph.Node;
import com.revolsys.gis.model.coordinates.Coordinates;

public class NodeEvent<T> extends EventObject {
  public static final String NODE_ADDED = "Added";

  public static final String NODE_CHANGED = "Changed";

  public static final String NODE_REMOVED = "Removed";

  private String action;

  private final Coordinates coordinates;

  private String notes;

  private String ruleName;

  private QName typeName;

  public NodeEvent(
    final Node<T> node) {
    super(node);
    this.coordinates = node.getCoordinates();
  }

  public NodeEvent(
    final Node<T> node,
    final QName typeName,
    final String ruleName,
    final String action,
    final String notes) {
    super(node);
    this.coordinates = node.getCoordinates();
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
    this.coordinates = node.getCoordinates();
    this.ruleName = ruleName;
    this.action = action;
  }

  public String getAction() {
    return action;
  }

  public Coordinates getCoordinates() {
    return coordinates;
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
