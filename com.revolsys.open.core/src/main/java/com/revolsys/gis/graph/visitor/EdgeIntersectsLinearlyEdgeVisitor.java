package com.revolsys.gis.graph.visitor;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.algorithm.index.IdObjectIndex;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Dimension;
import com.revolsys.jts.geom.IntersectionMatrix;
import com.revolsys.jts.geom.LineString;
import com.revolsys.visitor.CreateListVisitor;

public class EdgeIntersectsLinearlyEdgeVisitor<T> implements Consumer<Edge<T>> {

  public static <T> List<Edge<T>> getEdges(final Graph<T> graph, final Edge<T> edge) {
    final CreateListVisitor<Edge<T>> results = new CreateListVisitor<Edge<T>>();
    final LineString line = edge.getLine();
    final BoundingBox env = line.getBoundingBox();
    final IdObjectIndex<Edge<T>> index = graph.getEdgeIndex();
    index.forEach(new EdgeIntersectsLinearlyEdgeVisitor<T>(edge, results), env);
    final List<Edge<T>> edges = results.getList();
    Collections.sort(edges);
    return edges;

  }

  private final Edge<T> edge;

  private final Visitor<Edge<T>> matchVisitor;

  public EdgeIntersectsLinearlyEdgeVisitor(final Edge<T> edge,
    final Visitor<Edge<T>> matchVisitor) {
    this.edge = edge;
    this.matchVisitor = matchVisitor;
  }

  @Override
  public void accept(final Edge<T> edge2) {
    if (edge2 != this.edge) {
      final LineString line1 = this.edge.getLine();
      final LineString line2 = edge2.getLine();
      final BoundingBox envelope1 = line1.getBoundingBox();
      final BoundingBox envelope2 = line2.getBoundingBox();
      if (envelope1.intersects(envelope2)) {
        final IntersectionMatrix relate = line1.relate(line2);
        if (relate.get(0, 0) == Dimension.L) {
          this.matchVisitor.accept(edge2);
        }
      }
    }
  }

}
