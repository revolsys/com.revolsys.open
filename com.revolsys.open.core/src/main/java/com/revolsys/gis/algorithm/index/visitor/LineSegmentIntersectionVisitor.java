package com.revolsys.gis.algorithm.index.visitor;

import java.util.LinkedHashSet;
import java.util.Set;

import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.geometry.LineSegment;
import com.vividsolutions.jts.index.ItemVisitor;

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
    if (segment.getEnvelope().intersects(querySeg.getEnvelope())) {
      final CoordinatesList intersection = querySeg.getIntersection(segment);
      if (intersection != null && intersection.size() > 0) {
        intersections.add(intersection);
      }
    }

  }
}
