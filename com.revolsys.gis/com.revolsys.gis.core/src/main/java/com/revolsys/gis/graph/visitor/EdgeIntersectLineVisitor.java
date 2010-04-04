package com.revolsys.gis.graph.visitor;

import java.util.List;

import com.revolsys.gis.data.visitor.CreateListVisitor;
import com.revolsys.gis.data.visitor.Visitor;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.EdgeQuadTree;
import com.revolsys.gis.graph.Graph;
import com.vividsolutions.jts.geom.Dimension;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.geom.LineString;

public class EdgeIntersectLineVisitor<T> implements Visitor<Edge<T>> {

  public static <T> List<Edge<T>> getEdges(
    final Graph<T> graph,
    final LineString line) {
    final CreateListVisitor<Edge<T>> results = new CreateListVisitor<Edge<T>>();
    final Envelope env = line.getEnvelopeInternal();
    final EdgeQuadTree<T> index = graph.getEdgeIndex();
    index.query(env, new EdgeIntersectLineVisitor<T>(line, results));
    return results.getList();

  }

  private final LineString line;

  private final Visitor<Edge<T>> matchVisitor;

  public EdgeIntersectLineVisitor(
    final LineString line,
    final Visitor<Edge<T>> matchVisitor) {
    this.line = line;
    this.matchVisitor = matchVisitor;
  }

  public boolean visit(
    final Edge<T> edge) {
    final LineString line = edge.getLine();
    final IntersectionMatrix relate = this.line.relate(line);
    if (relate.get(0, 0) == Dimension.L) {
      matchVisitor.visit(edge);
    }
    return true;
  }

}
