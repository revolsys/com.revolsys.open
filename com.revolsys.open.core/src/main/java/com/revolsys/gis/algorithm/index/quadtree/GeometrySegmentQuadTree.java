package com.revolsys.gis.algorithm.index.quadtree;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.List;

import com.revolsys.data.equals.GeometryEqualsExact3d;
import com.revolsys.gis.jts.GeometryProperties;
import com.revolsys.gis.model.coordinates.filter.LineSegmentCoordinateDistanceFilter;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.segment.Segment;

public class GeometrySegmentQuadTree extends IdObjectQuadTree<Segment> {

  private static final String GEOMETRY_SEGMENT_INDEX = "GeometrySegmentQuadTree";

  private static final long serialVersionUID = 1L;

  static {
    GeometryEqualsExact3d.addExclude(GEOMETRY_SEGMENT_INDEX);
  }

  public static GeometrySegmentQuadTree get(final Geometry geometry) {
    if (geometry != null && !geometry.isEmpty()) {
      final Reference<GeometrySegmentQuadTree> reference = GeometryProperties.getGeometryProperty(
        geometry, GEOMETRY_SEGMENT_INDEX);
      GeometrySegmentQuadTree index;
      if (reference == null) {
        index = null;
      } else {
        index = reference.get();
      }
      if (index == null) {
        index = new GeometrySegmentQuadTree(geometry);
        GeometryProperties.setGeometryProperty(geometry, GEOMETRY_SEGMENT_INDEX,
          new SoftReference<GeometrySegmentQuadTree>(index));
      }
      return index;
    }
    return new GeometrySegmentQuadTree(null);
  }

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
    return this.geometry.getSegment(segmentId);
  }

  public List<Segment> getWithinDistance(final Point point, final double maxDistance) {
    BoundingBox boundingBox = point.getBoundingBox();
    boundingBox = boundingBox.expand(maxDistance);
    final LineSegmentCoordinateDistanceFilter filter = new LineSegmentCoordinateDistanceFilter(
      point, maxDistance);
    return query(boundingBox, filter);
  }

}
