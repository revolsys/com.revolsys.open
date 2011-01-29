package com.revolsys.gis.graph.linestring;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListIndexLineSegmentIterator;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.geometry.LineSegment;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

public class LineStringGraph extends Graph<LineSegment> {

  private LineString lineString;

  private GeometryFactory geometryFactory;

  private CoordinatesPrecisionModel precisionModel;

  public LineStringGraph(final LineString lineString) {
    this.lineString = lineString;
    geometryFactory = GeometryFactory.getFactory(lineString);
    precisionModel = geometryFactory.getCoordinatesPrecisionModel();
    CoordinatesList points = CoordinatesListUtil.get(lineString);
    CoordinatesListIndexLineSegmentIterator iterator = new CoordinatesListIndexLineSegmentIterator(
      geometryFactory, points);
    for (LineSegment lineSegment : iterator) {
      add(lineSegment, lineSegment.getLine());
    }
  }

  public Geometry getSelfIntersections() {
    Set<Coordinates> intersectionPoints = new HashSet<Coordinates>();
    CoordinatesList points = CoordinatesListUtil.get(lineString);
    for (Coordinates point : points) {
      Node<LineSegment> node = getNode(point);
      if (node.getDegree() > 2 || hasTouchingEdges(node)) {
        intersectionPoints.add(point);
      }
    }
    for (Edge<LineSegment> edge1 : getEdges()) {
      LineSegment lineSegment1 = edge1.getObject();
      List<Edge<LineSegment>> edges = getEdges(edge1);
      for (Edge<LineSegment> edge2 : edges) {
        if (edge1 != edge2) {
          LineSegment lineSegment2 = edge2.getObject();
          List<Coordinates> intersections = lineSegment1.intersection(
            precisionModel, lineSegment2);
          for (Coordinates intersection : intersections) {
            if (!lineSegment1.contains(intersection)
              && !lineSegment2.contains(intersection)) {
              intersectionPoints.add(intersection);
            }
          }
        }
      }
    }
    return geometryFactory.createMultiPoint(intersectionPoints);
  }

  public boolean hasTouchingEdges(Node<LineSegment> node) {
    List<Edge<LineSegment>> edges = findEdges(node, precisionModel.getScaleXY());
    for (Edge<LineSegment> edge : edges) {
      Coordinates lineStart = edge.getFromNode();
      Coordinates lineEnd = edge.getToNode();
      if (LineSegmentUtil.isPointOnLineMiddle(precisionModel, lineStart,
        lineEnd, node)) {
        return true;
      }
    }
    return false;
  }

}
