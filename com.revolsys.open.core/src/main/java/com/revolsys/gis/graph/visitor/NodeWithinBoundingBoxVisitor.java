package com.revolsys.gis.graph.visitor;

import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.algorithm.index.IdObjectIndex;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.visitor.CreateListVisitor;

public class NodeWithinBoundingBoxVisitor<T> implements Visitor<Node<T>> {
  public static <T> List<Node<T>> getNodes(final Graph<T> graph,
    final BoundingBox boundingBox) {
    final CreateListVisitor<Node<T>> results = new CreateListVisitor<Node<T>>();
    final IdObjectIndex<Node<T>> index = graph.getNodeIndex();
    final NodeWithinBoundingBoxVisitor<T> visitor = new NodeWithinBoundingBoxVisitor<T>(
        boundingBox, results);
    index.visit(boundingBox, visitor);
    return results.getList();
  }

  private final BoundingBox boundingBox;

  private final Visitor<Node<T>> matchVisitor;

  public NodeWithinBoundingBoxVisitor(final BoundingBox boundingBox,
    final Visitor<Node<T>> matchVisitor) {
    this.boundingBox = boundingBox;
    this.matchVisitor = matchVisitor;
  }

  @Override
  public boolean visit(final Node<T> node) {
    if (this.boundingBox.covers(node)) {
      this.matchVisitor.visit(node);
    }
    return true;
  }

}
