package com.revolsys.gis.model.geometry.operation.geomgraph;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.jts.geom.CoordinatesList;

/**
 * A EdgeList is a list of Edges. It supports locating edges that are pointwise
 * equals to a target edge.
 * 
 * @version 1.7
 */
public class EdgeList {
  private final List edges = new ArrayList();

  /**
   * An index of the edges, for fast lookup.
   */
  private final Map ocaMap = new TreeMap();

  public EdgeList() {
  }

  /**
   * Insert an edge unless it is already in the list
   */
  public void add(final Edge e) {
    edges.add(e);
    final OrientedCoordinateArray oca = new OrientedCoordinateArray(
      e.getCoordinates());
    ocaMap.put(oca, e);
  }

  public void addAll(final Collection edgeColl) {
    for (final Iterator i = edgeColl.iterator(); i.hasNext();) {
      add((Edge)i.next());
    }
  }

  /**
   * If the edge e is already in the list, return its index.
   * 
   * @return index, if e is already in the list -1 otherwise
   */
  public int findEdgeIndex(final Edge e) {
    for (int i = 0; i < edges.size(); i++) {
      if (((Edge)edges.get(i)).equals(e)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * If there is an edge equal to e already in the list, return it. Otherwise
   * return null.
   * 
   * @return equal edge, if there is one already in the list null otherwise
   */
  public Edge findEqualEdge(final Edge e) {
    final OrientedCoordinateArray oca = new OrientedCoordinateArray(
      e.getCoordinates());
    // will return null if no edge matches
    final Edge matchEdge = (Edge)ocaMap.get(oca);
    return matchEdge;
  }

  public Edge get(final int i) {
    return (Edge)edges.get(i);
  }

  public List getEdges() {
    return edges;
  }

  public Iterator iterator() {
    return edges.iterator();
  }

  public void print(final PrintStream out) {
    out.print("MULTILINESTRING ( ");
    for (int j = 0; j < edges.size(); j++) {
      final Edge e = (Edge)edges.get(j);
      if (j > 0) {
        out.print(",");
      }
      out.print("(");
      final CoordinatesList pts = e.getCoordinates();
      for (int i = 0; i < pts.size(); i++) {
        if (i > 0) {
          out.print(",");
        }
        out.print(pts.getX(i) + " " + pts.getY(i));
      }
      out.println(")");
    }
    out.print(")  ");
  }

}
