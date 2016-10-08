package com.revolsys.geometry.index.quadtree;

import java.util.List;

import com.revolsys.collection.map.WeakKeyValueMap;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.coordinates.filter.LineSegmentCoordinateDistanceFilter;
import com.revolsys.geometry.model.segment.Segment;
import com.revolsys.util.Property;

public class GeometrySegmentQuadTree extends IdObjectQuadTree<Segment> {

  private static final long serialVersionUID = 1L;

  private static final WeakKeyValueMap<Geometry, GeometrySegmentQuadTree> CACHE = new WeakKeyValueMap<>();

  public static GeometrySegmentQuadTree get(final Geometry geometry) {
    if (Property.hasValue(geometry)) {
      GeometrySegmentQuadTree index = CACHE.get(geometry);
      if (index == null) {
        index = new GeometrySegmentQuadTree(geometry);
        CACHE.put(geometry, index);
      }
      return index;
    } else {
      return null;
    }
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
    return boundingBox.getMinMaxValues(2);
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
