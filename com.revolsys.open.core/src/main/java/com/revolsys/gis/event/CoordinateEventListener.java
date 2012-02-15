package com.revolsys.gis.event;

import java.util.EventListener;

public interface CoordinateEventListener extends EventListener {
  public void coordinateEvent(CoordinateEvent coordinateEvent);
}
