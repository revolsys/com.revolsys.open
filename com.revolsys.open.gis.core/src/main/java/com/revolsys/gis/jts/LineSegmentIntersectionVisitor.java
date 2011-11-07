package com.revolsys.gis.jts;

import java.util.LinkedHashSet;
import java.util.Set;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.ItemVisitor;

public class LineSegmentIntersectionVisitor implements ItemVisitor {

  private final Set<Coordinate> intersections = new LinkedHashSet<Coordinate>();

  private final LineSegment3D querySeg;

  public LineSegmentIntersectionVisitor(
    final LineSegment3D querySeg) {
    this.querySeg = querySeg;
  }

  public Set<Coordinate> getIntersections() {
    return intersections;
  }

  public void visitItem(
    final Object item) {
    final LineSegment3D segment = (LineSegment3D)item;
    if (Envelope.intersects(segment.p0, segment.p1, querySeg.p0, querySeg.p1)) {
      final Coordinate intersection = querySeg.intersection3D(segment);
      if (intersection != null) {
        intersections.add(intersection);
      }
    }

  }
}
