package com.revolsys.gis.graph.visitor;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.revolsys.collection.Visitor;
import com.revolsys.data.equals.EqualsInstance;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.graph.DataObjectGraph;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Node;

public class NodeRemovalVisitor implements Visitor<Node<Record>> {

  /** The relationship between an Edge and it's next edge. */
  private enum EdgeType {
    /** The edge is backwards in relation to the next edge. */
    BACKWARDS,
    /** The edge has degree 1 at the TO node. */
    END_DEGREE_1,
    /** The edge has degree > 2 at the TO node. */
    END_DEGREE_N,
    /** The edge is forwards in relation to the next edge. */
    FORWARDS
  };

  private final Collection<String> excludedAttributes = new HashSet<String>();

  private final DataObjectGraph graph;

  public NodeRemovalVisitor(final RecordDefinition metaData,
    final DataObjectGraph graph, final Collection<String> excludedAttributes) {
    super();
    this.graph = graph;
    if (excludedAttributes != null) {
      this.excludedAttributes.addAll(excludedAttributes);
    }
  }

  /**
   * Check the direction of the edge by checking following the edge to the TO
   * node until the degree of the node != 2 of the next edge is reversed.
   * 
   * @param node
   * @param edge The edge.
   * @return The type of edge.
   */
  private EdgeType checkDirection(final Node<Record> node,
    final Edge<Record> edge) {
    final Node<Record> toNode = edge.getToNode();
    final int degree = toNode.getDegree();
    if (degree == 1) {
      return EdgeType.END_DEGREE_1;
    } else if (degree == 2) {
      final Edge<Record> nextEdge = toNode.getNextEdge(edge);
      if (nextEdge.isForwards(toNode) == edge.isForwards(toNode)) {
        return EdgeType.BACKWARDS;
      } else {
        final EdgeType nextDirection = checkDirection(toNode, nextEdge);
        if (nextDirection == EdgeType.BACKWARDS) {
          return EdgeType.BACKWARDS;
        } else {
          return EdgeType.FORWARDS;
        }
      }
    } else {
      return EdgeType.END_DEGREE_N;
    }

  }

  /**
   * Check to see if one of the two edges can be reversed and add the edge to
   * the list of resersedEdges.
   * 
   * @param node
   * @param reversedEdges The edges that can be reversed.
   * @param edge1 The first edge.
   * @param edge2 The second edge.
   * @return True if one of the edges can be reversed, false otherwise.
   */
  private boolean fixReversedEdges(final Node<Record> node,
    final Set<Edge<Record>> reversedEdges, final Edge<Record> edge1,
    final Edge<Record> edge2) {
    final EdgeType edge1Direction = checkDirection(node, edge1);
    final EdgeType edge2Direction = checkDirection(node, edge2);
    if (edge1Direction == edge2Direction) {
      return false;
    } else if (edge1Direction == EdgeType.BACKWARDS) {
      reversedEdges.add(edge1);
      return true;
    } else if (edge2Direction == EdgeType.BACKWARDS) {
      reversedEdges.add(edge2);
      return true;
    } else if (edge1Direction == EdgeType.END_DEGREE_N
      || edge1Direction == EdgeType.END_DEGREE_N) {
      return false;
    } else {
      return false;
    }
  }

  @Override
  public boolean visit(final Node<Record> node) {
    if (node.getDegree() == 2) {
      final List<Edge<Record>> edges = node.getEdges();
      if (edges.size() == 2) {
        final Edge<Record> edge1 = edges.get(0);
        final Edge<Record> edge2 = edges.get(1);
        if (edge1 != edge2) {
          final Record object1 = edge1.getObject();
          final Record object2 = edge2.getObject();
          if (EqualsInstance.INSTANCE.equals(object1, object2,
            excludedAttributes)) {
            if (edge1.isForwards(node) == edge2.isForwards(node)) {
              // if (!fixReversedEdges(node, reversedEdges, edge1, edge2)) {
              return true;
              // }
            }
            if (edge1.isForwards(node)) {
              graph.merge(node, edge2, edge1);
            } else {
              graph.merge(node, edge1, edge2);
            }
          }
        }
      }
    }
    return true;
  }

}
