package com.revolsys.gis.graph.visitor;

import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.data.visitor.CreateListVisitor;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.graph.NodeQuadTree;
import com.revolsys.gis.jts.LineStringUtil;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;

public class OnLineNodeVisitor<T> implements Visitor<Node<T>> {
  public static <T> List<Node<T>> getNodes(
    final Graph<T> graph,
    final LineString line,
    final double maxDistance) {
    final CreateListVisitor<Node<T>> results = new CreateListVisitor<Node<T>>();
    final Envelope env = new Envelope(line.getEnvelopeInternal());
    env.expandBy(maxDistance);
    final NodeQuadTree<T> index = graph.getNodeIndex();
    final OnLineNodeVisitor<T> visitor = new OnLineNodeVisitor<T>(
      line, results);
    index.query(env, visitor);
    return results.getList();
  }

  private final LineString line;

  private final Visitor<Node<T>> matchVisitor;

  
  public OnLineNodeVisitor(
    final LineString line,
    final Visitor<Node<T>> matchVisitor) {
    this.line = line;
    this.matchVisitor = matchVisitor;
   }

  public boolean visit(
    final Node<T> node) {
    final Coordinates point = node;
    if (LineStringUtil.isPointOnLine(line, point)) {
      matchVisitor.visit(node);
    }
    return true;
  }

}
