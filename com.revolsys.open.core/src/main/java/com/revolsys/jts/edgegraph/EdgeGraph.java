package com.revolsys.jts.edgegraph;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;

/**
 * A graph comprised of {@link HalfEdge}s.
 * It supports tracking the vertices in the graph
 * via edges incident on them, 
 * to allow efficient lookup of edges and vertices.
 * <p>
 * This class may be subclassed to use a 
 * different subclass of HalfEdge,
 * by overriding {@link #createEdge(Coordinate)}.
 * If additional logic is required to initialize
 * edges then {@link EdgeGraph#addEdge(Coordinate, Coordinate)}
 * can be overridden as well.
 * 
 * @author Martin Davis
 *
 */
public class EdgeGraph {
  private final Map vertexMap = new HashMap();

  public EdgeGraph() {
  }

  /**
   * Adds an edge between the coordinates orig and dest
   * to this graph.
   * 
   * @param orig the edge origin location
   * @param dest the edge destination location.
   * @return the created edge
   */
  public HalfEdge addEdge(final Coordinates orig, final Coordinates dest) {
    final int cmp = dest.compareTo(orig);
    // ignore zero-length edges
    if (cmp == 0) {
      return null;
    }

    /**
     * Attempt to find the edge already in the graph.
     * Return it if found.
     * Otherwise, use a found edge with same origin (if any) to construct new edge. 
     */
    final HalfEdge eAdj = (HalfEdge)vertexMap.get(orig);
    HalfEdge eSame = null;
    if (eAdj != null) {
      eSame = eAdj.find(dest);
    }
    if (eSame != null) {
      return eSame;
    }

    final HalfEdge e = insert(orig, dest, eAdj);
    return e;
  }

  private HalfEdge create(final Coordinates p0, final Coordinates p1) {
    final HalfEdge e0 = createEdge(p0);
    final HalfEdge e1 = createEdge(p1);
    HalfEdge.init(e0, e1);
    return e0;
  }

  /**
   * Creates a single HalfEdge.
   * Override to use a different HalfEdge subclass.
   * 
   * @param orig the origin location
   * @return a new HalfEdge with the given origin
   */
  protected HalfEdge createEdge(final Coordinates orig) {
    return new HalfEdge(orig);
  }

  /**
   * Finds an edge in this graph with the given origin
   * and destination, if one exists.
   * 
   * @param orig the origin location
   * @param dest the destination location.
   * @return an edge with the given orig and dest, or null if none exists
   */
  public HalfEdge findEdge(final Coordinates orig, final Coordinates dest) {
    final HalfEdge e = (HalfEdge)vertexMap.get(orig);
    if (e == null) {
      return null;
    }
    return e.find(dest);
  }

  public Collection getVertexEdges() {
    return vertexMap.values();
  }

  /**
   * Inserts an edge not already present into the graph.
   * 
   * @param orig the edge origin location
   * @param dest the edge destination location
   * @param eAdj an existing edge with same orig (if any)
   * @return the created edge
   */
  private HalfEdge insert(final Coordinates orig, final Coordinates dest,
    final HalfEdge eAdj) {
    // edge does not exist, so create it and insert in graph
    final HalfEdge e = create(orig, dest);
    if (eAdj != null) {
      eAdj.insert(e);
    } else {
      // add halfedges to to map
      vertexMap.put(orig, e);
    }

    final HalfEdge eAdjDest = (HalfEdge)vertexMap.get(dest);
    if (eAdjDest != null) {
      eAdjDest.insert(e.sym());
    } else {
      vertexMap.put(dest, e.sym());
    }
    return e;
  }
}
