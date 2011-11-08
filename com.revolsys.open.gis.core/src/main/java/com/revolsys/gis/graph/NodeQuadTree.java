package com.revolsys.gis.graph;

import java.util.Collection;
import java.util.List;

import com.revolsys.gis.algorithm.index.AbstractIdObjectPointQuadTree;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.vividsolutions.jts.geom.Envelope;

public class NodeQuadTree<T> extends AbstractIdObjectPointQuadTree<Node<T>> {
  private Graph<T> graph;

  public NodeQuadTree(final Graph<T> graph) {
    this.graph = graph;
    final Collection<Integer> ids = graph.getNodeIds();
    add(ids);
  }


  public Envelope getEnvelope(final Node<T> node) {
    final double x = node.getX();
    final double y = node.getY();
    final Envelope envelope = new Envelope(x, x, y, y);
    return envelope;
  }

  public int getId(final Node<T> object) {
    return object.getId();
  }

  public Node<T> getObject(final Integer id) {
    return this.graph.getNode(id);
  }

  public List<Node<T>> getObjects(final List<Integer> ids) {
    return graph.getNodes(ids);
  }

  @Override
  public Coordinates getCoordinates(Node<T> node) {
    return node;
  }
}
