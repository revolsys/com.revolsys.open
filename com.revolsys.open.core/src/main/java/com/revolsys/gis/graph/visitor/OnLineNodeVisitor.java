package com.revolsys.gis.graph.visitor;

import java.util.Collections;
import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.algorithm.index.IdObjectIndex;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.jts.LineStringUtil;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.visitor.CreateListVisitor;

public class OnLineNodeVisitor<T> implements Visitor<Node<T>> {
  public static <T> List<Node<T>> getNodes(final Graph<T> graph,
    final LineString line, final double maxDistance) {
    if (line == null) {
      return Collections.emptyList();
    } else {
      final CreateListVisitor<Node<T>> results = new CreateListVisitor<Node<T>>();
      BoundingBox env = line.getBoundingBox();
      env = env.expand(maxDistance);
      final IdObjectIndex<Node<T>> index = graph.getNodeIndex();
      final OnLineNodeVisitor<T> visitor = new OnLineNodeVisitor<T>(line,
          results);
      index.visit(env, visitor);
      return results.getList();
    }
  }

  private final LineString line;

  private final Visitor<Node<T>> matchVisitor;

  public OnLineNodeVisitor(final LineString line,
    final Visitor<Node<T>> matchVisitor) {
    this.line = line;
    this.matchVisitor = matchVisitor;
  }

  @Override
  public boolean visit(final Node<T> node) {
    final Point point = node;
    if (LineStringUtil.isPointOnLine(this.line, point)) {
      this.matchVisitor.visit(node);
    }
    return true;
  }

}
