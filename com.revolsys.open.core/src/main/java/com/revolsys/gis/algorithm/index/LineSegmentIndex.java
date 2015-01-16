package com.revolsys.gis.algorithm.index;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.gis.algorithm.index.quadtree.QuadTree;
import com.revolsys.gis.algorithm.index.visitor.LineSegmentIntersectionVisitor;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Lineal;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.jts.geom.segment.LineSegment;
import com.revolsys.jts.geom.segment.LineSegmentDoubleGF;

public class LineSegmentIndex extends QuadTree<LineSegment> {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public LineSegmentIndex() {
  }

  public LineSegmentIndex(final Lineal line) {
    insert(line);
  }

  public void insert(final Geometry geometry) {
    for (int i = 0; i < geometry.getGeometryCount(); i++) {
      final Geometry subGeometry = geometry.getGeometry(i);
      if (subGeometry instanceof LineString) {
        final LineString line = (LineString)subGeometry;
        insert(line);
      }
    }
  }

  public void insert(final Lineal line) {
    for (final LineSegment lineSegment : line.segments()) {
      final LineSegment clone = (LineSegment)lineSegment.clone();
      insert(clone);
    }
  }

  public void insert(final LineSegment lineSegment) {
    final BoundingBox envelope = lineSegment.getBoundingBox();
    insert(envelope, lineSegment);
  }

  public boolean isWithinDistance(final Point point) {
    BoundingBox envelope = new BoundingBoxDoubleGf(point);
    envelope = envelope.expand(1);
    final List<LineSegment> lines = query(envelope);
    for (final LineSegment line : lines) {
      if (line.distance(point) <= 1) {
        return true;
      }
    }

    return false;
  }

  public List<Geometry> queryIntersections(final LineSegment querySeg) {
    final BoundingBox env = querySeg.getBoundingBox();
    final LineSegmentIntersectionVisitor visitor = new LineSegmentIntersectionVisitor(
      querySeg);
    visit(env, visitor);
    final List<Geometry> intersections = new ArrayList<>(
        visitor.getIntersections());
    return intersections;
  }

  public List<Geometry> queryIntersections(final Point c0, final Point c1) {
    return queryIntersections(new LineSegmentDoubleGF(c0, c1));
  }
}
