package com.revolsys.gis.algorithm;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Geometry;

public class MaxPointDistanceFilter implements CoordinateFilter {
  public static double getMaxDistance(final Geometry srcGeom,
    final Geometry matchGeometry) {
    final MaxPointDistanceFilter filter = new MaxPointDistanceFilter(srcGeom);
    matchGeometry.apply(filter);
    return filter.getMaxPointDistance().getDistance();
  }

  private final EuclideanDistanceToPoint euclideanDist = new EuclideanDistanceToPoint();

  private final Geometry geom;

  private final PointPairDistance maxPtDist = new PointPairDistance();

  private final PointPairDistance minPtDist = new PointPairDistance();

  public MaxPointDistanceFilter(final Geometry geom) {
    this.geom = geom;
  }

  @Override
  public void filter(final Coordinate pt) {
    minPtDist.initialize();
    euclideanDist.computeDistance(geom, pt, minPtDist);
    maxPtDist.setMaximum(minPtDist);
  }

  public PointPairDistance getMaxPointDistance() {
    return maxPtDist;
  }
}
