package com.revolsys.gis.event;

import java.util.LinkedHashSet;

import javax.xml.namespace.QName;

import com.vividsolutions.jts.geom.Coordinate;

public class CoordinateEventListenerList extends
  LinkedHashSet<CoordinateEventListener> implements CoordinateEventListener {

  /**
   * 
   */
  private static final long serialVersionUID = 3504994646284361341L;

  public void coordinateEvent(
    final Coordinate coordinate,
    final QName typeName,
    final String ruleName,
    final String action,
    final String notes) {
    coordinateEvent(new CoordinateEvent(coordinate, typeName, ruleName, action,
      notes));
  }

  public void coordinateEvent(final CoordinateEvent coordinateEvent) {
    for (final CoordinateEventListener listener : this) {
      listener.coordinateEvent(coordinateEvent);
    }
  }
}
