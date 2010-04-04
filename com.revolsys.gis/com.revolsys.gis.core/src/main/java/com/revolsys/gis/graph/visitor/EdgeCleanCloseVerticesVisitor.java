package com.revolsys.gis.graph.visitor;

import java.util.LinkedHashSet;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.visitor.Visitor;
import com.revolsys.gis.event.CoordinateEventListenerList;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.event.EdgeEvent;
import com.revolsys.gis.graph.event.EdgeEventListenerList;
import com.revolsys.gis.model.coordinates.CoordinateSequenceCoordinatesIterator;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.util.MathUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

public class EdgeCleanCloseVerticesVisitor<T> implements Visitor<Edge<T>> {

  private final CoordinateEventListenerList coordinateListeners = new CoordinateEventListenerList();

  private final EdgeEventListenerList<T> edgeListeners = new EdgeEventListenerList<T>();

  private final Graph<T> graph;

  private final double minDistance;

  private Visitor<Edge<T>> visitor;

  public EdgeCleanCloseVerticesVisitor(
    final Graph<T> graph,
    final double minDistance) {
    this.graph = graph;
    this.minDistance = minDistance;
  }

  public EdgeCleanCloseVerticesVisitor(
    final Graph<T> graph,
    final double minDistance,
    final Visitor<Edge<T>> visitor) {
    this.graph = graph;
    this.minDistance = minDistance;
    this.visitor = visitor;
  }

  private double getAngle(
    final Edge<T> edge,
    final CoordinateSequenceCoordinatesIterator ordinates,
    final int relativeIndex) {
    final int index = ordinates.getIndex();
    if (index + relativeIndex - 1 < 0
      || index + relativeIndex + 1 >= ordinates.size()) {
      return Double.NaN;
    } else {
      final double x1 = ordinates.getValue(relativeIndex - 1, 0);
      final double y1 = ordinates.getValue(relativeIndex - 1, 1);
      final double x2 = ordinates.getValue(relativeIndex, 0);
      final double y2 = ordinates.getValue(relativeIndex, 1);
      final double x3 = ordinates.getValue(relativeIndex + 1, 0);
      final double y3 = ordinates.getValue(relativeIndex + 1, 1);
      return MathUtil.angle(x1, y1, x2, y2, x3, y3);
    }
  }

  public CoordinateEventListenerList getCoordinateListeners() {
    return coordinateListeners;
  }

  public EdgeEventListenerList<T> getEdgeListeners() {
    return edgeListeners;
  }

  // TODO look at the angles with the previous and next segments to decide
  // which coordinate to remove. If there is a right angle in a building then
  // it should probably not be removed. This would be confirmed by the angles
  // of the next and previous segments.
  /**
   * Visit the edge performing any required cleanup.
   * 
   * @param edge The edge to visit.
   * @return true If further edges should be processed.
   */
  public boolean visit(
    final Edge<T> edge) {
    final QName typeName = edge.getTypeName();
    final LineString lineString = edge.getLine();
    final CoordinateSequence coordinates = lineString.getCoordinateSequence();
    final int numCoordinates = coordinates.size();
    if (numCoordinates > 2) {
      final GeometryFactory geometryFactory = lineString.getFactory();
      final CoordinateSequenceCoordinatesIterator ordinates = new CoordinateSequenceCoordinatesIterator(
        coordinates);
      final LinkedHashSet<Integer> removeIndicies = new LinkedHashSet<Integer>();

      double x1 = ordinates.getValue(0);
      double y1 = ordinates.getValue(1);
      while (ordinates.hasNext()) {
        ordinates.next();
        final double x2 = ordinates.getValue(0);
        final double y2 = ordinates.getValue(1);
        final double distance = MathUtil.distance(x1, y1, x2, y2);
        if (distance < minDistance) {
          final double previousAngle = getAngle(edge, ordinates, -1);
          final double angle = getAngle(edge, ordinates, 0);
          final double nextAngle = getAngle(edge, ordinates, 1);
          boolean fixed = false;
          if (angle > previousAngle) {
            if (angle > nextAngle) {
              if (angle > Math.toRadians(160)) {
                fixed = true;
              }
            }
          } else if (previousAngle > nextAngle) {

          }
          if (fixed) {
            coordinateListeners.coordinateEvent(new Coordinate(x2, y2),
              typeName, "Short Segment", "Fixed", distance + " "
                + Math.toDegrees(previousAngle) + " " + Math.toDegrees(angle)
                + " " + Math.toDegrees(nextAngle));
          } else {
            coordinateListeners.coordinateEvent(new Coordinate(x2, y2),
              typeName, "Short Segment", "Review", distance + " "
                + Math.toDegrees(previousAngle) + " " + Math.toDegrees(angle)
                + " " + Math.toDegrees(nextAngle));
          }
        }
        x1 = x2;
        y1 = y2;
      }
      if (!removeIndicies.isEmpty()) {
        final int dimension = coordinates.getDimension();
        final CoordinateSequence newCoordinates = new DoubleCoordinatesList(
          numCoordinates - removeIndicies.size(), dimension);
        int k = 0;
        for (int j = 0; j < numCoordinates; j++) {
          if (!removeIndicies.contains(j)) {
            for (int d = 0; d < dimension; d++) {
              final double ordinate = coordinates.getOrdinate(j, d);
              newCoordinates.setOrdinate(k, d, ordinate);
            }
            k++;
          }
        }
        final LineString newLine = geometryFactory.createLineString(newCoordinates);
        final Edge<T> newEdge = graph.replaceEdge(edge, newLine);
        edgeListeners.edgeEvent(newEdge, "Edge close indicies",
          EdgeEvent.EDGE_CHANGED, null);
      }
    }
    return true;
  }

}
