package com.revolsys.jts.edgegraph;

import java.util.Collection;
import java.util.Iterator;

import com.revolsys.jts.geom.PointList;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineString;

/**
 * Builds an edge graph from geometries containing edges.
 * 
 * @author mdavis
 *
 */
public class EdgeGraphBuilder {
  public static EdgeGraph build(final Collection geoms) {
    final EdgeGraphBuilder builder = new EdgeGraphBuilder();
    builder.add(geoms);
    return builder.getGraph();
  }

  private final EdgeGraph graph = new EdgeGraph();

  public EdgeGraphBuilder() {

  }

  /**
   * Adds the edges in a collection of {@link Geometry}s to the graph. 
   * May be called multiple times.
   * Any dimension of Geometry may be added.
   * 
   * @param geometries the geometries to be added
   */
  public void add(final Collection geometries) {
    for (final Iterator i = geometries.iterator(); i.hasNext();) {
      final Geometry geometry = (Geometry)i.next();
      add(geometry);
    }
  }

  /**
   * Adds the edges of a Geometry to the graph. 
   * May be called multiple times.
   * Any dimension of Geometry may be added; the constituent edges are
   * extracted.
   * 
   * @param geometry geometry to be added
   */
  public void add(final Geometry geometry) {
    for (final LineString line : geometry.getGeometryComponents(LineString.class)) {
      add(line);
    }
  }

  private void add(final LineString lineString) {
    final PointList seq = lineString.getCoordinatesList();
    for (int i = 1; i < seq.size(); i++) {
      graph.addEdge(seq.getCoordinate(i - 1), seq.getCoordinate(i));
    }
  }

  public EdgeGraph getGraph() {
    return graph;
  }

}
