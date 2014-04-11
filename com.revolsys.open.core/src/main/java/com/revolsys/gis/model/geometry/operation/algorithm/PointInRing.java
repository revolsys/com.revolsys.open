package com.revolsys.gis.model.geometry.operation.algorithm;

import com.revolsys.gis.model.geometry.algorithm.locate.PointOnGeometryLocator;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;

/**
 * An interface for classes which test whether a {@link Coordinate} lies inside
 * a ring.
 *
 * @version 1.7
 * 
 * @see PointOnGeometryLocator
 */
public interface PointInRing {

  boolean isInside(Coordinates pt);
}
