package com.revolsys.gis.algorithm.index.quadtree.linesegment;

import java.util.Collections;
import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.filter.Filter;
import com.revolsys.filter.InvokeMethodFilter;
import com.revolsys.gis.model.coordinates.filter.LineSegmentCoordinateDistanceFilter;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.segment.Segment;
import com.revolsys.visitor.CreateListVisitor;

public class LineSegmentQuadTree {
  public static BoundingBox ensureExtent(final BoundingBox envelope,
    final double minExtent) {
    double minX = envelope.getMinX();
    double maxX = envelope.getMaxX();
    double minY = envelope.getMinY();
    double maxY = envelope.getMaxY();
    if (minX != maxX && minY != maxY) {
      return envelope;
    }

    if (minX == maxX) {
      minX = minX - minExtent / 2.0;
      maxX = minX + minExtent / 2.0;
    }
    if (minY == maxY) {
      minY = minY - minExtent / 2.0;
      maxY = minY + minExtent / 2.0;
    }
    return new Envelope(2, minX, minY, maxX, maxY);
  }

  private final Geometry geometry;

  private final Root root = new Root();

  private double minExtent = 1.0;

  private int size = 0;

  public LineSegmentQuadTree(final Geometry geometry) {
    this.geometry = geometry;
    if (geometry != null) {
      for (final Segment segment : geometry.segments()) {
        final BoundingBox boundingBox = segment.getBoundingBox();
        final int[] segmentId = segment.getSegmentId();
        insert(boundingBox, segmentId);
      }
    }
  }

  private void collectStats(final com.revolsys.jts.geom.BoundingBox envelope) {
    final double width = envelope.getWidth();
    if (width < minExtent && width > 0.0) {
      minExtent = width;
    }

    final double height = envelope.getHeight();
    if (height < minExtent && height > 0.0) {
      minExtent = height;
    }
  }

  public int depth() {
    return root.depth();
  }

  protected BoundingBox getEnvelope(final int[] index) {
    final Segment segment = geometry.getSegment(index);
    return segment.getBoundingBox();
  }

  public List<Segment> getIntersecting(final BoundingBox boundingBox) {
    final CreateListVisitor<Segment> visitor = new CreateListVisitor<>();
    visit(boundingBox, visitor);
    return visitor.getList();
  }

  public List<Segment> getIntersectingBoundingBox(final Geometry geometry) {
    if (geometry == null) {
      return Collections.emptyList();
    } else {
      final BoundingBox boundingBox = geometry.getBoundingBox();
      return getIntersecting(boundingBox);
    }
  }

  protected Segment getLineSegment(final int[] index) {
    return geometry.getSegment(index);
  }

  public int getSize() {
    return size;
  }

  public List<Segment> getWithin(final BoundingBox boundingBox,
    final Filter<Segment> filter) {
    final CreateListVisitor<Segment> visitor = new CreateListVisitor<>(filter);
    visit(boundingBox, visitor);
    return visitor.getList();
  }

  public List<Segment> getWithinDistance(final Point point,
    final double maxDistance) {
    BoundingBox boundingBox = new Envelope(point);
    boundingBox = boundingBox.expand(maxDistance);
    final LineSegmentCoordinateDistanceFilter filter = new LineSegmentCoordinateDistanceFilter(
      point, maxDistance);
    return getWithin(boundingBox, filter);
  }

  public void insert(final BoundingBox envelope, final int[] index) {
    size++;
    collectStats(envelope);
    final BoundingBox insertEnv = ensureExtent(envelope, minExtent);
    root.insert(insertEnv, index);
  }

  public List<Segment> query(final BoundingBox boundingBox,
    final String methodName, final Object... parameters) {
    final Filter<Segment> filter = new InvokeMethodFilter<>(methodName,
      parameters);
    return getWithin(boundingBox, filter);
  }

  public int size() {
    return getSize();
  }

  public void visit(final BoundingBox boundingBox,
    final Visitor<Segment> visitor) {
    root.visit(this, boundingBox, visitor);
  }

}
