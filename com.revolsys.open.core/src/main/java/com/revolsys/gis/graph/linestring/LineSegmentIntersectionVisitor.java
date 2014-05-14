package com.revolsys.gis.graph.linestring;

import java.util.LinkedHashSet;
import java.util.Set;

import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.visitor.AbstractEdgeListenerVisitor;
import com.revolsys.jts.geom.PointList;
import com.revolsys.jts.geom.LineSegment;

public class LineSegmentIntersectionVisitor extends
  AbstractEdgeListenerVisitor<LineSegment> {

  private final Set<PointList> intersections = new LinkedHashSet<PointList>();

  private final LineSegment querySeg;

  public LineSegmentIntersectionVisitor(final LineSegment querySeg) {
    this.querySeg = querySeg;
  }

  public Set<PointList> getIntersections() {
    return intersections;
  }

  @Override
  public boolean visit(final Edge<LineSegment> edge) {
    final LineSegment lineSegment = edge.getObject();
    if (lineSegment.getBoundingBox().intersects(querySeg.getBoundingBox())) {
      final PointList intersection = querySeg.getIntersection(lineSegment);
      if (intersection != null && intersection.size() > 0) {
        intersections.add(intersection);
      }
    }
    return true;
  }
}
