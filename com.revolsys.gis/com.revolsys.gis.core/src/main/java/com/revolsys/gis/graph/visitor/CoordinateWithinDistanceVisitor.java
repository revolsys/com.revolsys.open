package com.revolsys.gis.graph.visitor;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.revolsys.gis.data.visitor.CreateListVisitor;
import com.revolsys.gis.data.visitor.Visitor;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.graph.NodeQuadTree;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class CoordinateWithinDistanceVisitor<T> implements Visitor<Node<T>> {
  public static <T> List<Node<T>> find(
    final NodeQuadTree<T> index,
    final Coordinate coordinate,
    final double maxDistance) {
    final CreateListVisitor<Node<T>> visitor = new CreateListVisitor<Node<T>>();
    visit(index, coordinate, maxDistance, visitor);
    final List<Node<T>> list = visitor.getList();
    Collections.sort(list, new Comparator<Node<T>>() {
      public int compare(
        final Node<T> o1,
        final Node<T> o2) {
        final Coordinate c1 = o1.getCoordinate();
        final Coordinate c2 = o2.getCoordinate();
        final double distance1 = c1.distance(coordinate);
        final double distance2 = c2.distance(coordinate);
        return Double.compare(distance1, distance2);
      }
    });
    return list;
  }

  public static <T> void visit(
    final NodeQuadTree<T> index,
    final Coordinate coordinate,
    final double maxDistance,
    final Visitor<Node<T>> matchVisitor) {
    final Visitor<Node<T>> visitor = new CoordinateWithinDistanceVisitor<T>(
      coordinate, maxDistance, matchVisitor);
    final Envelope envelope = new Envelope(coordinate);
    envelope.expandBy(maxDistance);
    index.query(envelope, visitor);
  }

  private final Coordinate coordinate;

  private final Visitor<Node<T>> matchVisitor;

  private final double maxDistance;

  public CoordinateWithinDistanceVisitor(
    final Coordinate coordinate,
    final double maxDistance,
    final Visitor<Node<T>> matchVisitor) {
    this.coordinate = coordinate;
    this.maxDistance = maxDistance;
    this.matchVisitor = matchVisitor;
  }

  public boolean visit(
    final Node<T> node) {
    final Coordinate nodeCoordinate = node.getCoordinate();
    final double distance = nodeCoordinate.distance(coordinate);
    if (distance <= maxDistance) {
      return matchVisitor.visit(node);
    } else {
      return true;
    }
  }

}
