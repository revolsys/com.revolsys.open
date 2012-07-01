package com.revolsys.gis.graph.visitor;

import java.util.List;
import java.util.Set;

import com.revolsys.filter.Filter;
import com.revolsys.gis.data.visitor.AbstractVisitor;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.graph.filter.LineFilter;
import com.revolsys.gis.jts.LineStringUtil;
import com.revolsys.gis.jts.filter.LineEqualIgnoreDirectionFilter;
import com.revolsys.util.ObjectProcessor;
import com.vividsolutions.jts.geom.LineString;

/**
 * Find all edges that share the same line geometry and remove the current edge
 * and matching edges. Can be used to dissolve lines between polygons.
 */
public class RemoveBothDuplicateEdgeVisitor<T> extends AbstractVisitor<Edge<T>>
  implements ObjectProcessor<Graph<T>> {
  @Override
  public void process(Graph<T> graph) {
    graph.visitEdges(this);
  }

  public boolean visit(final Edge<T> edge) {
    final LineString line = edge.getLine();
    Node<T> fromNode = edge.getFromNode();
    Node<T> toNode = edge.getToNode();
    Set<Edge<T>> edges = fromNode.getEdgesTo(toNode);
    edges.remove(edge);
    boolean hasDuplicate = false;
    for (Edge<T> edge2 : edges) {
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
