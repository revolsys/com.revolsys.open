package com.revolsys.gis.graph.visitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.revolsys.collection.Visitor;
import com.revolsys.filter.Filter;
import com.revolsys.filter.FilterUtil;
import com.revolsys.gis.algorithm.linematch.LineMatchGraph;
import com.revolsys.gis.algorithm.linematch.LineSegmentMatch;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.graph.filter.LineFilter;
import com.revolsys.gis.jts.JtsGeometryUtil;
import com.revolsys.gis.jts.filter.EqualFilter;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.geometry.LineSegment;
import com.vividsolutions.jts.geom.LineString;

public class SplitIntersectingEdgeVisitor implements Visitor<Edge<DataObject>> {

  /**
   * Split the line into segments which are either within or not within 1m of
   * the other line. The result is a list of split lines from the line. For each
   * section which is within 1m any additional noding from the other line will
   * be introduced, this may result in the line being slightly different from
   * the other line.
   * 
   * @param graph The graph containing the two lines.
   * @param line The line
   * @param index The index of the line in the graph.
   * @return The split lines.
   */
  private List<LineString> getSplitLines(
    final LineMatchGraph<LineSegmentMatch> graph, final LineString line,
    final int index) {
    final Coordinates startCoordinate = CoordinatesUtil.get(line);
    Node<LineSegmentMatch> currentNode = graph.findNode(startCoordinate);
    final Set<Edge<LineSegmentMatch>> processedEdges = new HashSet<Edge<LineSegmentMatch>>();
    boolean started = false;
    boolean matched = false;
    final List<Coordinates> coordinates = new ArrayList<Coordinates>();
    final GeometryFactory factory = GeometryFactory.getFactory(line);
    final List<LineString> lines = new ArrayList<LineString>();

    while (currentNode != null) {
      final Coordinates coordinate = currentNode;
      coordinates.add(coordinate);
      final Edge<LineSegmentMatch> edge = graph.getUnprocessedEdgeWithSegment(
        currentNode, index, processedEdges);
      if (edge != null) {
        final LineSegmentMatch lineEdge = edge.getObject();
        boolean hasMatches = lineEdge.hasMatches(index);
        if (!hasMatches) {
          final Node<LineSegmentMatch> endNode = edge.getOppositeNode(currentNode);
          for (final Edge<LineSegmentMatch> matchEdge : Node.getEdgesBetween(
            currentNode, endNode)) {
            if (matchEdge != edge) {
              final List<LineSegment> segments = matchEdge.getObject()
                .getSegments();
              for (int i = 0; i < segments.size(); i++) {
                if (i != index) {
                  final LineSegment segment = segments.get(i);
                  if (segment != null) {
                    hasMatches = true;
                  }
                }
              }
            }
          }

        }
        if (started) {
          if (hasMatches != matched) {
            final LineString newLine = factory.createLineString(coordinates);
            newLine.setUserData(line.getUserData());
            JtsGeometryUtil.setGeometryProperty(newLine, "mtached", matched);
            lines.add(newLine);
            matched = hasMatches;
            coordinates.clear();
            coordinates.add(coordinate);
          }
        } else {
          started = true;
          matched = hasMatches;
        }
        processedEdges.add(edge);
        currentNode = edge.getToNode();
      } else {
        currentNode = null;
      }
    }
    if (coordinates.size() > 1) {
      final LineString newLine = factory.createLineString(coordinates);
      newLine.setUserData(line.getUserData());
      lines.add(newLine);
    }
    return lines;
  }

  /**
   * Split each of the two lines into segments which are either within or not
   * within 1m of the other line. The result is a list with the first element
   * being the list of split lines from the first line and the second element
   * being the list of split lines from the second line. For each section which
   * is within 1m any additional noding from the other line will be introduced,
   * this may result in the line being slightly different from the other line.
   * 
   * @param line1 The first line.
   * @param line2 The second line.
   * @return The split lines.
   */
  private List<List<LineString>> getSplitLines(final LineString line1,
    final LineString line2) {
    final LineMatchGraph<LineSegmentMatch> graph = new LineMatchGraph<LineSegmentMatch>(
      line1);
    graph.add(line2);
    final List<List<LineString>> lines = new ArrayList<List<LineString>>();
    final List<LineString> lines1 = getSplitLines(graph, line1, 0);
    lines.add(lines1);
    final List<LineString> lines2 = getSplitLines(graph, line2, 1);
    lines.add(lines2);
    return lines;
  }

  /**
   * Visit each edge in the graph to find other edges which linearly intersect
   * the edge and are not geometrically equal. The current edge and the matched
   * edge will be split into parts for each section which is equal and each
   * section which is not equal.
   * 
   * @param edge The edge to process.
   * @return True
   */
  @Override
  public boolean visit(final Edge<DataObject> edge) {
    final LineString line = edge.getLine();
    final List<Edge<DataObject>> intersectEdges = EdgeIntersectsLinearlyEdgeVisitor.getEdges(
      edge.getGraph(), edge);
    if (!intersectEdges.isEmpty()) {
      final Filter<Edge<DataObject>> edgeEqualFilter = new LineFilter<DataObject>(
        new EqualFilter<LineString>(line));
      FilterUtil.remove(intersectEdges, edgeEqualFilter);
      for (final Edge<DataObject> edge2 : intersectEdges) {
        if (!edge2.isRemoved()) {
          final LineString line2 = edge2.getLine();
          final List<List<LineString>> lines = getSplitLines(line, line2);
          final List<LineString> lines1 = lines.get(0);
          final List<LineString> lines2 = lines.get(1);
          if (!lines1.isEmpty() && !lines2.isEmpty()) {
            if ((lines1.size() > 1 && lines2.size() > 1)) {
              edge.replace(lines1);
              edge2.replace(lines2);
              return true;
            }
          }
        }
      }
    }
    return true;
  }
}
