package com.revolsys.gis.jts;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.geometry.LineSegment;
import com.revolsys.gis.model.geometry.algorithm.locate.Location;
import com.revolsys.gis.model.geometry.algorithm.locate.PointOnGeometryLocator;
import com.revolsys.gis.model.geometry.index.SortedPackedIntervalRTree;
import com.revolsys.jts.algorithm.PointInArea;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;

public class IndexedPointInAreaLocator implements PointOnGeometryLocator {

  private static class IntervalIndexedGeometry {
    private final SortedPackedIntervalRTree<LineSegment> index = new SortedPackedIntervalRTree<LineSegment>();

    public IntervalIndexedGeometry(final Geometry geom) {
      init(geom);
    }

    private void addLine(final CoordinatesList points) {
      final int size = points.size();
      if (size > 1) {
        for (int i = 1; i < size; i++) {
          final double x1 = points.getX(i - 1);
          final double x2 = points.getX(i);
          final double y1 = points.getY(i - 1);
          final double y2 = points.getY(i);
          final LineSegment seg = new LineSegment(x1, y1, x2, y2);
          final double min = Math.min(y1, y2);
          final double max = Math.max(y1, y2);
          index.insert(min, max, seg);
        }
      }
    }

    private void init(final Geometry geometry) {
      for (final CoordinatesList points : CoordinatesListUtil.getAll(geometry)) {
        addLine(points);
      }
    }

    public void query(final double min, final double max,
      final Visitor<LineSegment> visitor) {
      index.query(min, max, visitor);
    }
  }

  private static final String KEY = IndexedPointInAreaLocator.class.getName();

  public static PointOnGeometryLocator get(final Geometry geometry) {
    PointOnGeometryLocator locator = JtsGeometryUtil.getGeometryProperty(
      geometry, KEY);
    if (locator == null) {
      locator = new IndexedPointInAreaLocator(geometry);
      JtsGeometryUtil.setGeometryProperty(geometry, KEY, locator);
    }
    return locator;
  }

  private final IntervalIndexedGeometry index;

  private final Geometry geometry;

  /**
   * Creates a new locator for a given {@link Geometry}
   * 
   * @param geometry the Geometry to locate in
   */
  public IndexedPointInAreaLocator(final Geometry geometry) {
    this.geometry = geometry;
    this.index = new IntervalIndexedGeometry(geometry);
  }

  public Geometry getGeometry() {
    return geometry;
  }

  public GeometryFactory getGeometryFactory() {
    return GeometryFactory.getFactory(geometry);
  }

  public IntervalIndexedGeometry getIndex() {
    return index;
  }

  @Override
  public Location locate(final Coordinates coordinates) {
    final double x = coordinates.getX();
    final double y = coordinates.getY();
    return locate(x, y);
  }

  @Override
  public Location locate(final double x, final double y) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final double resolutionXy = geometryFactory.getResolutionXy();
    final double minY = y - resolutionXy;
    final double maxY = y + resolutionXy;
    final PointInArea visitor = new PointInArea(geometryFactory, x, y);
    index.query(minY, maxY, visitor);

    return visitor.getLocation();
  }

}
