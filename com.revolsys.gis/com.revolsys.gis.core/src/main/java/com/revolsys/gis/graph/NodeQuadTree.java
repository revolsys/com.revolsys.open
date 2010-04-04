package com.revolsys.gis.graph;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.revolsys.gis.data.visitor.Visitor;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.ItemVisitor;
import com.vividsolutions.jts.index.quadtree.Quadtree;

public class NodeQuadTree<T> extends Quadtree implements Iterable<Node<T>> {

  public Node<T> add(
    final Node<T> node) {
    final Coordinate coordinate = node.getCoordinate();
    final Envelope envelope = new Envelope(coordinate);
    insert(envelope, node);
    return node;
  }

  public void addAll(
    final Collection<Node<T>> nodes) {
    for (final Node<T> node : nodes) {
      add(node);
    }
  }

  public Node<T> find(
    final Coordinate coordinate) {
    final Envelope envelope = new Envelope(coordinate);
    final List<Node<T>> nodes = query(envelope);
    for (final Node<T> node : nodes) {
      if (node.getCoordinate().equals2D(coordinate)) {
        return node;
      }
    }
    return null;
  }

  public Iterator<Node<T>> iterator() {
    return queryAll().iterator();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Node<T>> query(
    final Envelope searchEnv) {
    return super.query(searchEnv);
  }

  public void query(
    final Envelope searchEnv,
    final Visitor<Node<T>> visitor) {
    super.query(searchEnv, new ItemVisitor() {
      @SuppressWarnings("unchecked")
      public void visitItem(
        final Object item) {
        visitor.visit((Node<T>)item);
      }
    });
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Node<T>> queryAll() {
    return super.queryAll();
  }

  public Node<T> remove(
    final Coordinate coordinate) {
    final Node<T> node = find(coordinate);
    if (node != null) {
      remove(node);
      return node;
    } else {
      return null;
    }
  }

  public void remove(
    final Node<T> node) {
    final Coordinate coordinate = node.getCoordinate();
    final Envelope envelope = new Envelope(coordinate);
    remove(envelope, node);
  }

  public void removeAll(
    final Collection<Node<T>> nodes) {
    for (final Node<T> node : nodes) {
      remove(node);
    }
  }

}
