package com.revolsys.gis.graph.visitor;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.util.ObjectProcessor;

public class DeleteEdgeVisitor<T> implements Visitor<Edge<T>>,
ObjectProcessor<Graph<T>> {
  @Override
  public void process(final Graph<T> graph) {
    graph.visitEdges(this);
  }

  @Override
  public boolean visit(final Edge<T> edge) {
    final Graph<T> graph = edge.getGraph();
    graph.remove(edge);
    return true;
  }

}
