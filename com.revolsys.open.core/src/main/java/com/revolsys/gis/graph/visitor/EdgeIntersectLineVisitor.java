package com.revolsys.gis.graph.visitor;

import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.algorithm.index.IdObjectIndex;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Dimension;
import com.revolsys.jts.geom.IntersectionMatrix;
import com.revolsys.jts.geom.LineString;
import com.revolsys.visitor.CreateListVisitor;

public class EdgeIntersectLineVisitor<T> implements Visitor<Edge<T>> {

  public static <T> List<Edge<T>> getEdges(final Graph<T> graph,
    final LineString line) {
    final CreateListVisitor<Edge<T>> results = new CreateListVisitor<Edge<T>>();
    final BoundingBox env = line.getBoundingBox();
    final IdObjectIndex<Edge<T>> index = graph.getEdgeIndex();
    index.visit(env, new EdgeIntersectLineVisitor<T>(line, results));
    return results.getList();

  }

  private final LineString line;

  private final Visitor<Edge<T>> matchVisitor;

  public EdgeIntersectLineVisitor(final LineString line,
    final Visitor<Edge<T>> matchVisitor) {
    this.line = line;
    this.matchVisitor = matchVisitor;
  }

  @Override
  public boolean visit(final Edge<T> edge) {
    final LineString line = edge.getLine();
    final IntersectionMatrix relate = this.line.relate(line);
    if (relate.get(0, 0) == Dimension.L) {
      matchVisitor.visit(edge);
    }
    return true;
  }

}
