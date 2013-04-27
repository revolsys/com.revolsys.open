package com.revolsys.gis.graph.visitor;

import java.util.Set;

import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.jts.LineStringUtil;
import com.revolsys.util.ObjectProcessor;
import com.revolsys.visitor.AbstractVisitor;
import com.vividsolutions.jts.geom.LineString;

/**
 * Find all edges that share the same line geometry and remove the current edge
 * and matching edges. Can be used to dissolve lines between polygons.
 */
public class RemoveBothDuplicateEdgeVisitor<T> extends AbstractVisitor<Edge<T>>
  implements ObjectProcessor<Graph<T>> {
  @Override
  public void process(final Graph<T> graph) {
    graph.visitEdges(this);
  }

  @Override
  public boolean visit(final Edge<T> edge) {
    final LineString line = edge.getLine();
    final Node<T> fromNode = edge.getFromNode();
    final Node<T> toNode = edge.getToNode();
    final Set<Edge<T>> edges = fromNode.getEdgesTo(toNode);
    edges.remove(edge);
    boolean hasDuplicate = false;
    for (final Edge<T> edge2 : edges) {
      final LineString line2 = edge2.getLine();
      if (LineStringUtil.equalsIgnoreDirection(line, line2, 2)) {
        edge2.remove();
        hasDuplicate = true;
      }
    }
    if (hasDuplicate) {
      edge.remove();
    }
    return true;
  }
}
