package com.revolsys.geometry.algorithm.locate;

import java.util.function.Consumer;

import com.revolsys.geometry.index.intervalrtree.SortedPackedIntervalRTree;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.LineSegmentDoubleXY;
import com.revolsys.util.function.Consumer4Double;

public class GeometrySegmentYIntervalIndex {
  public static GeometrySegmentYIntervalIndex sortY(final Geometry geometry) {
    final SortedPackedIntervalRTree<LineSegmentDoubleXY> index = new SortedPackedIntervalRTree<>();

    geometry.forEachSegment((x1, y1, x2, y2) -> {
      if (y1 <= y2) {
        final LineSegmentDoubleXY segment = new LineSegmentDoubleXY(x1, y1, x2, y2);
        index.insert(y1, y2, segment);
      } else {
        final LineSegmentDoubleXY segment = new LineSegmentDoubleXY(x2, y2, x1, y1);
        index.insert(y2, y1, segment);
      }
    });
    return new GeometrySegmentYIntervalIndex(index);
  }

  private final SortedPackedIntervalRTree<LineSegmentDoubleXY> index;

  public GeometrySegmentYIntervalIndex(final Geometry geometry) {
    this(new SortedPackedIntervalRTree<>());
    geometry.forEachSegment((x1, y1, x2, y2) -> {
      double minY;
      double maxY;
      if (y1 < y2) {
        minY = y1;
        maxY = y2;
      } else {
        minY = y2;
        maxY = y1;
      }
      final LineSegmentDoubleXY segment = new LineSegmentDoubleXY(x1, y1, x2, y2);
      this.index.insert(minY, maxY, segment);
    });
  }

  public GeometrySegmentYIntervalIndex(final SortedPackedIntervalRTree<LineSegmentDoubleXY> index) {
    this.index = index;
  }

  public void query(final double y, final Consumer<LineSegment> visitor) {
    this.index.query(y, y, visitor);
  }

  public void query(final double y, final Consumer4Double visitor) {
    this.index.query(y, y, segment -> {
      final double x1 = segment.getX1();
      final double y1 = segment.getY1();
      final double x2 = segment.getX2();
      final double y2 = segment.getY2();
      visitor.accept(x1, y1, x2, y2);
    });
  }

  public void query(final double minY, final double maxY, final Consumer<LineSegment> visitor) {
    this.index.query(minY, maxY, visitor);
  }

  public void query(final double minY, final double maxY, final Consumer4Double visitor) {
    this.index.query(minY, maxY, segment -> {
      final double x1 = segment.getX1();
      final double y1 = segment.getY1();
      final double x2 = segment.getX2();
      final double y2 = segment.getY2();
      visitor.accept(x1, y1, x2, y2);
    });
  }
}
