package com.revolsys.gis.graph;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;

public class EdgeQuadTree<T> extends AbstractIdObjectQuadTree<Edge<T>> {
  private final Graph<T> graph;

  public EdgeQuadTree(final Graph<T> graph) {
    this.graph = graph;
    final Collection<Integer> ids = graph.getEdgeIds();
    add(ids);
  }

  public Envelope getEnvelope(final Edge<T> edge) {
    final LineString line = edge.getLine();
    final Envelope envelope = line.getEnvelopeInternal();
    return envelope;
  }

  public int getId(final Edge<T> edge) {
    return edge.getId();
  }

  public Edge<T> getObject(final Integer id) {
    return graph.getEdge(id);
  }

  public List<Edge<T>> getObjects(final List<Integer> ids) {
    return graph.getEdges(ids);
  }

  public List<Edge<T>> queryCrosses(final LineString line) {
    final PreparedGeometry preparedLine = PreparedGeometryFactory.prepare(line);
    final Envelope envelope = line.getEnvelopeInternal();
    final List<Edge<T>> edges = query(envelope);
    // TODO change to use an visitor
    for (final Iterator<Edge<T>> iterator = edges.iterator(); iterator.hasNext();) {
      final Edge<T> edge = iterator.next();
      final LineString matchLine = edge.getLine();
      if (!preparedLine.crosses(matchLine)) {
        iterator.remove();
      }
    }
    return edges;
  }
}
