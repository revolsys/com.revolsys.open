package com.revolsys.gis.model.geometry.util;

import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.jts.geom.Coordinates;

/**
 * Indicates an invalid or inconsistent topological situation encountered during
 * processing
 * 
 * @version 1.7
 */
public class TopologyException extends RuntimeException {
  private static String msgWithCoord(final String msg, final Coordinates pt) {
    if (pt != null) {
      return msg + " [ " + pt + " ]";
    }
    return msg;
  }

  private Coordinates pt = null;

  public TopologyException(final String msg) {
    super(msg);
  }

  public TopologyException(final String msg, final Coordinates pt) {
    super(msgWithCoord(msg, pt));
    this.pt = new DoubleCoordinates(pt);
  }

  public Coordinates getCoordinate() {
    return pt;
  }

}
