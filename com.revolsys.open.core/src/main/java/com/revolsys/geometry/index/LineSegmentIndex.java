package com.revolsys.geometry.index;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.index.quadtree.QuadTree;
import com.revolsys.geometry.index.visitor.LineSegmentIntersectionVisitor;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.LineSegmentDoubleGF;

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
    BoundingBox envelope = point.getBoundingBox();
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
    final LineSegmentIntersectionVisitor visitor = new LineSegmentIntersectionVisitor(querySeg);
    forEach(visitor, env);
    final List<Geometry> intersections = new ArrayList<>(visitor.getIntersections());
    return intersections;
  }

  public List<Geometry> queryIntersections(final Point c0, final Point c1) {
    return queryIntersections(new LineSegmentDoubleGF(c0, c1));
  }
}
