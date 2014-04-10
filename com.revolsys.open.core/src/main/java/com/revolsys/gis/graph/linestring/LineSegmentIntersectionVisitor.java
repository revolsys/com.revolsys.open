package com.revolsys.gis.graph.linestring;

import java.util.LinkedHashSet;
import java.util.Set;

import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.visitor.AbstractEdgeListenerVisitor;
import com.revolsys.gis.model.geometry.LineSegment;
import com.revolsys.jts.geom.CoordinatesList;

public class LineSegmentIntersectionVisitor extends
  AbstractEdgeListenerVisitor<LineSegment> {

  private final Set<CoordinatesList> intersections = new LinkedHashSet<CoordinatesList>();

  private final LineSegment querySeg;

  public LineSegmentIntersectionVisitor(final LineSegment querySeg) {
    this.querySeg = querySeg;
  }

  public Set<CoordinatesList> getIntersections() {
    return intersections;
  }

  @Override
  public boolean visit(final Edge<LineSegment> edge) {
    final LineSegment lineSegment = edge.getObject();
    if (lineSegment.getEnvelope().intersects(querySeg.getEnvelope())) {
      final CoordinatesList intersection = querySeg.getIntersection(lineSegment);
      if (intersection != null && intersection.size() > 0) {
        intersections.add(intersection);
      }
    }
    return true;
  }
}
