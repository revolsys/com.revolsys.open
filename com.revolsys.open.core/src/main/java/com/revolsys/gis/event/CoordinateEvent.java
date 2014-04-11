package com.revolsys.gis.event;

import java.util.EventObject;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;

public class CoordinateEvent extends EventObject {
  /**
   * 
   */
  private static final long serialVersionUID = -1809350055079477785L;

  public static final String NODE_ADDED = "Coordinate added";

  public static final String NODE_CHANGED = "Coordinate changed";

  public static final String NODE_REMOVED = "Coordinate removed";

  private String action;

  private Coordinates coordinate;

  private String notes;

  private String ruleName;

  private String typePath;

  public CoordinateEvent(final Coordinate coordinate) {
    super(coordinate);
  }

  public CoordinateEvent(final Coordinate coordinate, final String ruleName,
    final String action) {
    super(coordinate);
    this.ruleName = ruleName;
    this.action = action;
  }

  public CoordinateEvent(final Coordinate coordinate, final String path,
    final String ruleName, final String action, final String notes) {
    super(coordinate);
    this.typePath = typePath;
    this.ruleName = ruleName;
    this.action = action;
    this.notes = notes;
  }

  public String getAction() {
    return action;
  }

  public Coordinates getCoordinate() {
    return (Coordinates)getSource();
  }

  public String getNotes() {
    return notes;
  }

  public String getRuleName() {
    return ruleName;
  }

  public String getTypeName() {
    return typePath;
  }

}
