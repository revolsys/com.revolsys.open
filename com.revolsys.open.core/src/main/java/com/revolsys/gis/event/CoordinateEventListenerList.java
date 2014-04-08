package com.revolsys.gis.event;

import java.util.LinkedHashSet;

import com.revolsys.jts.geom.Coordinate;

public class CoordinateEventListenerList extends
  LinkedHashSet<CoordinateEventListener> implements CoordinateEventListener {

  /**
   * 
   */
  private static final long serialVersionUID = 3504994646284361341L;

  public void coordinateEvent(final Coordinate coordinate,
    final String typePath, final String ruleName, final String action,
    final String notes) {
    coordinateEvent(new CoordinateEvent(coordinate, typePath, ruleName, action,
      notes));
  }

  @Override
  public void coordinateEvent(final CoordinateEvent coordinateEvent) {
    for (final CoordinateEventListener listener : this) {
      listener.coordinateEvent(coordinateEvent);
    }
  }
}
