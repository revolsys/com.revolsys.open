package com.revolsys.gis.jts;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.index.quadtree.Quadtree;

public class LineSegmentIndex extends Quadtree {
  public void insert(
    final Geometry geometry) {
    for (int i = 0; i < geometry.getNumGeometries(); i++) {
      final Geometry subGeometry = geometry.getGeometryN(i);
      if (subGeometry instanceof LineString) {
        final LineString line = (LineString)subGeometry;
        insert(line);
      }
    }
  }

  public void insert(
    final LineSegment3D lineSegment) {
    final Envelope envelope = lineSegment.getEnvelope();
    insert(envelope, lineSegment);
  }

  public void insert(
    final LineString line) {
    final CoordinateSequenceLineSegmentIterator segments = new CoordinateSequenceLineSegmentIterator(
      line.getCoordinateSequence());
    for (final LineSegment3D lineSegment : segments) {
      insert(lineSegment);
    }
  }

  public List<Coordinate> queryIntersections(
    final LineSegment3D querySeg) {
    final Envelope env = new Envelope(querySeg.p0, querySeg.p1);
    final LineSegmentIntersectionVisitor visitor = new LineSegmentIntersectionVisitor(
      querySeg);
    query(env, visitor);
    final List<Coordinate> intersections = new ArrayList<Coordinate>(
      visitor.getIntersections());
    return intersections;
  }

  public List<Coordinate> queryIntersections(
    Coordinate c0,
    Coordinate c1) {
    return queryIntersections(new LineSegment3D(c0, c1));
  }
}
