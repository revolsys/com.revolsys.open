package com.revolsys.gis.graph;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.revolsys.gis.data.visitor.Visitor;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;
import com.vividsolutions.jts.index.ItemVisitor;
import com.vividsolutions.jts.index.quadtree.Quadtree;

public class EdgeQuadTree<T> extends Quadtree implements Iterable<Edge<T>> {

  public Edge<T> add(final Edge<T> edge) {
    final LineString line = edge.getLine();
    final Envelope envelope = line.getEnvelopeInternal();
    insert(envelope, edge);
    return edge;
  }

  public void addAll(final Collection<Edge<T>> edges) {
    for (final Edge<T> edge : edges) {
      add(edge);
    }
  }

  public Iterator<Edge<T>> iterator() {
    return queryAll().iterator();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Edge<T>> query(final Envelope searchEnv) {
    List<Edge<T>> edges = super.query(searchEnv);
    for (Iterator<Edge<T>> edgeIter = edges.iterator(); edgeIter.hasNext();) {
      Edge<T> edge = edgeIter.next();
      if (!edge.getEnvelope().intersects(searchEnv)) {
        edgeIter.remove();
      }
    }
    return edges;
  }

  public void query(final Envelope searchEnv, final Visitor<Edge<T>> visitor) {
    super.query(searchEnv, new ItemVisitor() {
      @SuppressWarnings("unchecked")
      public void visitItem(final Object item) {
        visitor.visit((Edge<T>)item);
      }
    });
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Edge<T>> queryAll() {
    return super.queryAll();
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

  public void remove(final Edge<T> edge) {
    final LineString line = edge.getLine();
    final Envelope envelope = line.getEnvelopeInternal();
    remove(envelope, edge);
  }

  public void removeAll(final Collection<Edge<T>> edges) {
    for (final Edge<T> edge : edges) {
      remove(edge);
    }
  }
}
