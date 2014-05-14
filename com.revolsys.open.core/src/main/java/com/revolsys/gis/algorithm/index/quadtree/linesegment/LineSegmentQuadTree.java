package com.revolsys.gis.algorithm.index.quadtree.linesegment;

import java.util.Collections;
import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.filter.Filter;
import com.revolsys.gis.jts.GeometryEditUtil;
import com.revolsys.gis.jts.LineSegmentImpl;
import com.revolsys.gis.model.coordinates.filter.LineSegmentCoordinateDistanceFilter;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.PointList;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineSegment;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.visitor.CreateListVisitor;

public class LineSegmentQuadTree {
  public static Envelope ensureExtent(final Envelope envelope,
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
    if (geometry == null || geometry.isEmpty()) {
    } else if (geometry instanceof Point) {
    } else if (geometry instanceof MultiPoint) {
    } else if (geometry instanceof LineString) {
      final PointList points = CoordinatesListUtil.get(geometry);
      add(points);
    } else if (geometry instanceof Polygon) {
      final Polygon polygon = (Polygon)geometry;
      final List<PointList> rings = CoordinatesListUtil.getAll(polygon);
      for (int ringIndex = 0; ringIndex < rings.size(); ringIndex++) {
        final PointList points = rings.get(ringIndex);
        add(points, ringIndex);
      }
    } else {
      for (int partIndex = 0; partIndex < geometry.getGeometryCount(); partIndex++) {
        final Geometry part = geometry.getGeometry(partIndex);
        if (part instanceof Point) {
        } else if (part instanceof LineString) {
          final LineString line = (LineString)part;
          final PointList points = CoordinatesListUtil.get(line);
          add(points, partIndex);
        } else if (part instanceof Polygon) {
          final Polygon polygon = (Polygon)part;
          final List<PointList> rings = CoordinatesListUtil.getAll(polygon);
          for (int ringIndex = 0; ringIndex < rings.size(); ringIndex++) {
            final PointList points = rings.get(ringIndex);
            add(points, partIndex, ringIndex);
          }
        }
      }
    }
  }

  private void add(final PointList points, final int... parentIndex) {
    double x1 = points.getX(0);
    double y1 = points.getY(0);
    for (int segmentIndex = 0; segmentIndex < points.size() - 1; segmentIndex++) {
      final int segmentEndVertexIndex = segmentIndex + 1;
      final double x2 = points.getX(segmentEndVertexIndex);
      final double y2 = points.getY(segmentEndVertexIndex);
      final int[] index = GeometryEditUtil.createVertexIndex(parentIndex,
        segmentIndex);
      final Envelope envelope = new Envelope(2, x1, y1, x2, y2);
      insert(envelope, index);
      x1 = x2;
      y1 = y2;
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

  public List<LineSegment> getAll() {
    final CreateListVisitor<LineSegment> visitor = new CreateListVisitor<LineSegment>();
    root.visit(this, visitor);
    return visitor.getList();
  }

  protected com.revolsys.jts.geom.BoundingBox getEnvelope(final int[] index) {
    final PointList points = GeometryEditUtil.getPoints(geometry, index);
    final int vertexIndex = GeometryEditUtil.getVertexIndex(index);
    final double x1 = points.getX(vertexIndex);
    final double y1 = points.getY(vertexIndex);
    final double x2 = points.getX(vertexIndex + 1);
    final double y2 = points.getY(vertexIndex + 1);
    return new Envelope(2, x1, y1, x2, y2);
  }

  public List<LineSegment> getIntersecting(final BoundingBox boundingBox) {
    final CreateListVisitor<LineSegment> visitor = new CreateListVisitor<LineSegment>();
    visit(boundingBox, visitor);
    return visitor.getList();
  }

  public List<LineSegment> getIntersectingBoundingBox(final Geometry geometry) {
    if (geometry == null) {
      return Collections.emptyList();
    } else {
      final BoundingBox boundingBox = geometry.getBoundingBox();
      return getIntersecting(boundingBox);
    }
  }

  protected LineSegment getLineSegment(final int[] index) {
    final PointList points = GeometryEditUtil.getPoints(geometry, index);
    final int vertexIndex = GeometryEditUtil.getVertexIndex(index);
    final Point p1 = points.get(vertexIndex);
    final Point p2 = points.get(vertexIndex + 1);
    return new LineSegmentImpl(GeometryFactory.getFactory(geometry), p1, p2);
  }

  public int getSize() {
    return size;
  }

  public List<LineSegment> getWithin(final BoundingBox boundingBox,
    final Filter<LineSegment> filter) {
    final CreateListVisitor<LineSegment> visitor = new CreateListVisitor<LineSegment>(
      filter);
    visit(boundingBox, visitor);
    return visitor.getList();
  }

  public List<LineSegment> getWithinDistance(final Point point,
    final double maxDistance) {
    BoundingBox boundingBox = new Envelope(point);
    boundingBox = boundingBox.expand(maxDistance);
    final LineSegmentCoordinateDistanceFilter filter = new LineSegmentCoordinateDistanceFilter(
      point, maxDistance);
    return getWithin(boundingBox, filter);
  }

  public void insert(final Envelope envelope, final int[] index) {
    size++;
    collectStats(envelope);
    final Envelope insertEnv = ensureExtent(envelope, minExtent);
    root.insert(insertEnv, index);
  }

  public int size() {
    return getSize();
  }

  public void visit(final BoundingBox boundingBox,
    final Visitor<LineSegment> visitor) {
    root.visit(this, boundingBox, visitor);
  }

}
