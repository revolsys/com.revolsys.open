package com.revolsys.gis.algorithm.index.visitor;

import java.util.LinkedHashSet;
import java.util.Set;

import com.revolsys.collection.Visitor;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.segment.LineSegment;

public class LineSegmentIntersectionVisitor implements Visitor<LineSegment> {

  private final Set<Geometry> intersections = new LinkedHashSet<>();

  private final LineSegment querySeg;

  public LineSegmentIntersectionVisitor(final LineSegment querySeg) {
    this.querySeg = querySeg;
  }

  public Set<Geometry> getIntersections() {
    return this.intersections;
  }

  @Override
  public boolean visit(final LineSegment segment) {
    if (segment.getBoundingBox().intersects(this.querySeg.getBoundingBox())) {
      final Geometry intersection = this.querySeg.getIntersection(segment);
      if (intersection != null && intersection.isEmpty()) {
        this.intersections.add(intersection);
      }
    }
    return true;
  }
}
