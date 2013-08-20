package com.revolsys.gis.model.geometry.operation.geomgraph;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.vividsolutions.jts.geom.Location;

/**
 * A map of nodes, indexed by the coordinate of the node
 * 
 * @version 1.7
 */
public class NodeMap

{
  // Map nodeMap = new HashMap();
  Map nodeMap = new TreeMap();

  NodeFactory nodeFact;

  public NodeMap(final NodeFactory nodeFact) {
    this.nodeFact = nodeFact;
  }

  /**
   * Adds a node for the start point of this EdgeEnd (if one does not already
   * exist in this map). Adds the EdgeEnd to the (possibly new) node.
   */
  public void add(final EdgeEnd e) {
    final Coordinates p = e.getCoordinate();
    final Node n = addNode(p);
    n.add(e);
  }

  /**
   * Factory function - subclasses can override to create their own types of
   * nodes
   */
  /*
   * protected Node createNode(Coordinates coord) { return new Node(coord); }
   */
  /**
   * This method expects that a node has a coordinate value.
   */
  public Node addNode(final Coordinates coord) {
    Node node = (Node)nodeMap.get(coord);
    if (node == null) {
      node = nodeFact.createNode(coord);
      nodeMap.put(coord, node);
    }
    return node;
  }

  public Node addNode(final Node n) {
    final Node node = (Node)nodeMap.get(n.getCoordinate());
    if (node == null) {
      nodeMap.put(n.getCoordinate(), n);
      return n;
    }
    node.mergeLabel(n);
    return node;
  }

  /**
   * @return the node if found; null otherwise
   */
  public Node find(final Coordinates coord) {
    return (Node)nodeMap.get(coord);
  }

  public Collection getBoundaryNodes(final int geomIndex) {
    final Collection bdyNodes = new ArrayList();
    for (final Iterator i = iterator(); i.hasNext();) {
      final Node node = (Node)i.next();
      if (node.getLabel().getLocation(geomIndex) == Location.BOUNDARY) {
        bdyNodes.add(node);
      }
    }
    return bdyNodes;
  }

  public Iterator<Node> iterator() {
    return values().iterator();
  }

  public void print(final PrintStream out) {
    for (final Iterator it = iterator(); it.hasNext();) {
      final Node n = (Node)it.next();
      n.print(out);
    }
  }

  public Collection<Node> values() {
    return nodeMap.values();
  }
}
