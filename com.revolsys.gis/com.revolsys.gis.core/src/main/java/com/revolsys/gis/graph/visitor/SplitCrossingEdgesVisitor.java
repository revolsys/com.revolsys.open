package com.revolsys.gis.graph.visitor;

import java.util.Collection;
import java.util.List;

import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.EdgeQuadTree;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.graph.event.NodeEventListener;
import com.revolsys.gis.jts.LineStringUtil;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.vividsolutions.jts.geom.LineString;

public class SplitCrossingEdgesVisitor<T> extends
  AbstractEdgeListenerVisitor<T> {
  public static final String CROSSING_EDGES = "Crossing edges";

  private final Graph<T> graph;

  private final SplitEdgesCloseToNodeVisitor<T> splitEdgesCloseToNodeVisitor;

  public SplitCrossingEdgesVisitor(
    final Graph<T> graph) {
    this.graph = graph;
    splitEdgesCloseToNodeVisitor = new SplitEdgesCloseToNodeVisitor<T>(graph,
      CROSSING_EDGES, 1);
  }

  @Override
  public void addNodeListener(
    final NodeEventListener<T> listener) {
    splitEdgesCloseToNodeVisitor.addNodeListener(listener);
  }

  public Collection<Edge<T>> getNewEdges() {
    return splitEdgesCloseToNodeVisitor.getNewEdges();
  }

  public Collection<T> getSplitObjects() {
    return splitEdgesCloseToNodeVisitor.getSplitObjects();
  }

  public void setNewEdges(
    final Collection<Edge<T>> newEdges) {
    splitEdgesCloseToNodeVisitor.setNewEdges(newEdges);
  }

  public void setSplitObjects(
    final Collection<T> splitObjects) {
    splitEdgesCloseToNodeVisitor.setSplitObjects(splitObjects);
  }

  public boolean visit(
    final Edge<T> edge) {
    final EdgeQuadTree<T> edgeIndex = graph.getEdgeIndex();
    final LineString line = edge.getLine();
    final List<Edge<T>> crossings = edgeIndex.queryCrosses(line);
    crossings.remove(edge);

    for (final Edge<T> crossEdge : crossings) {
      if (!crossEdge.isRemoved()) {
        final LineString crossLine = crossEdge.getLine();
        final Coordinates intersection = LineStringUtil.getCrossingIntersection(
          line, crossLine);
        if (intersection != null) {
          graph.getPrecisionModel().makePrecise(intersection);
           final Node<T> node = graph.getNode(intersection);
          splitEdgesCloseToNodeVisitor.visit(node);
        }
      }
    }
    return true;
  }
}
