package com.revolsys.gis.graph.event;

import java.util.EventObject;

import javax.xml.namespace.QName;

import com.revolsys.gis.graph.Edge;
import com.vividsolutions.jts.geom.LineString;

public class EdgeEvent<T> extends EventObject {
  /**
   * 
   */
  private static final long serialVersionUID = -2271176357444777709L;

  public static final String EDGE_ADDED = "Added";

  public static final String EDGE_CHANGED = "Changed";

  public static final String EDGE_REMOVED = "Removed";

  private String action;

  private final LineString line;

  private String notes;

  private String ruleName;

  private final QName typeName;

  @Override
  public Edge<T> getSource() {
    return (Edge<T>)super.getSource();
  }

  public EdgeEvent(final Edge<T> edge) {
    super(edge);
    this.line = edge.getLine();
    this.typeName = edge.getTypeName();
  }

  public EdgeEvent(final Edge<T> edge, final String ruleName,
    final String action) {
    super(edge);
    this.line = edge.getLine();
    this.typeName = edge.getTypeName();
    this.ruleName = ruleName;
    this.action = action;
  }

  public EdgeEvent(final Edge<T> edge, final String ruleName,
    final String action, final String notes) {
    super(edge);
    this.line = edge.getLine();
    this.typeName = edge.getTypeName();
    this.ruleName = ruleName;
    this.action = action;
    this.notes = notes;
  }

  public String getAction() {
    return action;
  }

  public Edge<T> getEdge() {
    return (Edge<T>)getSource();
  }

  public LineString getLine() {
    return line;
  }

  public T getObject() {
    return getEdge().getObject();
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
