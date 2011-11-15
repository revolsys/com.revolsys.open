package com.revolsys.gis.event;

import java.util.EventObject;

import javax.xml.namespace.QName;

import com.vividsolutions.jts.geom.Coordinate;

public class CoordinateEvent extends EventObject {
  public static final String NODE_ADDED = "Coordinate added";

  public static final String NODE_CHANGED = "Coordinate changed";

  public static final String NODE_REMOVED = "Coordinate removed";

  private String action;

  private Coordinate coordinate;

  private String notes;

  private String ruleName;

  private QName typeName;

  public CoordinateEvent(
    final Coordinate coordinate) {
    super(coordinate);
  }

  public CoordinateEvent(
    final Coordinate coordinate,
    final QName typeName,
    final String ruleName,
    final String action,
    final String notes) {
    super(coordinate);
    this.typeName = typeName;
    this.ruleName = ruleName;
    this.action = action;
    this.notes = notes;
  }

  public CoordinateEvent(
    final Coordinate coordinate,
    final String ruleName,
    final String action) {
    super(coordinate);
    this.ruleName = ruleName;
    this.action = action;
  }

  public String getAction() {
    return action;
  }

  public Coordinate getCoordinate() {
    return (Coordinate)getSource();
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
