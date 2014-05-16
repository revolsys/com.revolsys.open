package com.revolsys.gis.algorithm.index.quadtree;

import java.util.List;

import com.revolsys.gis.model.coordinates.filter.LineSegmentCoordinateDistanceFilter;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.segment.Segment;

public class GeometrySegmentQuadTree extends IdObjectQuadTree<Segment> {

  private final Geometry geometry;

  public GeometrySegmentQuadTree(final Geometry geometry) {
    this.geometry = geometry;
    if (geometry != null) {
      setGeometryFactory(geometry.getGeometryFactory());
      for (final Segment segment : geometry.segments()) {
        final BoundingBox boundingBox = segment.getBoundingBox();
        insert(boundingBox, segment);
      }
    }
  }

  @Override
  protected double[] getBounds(final Object id) {
    final Segment segment = getItem(id);
    final BoundingBox boundingBox = segment.getBoundingBox();
    return boundingBox.getBounds(2);
  }

  @Override
  protected Object getId(final Segment segment) {
    return segment.getSegmentId();
  }

  @Override
  protected Segment getItem(final Object id) {
    final int[] segmentId = (int[])id;
    return geometry.getSegment(segmentId);
  }

  public List<Segment> getWithinDistance(final Point point,
    final double maxDistance) {
    BoundingBox boundingBox = point.getBoundingBox();
    boundingBox = boundingBox.expand(maxDistance);
    final LineSegmentCoordinateDistanceFilter filter = new LineSegmentCoordinateDistanceFilter(
      point, maxDistance);
    return query(boundingBox, filter);
  }

}
