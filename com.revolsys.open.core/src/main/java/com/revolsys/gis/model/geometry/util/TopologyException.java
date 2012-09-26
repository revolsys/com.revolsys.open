package com.revolsys.gis.model.geometry.util;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;

/**
 * Indicates an invalid or inconsistent topological situation encountered during
 * processing
 * 
 * @version 1.7
 */
public class TopologyException extends RuntimeException {
  private static String msgWithCoord(String msg, Coordinates pt) {
    if (pt != null)
      return msg + " [ " + pt + " ]";
    return msg;
  }

  private Coordinates pt = null;

  public TopologyException(String msg) {
    super(msg);
  }

  public TopologyException(String msg, Coordinates pt) {
    super(msgWithCoord(msg, pt));
    this.pt = new DoubleCoordinates(pt);
  }

  public Coordinates getCoordinate() {
    return pt;
  }

}
