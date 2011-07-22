package com.revolsys.gis.graph.visitor;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;

public class DeleteEdgeVisitor<T> implements Visitor<Edge<T>> {

  public boolean visit(
    Edge<T> edge) {
    final Graph<T> graph = edge.getGraph();
    graph.remove(edge);
    return true;
  }

}
