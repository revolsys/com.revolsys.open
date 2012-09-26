package com.revolsys.gis.model.geometry.algorithm.locate;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.geometry.Geometry;
import com.revolsys.gis.model.geometry.LineSegment;
import com.revolsys.gis.model.geometry.Polygonal;
import com.revolsys.gis.model.geometry.algorithm.RayCrossingCounter;
import com.vividsolutions.jts.index.ItemVisitor;
import com.vividsolutions.jts.index.intervalrtree.SortedPackedIntervalRTree;

public class IndexedPointInAreaLocator implements PointOnGeometryLocator {

  private static class IntervalIndexedGeometry {
    private final SortedPackedIntervalRTree index = new SortedPackedIntervalRTree();

    public IntervalIndexedGeometry(final Geometry geom) {
      init(geom);
    }

    private void addLine(final CoordinatesList points) {
      int size = points.size();
      if (size > 1) {
        for (int i = 1; i < size; i++) {
          final LineSegment seg = new LineSegment(points.get(i - 1),
            points.get(i));
          final double y1 = seg.getY(0);
          final double y2 = seg.getY(1);
          final double min = Math.min(y1, y2);
          final double max = Math.max(y1, y2);
          index.insert(min, max, seg);
        }
      }
    }

    private void init(final Geometry geometry) {
      for (CoordinatesList points : geometry.getCoordinatesLists()) {
        addLine(points);
      }
    }

    public void query(final double min, final double max,
      final ItemVisitor visitor) {
      index.query(min, max, visitor);
    }
  }

  private static class SegmentVisitor implements ItemVisitor {
    private final RayCrossingCounter counter;

    public SegmentVisitor(final RayCrossingCounter counter) {
      this.counter = counter;
    }

    @Override
    public void visitItem(final Object item) {
      final LineSegment seg = (LineSegment)item;
      counter.countSegment(seg.get(0), seg.get(1));
    }
  }

  private static final String KEY = IndexedPointInAreaLocator.class.getName();

  public static PointOnGeometryLocator get(final Geometry geometry) {
    PointOnGeometryLocator locator = geometry.getProperty(KEY);
    if (locator == null) {
      locator = new IndexedPointInAreaLocator(geometry);
      geometry.setPropertySoft(KEY, locator);
    }
    return locator;
  }

  private IntervalIndexedGeometry index;

  /**
   * Creates a new locator for a given {@link Geometry}
   * 
   * @param geometry the Geometry to locate in
   */
  public IndexedPointInAreaLocator(final Geometry geometry) {
    if (!(geometry instanceof Polygonal)) {
      throw new IllegalArgumentException("Argument must be Polygonal");
    }
    buildIndex(geometry);
  }

  private void buildIndex(final Geometry geometry) {
    index = new IntervalIndexedGeometry(geometry);
  }

  @Override
  public int locate(final Coordinates coordinates) {
    final RayCrossingCounter rcc = new RayCrossingCounter(coordinates);

    final SegmentVisitor visitor = new SegmentVisitor(rcc);
    index.query(coordinates.getY(), coordinates.getY(), visitor);

    return rcc.getLocation();
  }

}
