package com.revolsys.gis.jts;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.jts.locator.SortedPackedIntervalRTree;
import com.revolsys.jts.algorithm.PointInArea;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Location;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.segment.LineSegment;
import com.revolsys.jts.geom.segment.Segment;

public class IndexedPointInAreaLocator {

  private static class IntervalIndexedGeometry {
    private final SortedPackedIntervalRTree<LineSegment> index = new SortedPackedIntervalRTree<LineSegment>();

    public IntervalIndexedGeometry(final Geometry geom) {
      init(geom);
    }

    private void addLine(final LineString line) {
      for (final Segment segment : line.segments()) {
        final double y1 = segment.getY(0);
        final double y2 = segment.getY(1);
        final double min = Math.min(y1, y2);
        final double max = Math.max(y1, y2);
        index.insert(min, max, segment.clone());
      }
    }

    private void init(final Geometry geometry) {
      for (final LineString line : geometry.getGeometryComponents(LineString.class)) {
        addLine(line);
      }
    }

    public void query(final double min, final double max,
      final Visitor<LineSegment> visitor) {
      index.query(min, max, visitor);
    }
  }

  private static final String KEY = IndexedPointInAreaLocator.class.getName();

  public static IndexedPointInAreaLocator get(final Geometry geometry) {
    IndexedPointInAreaLocator locator = GeometryProperties.getGeometryProperty(
      geometry, KEY);
    if (locator == null) {
      locator = new IndexedPointInAreaLocator(geometry);
      GeometryProperties.setGeometryProperty(geometry, KEY, locator);
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
    return geometry.getGeometryFactory();
  }

  public IntervalIndexedGeometry getIndex() {
    return index;
  }

  public Location locate(final double x, final double y) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final double resolutionXy = geometryFactory.getResolutionXy();
    final double minY = y - resolutionXy;
    final double maxY = y + resolutionXy;
    final PointInArea visitor = new PointInArea(geometryFactory, x, y);
    index.query(minY, maxY, visitor);

    return visitor.getLocation();
  }

  public Location locate(final Point coordinates) {
    final double x = coordinates.getX();
    final double y = coordinates.getY();
    return locate(x, y);
  }

}
