package com.revolsys.gis.algorithm.index.visitor;

import java.util.LinkedHashSet;
import java.util.Set;

import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.LineSegment;
import com.revolsys.jts.index.ItemVisitor;

public class LineSegmentIntersectionVisitor implements ItemVisitor {

  private final Set<CoordinatesList> intersections = new LinkedHashSet<CoordinatesList>();

  private final LineSegment querySeg;

  public LineSegmentIntersectionVisitor(final LineSegment querySeg) {
    this.querySeg = querySeg;
  }

  public Set<CoordinatesList> getIntersections() {
    return intersections;
  }

  @Override
  public void visitItem(final Object item) {
    final LineSegment segment = (LineSegment)item;
    if (segment.getBoundingBox().intersects(querySeg.getBoundingBox())) {
      final CoordinatesList intersection = querySeg.getIntersection(segment);
      if (intersection != null && intersection.size() > 0) {
        intersections.add(intersection);
      }
    }

  }
}
