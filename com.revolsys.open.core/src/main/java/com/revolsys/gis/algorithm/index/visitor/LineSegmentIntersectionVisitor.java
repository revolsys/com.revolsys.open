package com.revolsys.gis.algorithm.index.visitor;

import java.util.LinkedHashSet;
import java.util.Set;

import com.revolsys.collection.Visitor;
import com.revolsys.jts.geom.PointList;
import com.revolsys.jts.geom.LineSegment;

public class LineSegmentIntersectionVisitor implements Visitor<LineSegment> {

  private final Set<PointList> intersections = new LinkedHashSet<PointList>();

  private final LineSegment querySeg;

  public LineSegmentIntersectionVisitor(final LineSegment querySeg) {
    this.querySeg = querySeg;
  }

  public Set<PointList> getIntersections() {
    return intersections;
  }

  @Override
  public boolean visit(final LineSegment segment) {
    if (segment.getBoundingBox().intersects(querySeg.getBoundingBox())) {
      final PointList intersection = querySeg.getIntersection(segment);
      if (intersection != null && intersection.size() > 0) {
        intersections.add(intersection);
      }
    }
    return true;
  }
}
