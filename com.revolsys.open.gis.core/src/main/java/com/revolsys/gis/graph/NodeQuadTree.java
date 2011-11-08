package com.revolsys.gis.graph;

import java.util.Collection;
import java.util.List;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.vividsolutions.jts.geom.Envelope;

public class NodeQuadTree<T> extends AbstractIdObjectQuadTree<Node<T>> {
  private Graph<T> graph;

  public NodeQuadTree(final Graph<T> graph) {
    this.graph = graph;
    final Collection<Integer> ids = graph.getNodeIds();
    add(ids);
  }

  public Node<T> find(final Coordinates coordinates) {
    final Envelope envelope = new BoundingBox(coordinates);
    final List<Node<T>> nodes = query(envelope);
    for (final Node<T> node : nodes) {
      if (node.equals2d(coordinates)) {
        return node;
      }
    }
    return null;
  }

  @Override
  public Envelope getEnvelope(final Node<T> object) {
    final double x = object.getX();
    final double y = object.getY();
    final Envelope envelope = new Envelope(x, x, y, y);
    return envelope;
  }

  @Override
  public int getId(final Node<T> object) {
    return object.getId();
  }

  @Override
  public Node<T> getObject(final Integer id) {
    return this.graph.getNode(id);
  }

  @Override
  public List<Node<T>> getObjects(final List<Integer> ids) {
    return graph.getNodes(ids);
  }
}
