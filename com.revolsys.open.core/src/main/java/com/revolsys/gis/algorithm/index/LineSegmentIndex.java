package com.revolsys.gis.algorithm.index;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.gis.algorithm.index.visitor.LineSegmentIntersectionVisitor;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListIndexLineSegmentIterator;
import com.revolsys.gis.model.geometry.LineSegment;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.index.quadtree.Quadtree;

public class LineSegmentIndex extends Quadtree {
  public void insert(final Geometry geometry) {
    for (int i = 0; i < geometry.getNumGeometries(); i++) {
      final Geometry subGeometry = geometry.getGeometryN(i);
      if (subGeometry instanceof LineString) {
        final LineString line = (LineString)subGeometry;
        insert(line);
      }
    }
  }

  public void insert(final LineSegment lineSegment) {
    final Envelope envelope = lineSegment.getEnvelope();
    insert(envelope, lineSegment);
  }

  public void insert(final LineString line) {
    final CoordinatesListIndexLineSegmentIterator segments = new CoordinatesListIndexLineSegmentIterator(
      line);
    for (final LineSegment lineSegment : segments) {
      insert(lineSegment);
    }
  }

  public boolean isWithinDistance(final Coordinates point) {
    final Envelope envelope = new BoundingBox(point);
    envelope.expandBy(1);
    @SuppressWarnings("unchecked")
    final List<LineSegment> lines = query(envelope);
    for (final LineSegment line : lines) {
      if (line.distance(point) <= 1) {
        return true;
      }
    }

    return false;
  }

  public List<CoordinatesList> queryIntersections(final Coordinates c0,
    final Coordinates c1) {
    return queryIntersections(new LineSegment(c0, c1));
  }

  public List<CoordinatesList> queryIntersections(final LineSegment querySeg) {
    final Envelope env = querySeg.getEnvelope();
    final LineSegmentIntersectionVisitor visitor = new LineSegmentIntersectionVisitor(
      querySeg);
    query(env, visitor);
    final List<CoordinatesList> intersections = new ArrayList<CoordinatesList>(
      visitor.getIntersections());
    return intersections;
  }
}
