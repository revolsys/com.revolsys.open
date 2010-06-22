package com.revolsys.gis.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.revolsys.gis.algorithm.linematch.LineSegmentMatch;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.graph.attribute.ObjectAttributeProxy;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class Node<T> implements Comparable<Node<T>> {
  public static List<Coordinates> getCoordinates(
    final List<Node<DataObject>> nodes) {
    final List<Coordinates> coordinates = new ArrayList(nodes.size());
    for (final Node<DataObject> node : nodes) {
      final Coordinates coordinate = node.getCoordinates();
      coordinates.add(coordinate);
    }
    return coordinates;
  }

  public static <V> int getEdgeIndex(
    final List<Edge<V>> edges,
    final Edge<V> edge) {
    return edges.indexOf(edge);
  }

  public static <T> Collection<Edge<T>> getEdgesBetween(
    final Node<T> node0,
    final Node<T> node1) {
    if (node1 == null) {
      return Collections.emptyList();
    }
    final Set<Edge<T>> commonEdges = new HashSet<Edge<T>>();
    if (node0 == node1) {
      for (final Edge<T> edge : node0.getEdges()) {
        if (edge.getFromNode() == edge.getToNode()) {
          commonEdges.add(edge);
        }
      }
    } else {
      final Collection<Edge<T>> edges0 = node0.getEdges();
      commonEdges.addAll(edges0);
      final Collection<Edge<T>> edges1 = node1.getEdges();
      commonEdges.retainAll(edges1);
    }
    return commonEdges;
  }

  public static <V> Edge<V> getNextEdge(
    final List<Edge<V>> edges,
    final Edge<V> edge) {
    final int index = getEdgeIndex(edges, edge);
    final int nextIndex = (index + 1) % edges.size();
    return edges.get(nextIndex);
  }

  private Map<String, Object> attributes = Collections.emptyMap();

  private Coordinates coordinates;

  private List<Edge<T>> edges = new ArrayList<Edge<T>>();

  private final Graph<T> graph;

  private List<Edge<T>> inEdges = new ArrayList<Edge<T>>();

  private List<Edge<T>> outEdges = new ArrayList<Edge<T>>();

  public Node(
    final Graph<T> graph,
    final Coordinates point) {
    this.graph = graph;
    this.coordinates = new DoubleCoordinates(point);
  }

  protected void addInEdge(
    final Edge<T> edge) {
    edges.clear();
    inEdges.add(edge);
    final EdgeToAngleComparator<T> comparator = EdgeToAngleComparator.get();
    Collections.sort(inEdges, comparator);
    updateAttributes();
  }

  protected void addOutEdge(
    final Edge<T> edge) {
    edges.clear();
    outEdges.add(edge);
    final EdgeFromAngleComparator<T> comparator = EdgeFromAngleComparator.get();
    Collections.sort(outEdges, comparator);
    updateAttributes();
  }

  public int compareTo(
    final Node<T> node) {
    final Coordinates otherPoint = node.getCoordinates();
    return coordinates.compareTo(otherPoint);
  }

  public boolean equalsCoordinate(
    final Coordinates otherPoint) {
    return coordinates.equals2d(otherPoint);
  }

  public boolean equalsCoordinate(
    final double x,
    final double y) {
    return coordinates.getX() == x && coordinates.getY() == y;
  }

  public boolean equalsCoordinate(
    final Node<T> node) {
    final Coordinates otherPoint = node.getCoordinates();
    return equalsCoordinate(otherPoint);
  }

  public double getAngle(
    final Node<LineSegmentMatch> node) {
    return getCoordinates().angle2d(node.getCoordinates());
  }

  @SuppressWarnings("unchecked")
  public <D> D getAttribute(
    final String name) {
    Object value = attributes.get(name);
    if (value instanceof ObjectAttributeProxy) {
      final ObjectAttributeProxy proxy = (ObjectAttributeProxy)value;
      value = proxy.getValue(this);
    }
    return (D)value;
  }

  public Map<String, Object> getAttributes() {
    return attributes;
  }


  public Coordinates getCoordinates() {
    return coordinates;
  }

  public int getDegree() {
    return inEdges.size() + outEdges.size();
  }

  /**
   * Get the distance between this node and the point coordinates.
   * 
   * @param point The coordinates.
   * @return The distance.
   */
  public double getDistance(
    final Coordinates point) {
    return this.coordinates.distance(point);
  }

  /**
   * Get the distance between this node and the geometry.
   * 
   * @param geometry The geometry.
   * @return The distance.
   */
  public double getDistance(
    final Geometry geometry) {
    final GeometryFactory factory = GeometryFactory.getFactory(geometry);
    final Point point = factory.createPoint(coordinates);
    return point.distance(geometry);
  }

  /**
   * Get the distance between this node and the node.
   * 
   * @param node The node.
   * @return The distance.
   */
  public double getDistance(
    final Node<?> node) {
    final Coordinates otherPoint = node.getCoordinates();
    return getDistance(otherPoint);
  }

  public int getEdgeIndex(
    final Edge<T> edge) {
    final List<Edge<T>> edges = getEdges();
    return getEdgeIndex(edges, edge);
  }

  public List<Edge<T>> getEdges() {
    if (edges != null && edges.isEmpty()) {
      sortEdges();
    }
    return new ArrayList<Edge<T>>(edges);
  }

  public Collection<Edge<T>> getEdgesTo(
    final Node<T> node) {
    return getEdgesBetween(this, node);
  }

  /**
   * Get all the edges from a node which do not have an attribute with the
   * specified name.
   * 
   * @param node The node to get the edges for.
   * @param attributeName The attribute name.
   * @return The list of edges without the attribute.
   */
  public List<Edge<T>> getEdgesWithoutAttribute(
    final String attributeName) {
    final List<Edge<T>> edges = new ArrayList<Edge<T>>();
    for (final Edge<T> edge : getEdges()) {
      if (edge.getAttribute(attributeName) == null) {
        edges.add(edge);
      }
    }
    return edges;
  }

  public Graph<T> getGraph() {
    return graph;
  }

  public int getInEdgeIndex(
    final Edge<T> edge) {
    return inEdges.indexOf(edge);
  }

  public List<Edge<T>> getInEdges() {
    return inEdges;
  }

  public Edge<T> getNextEdge(
    final Edge<T> edge) {
    final List<Edge<T>> edges = getEdges();
    return getNextEdge(edges, edge);
  }

  public Edge<T> getNextInEdge(
    final Edge<T> edge) {
    final int index = getInEdgeIndex(edge);
    final int nextIndex = (index + 1) % inEdges.size();
    return inEdges.get(nextIndex);
  }

  public Edge<T> getNextOutEdge(
    final Edge<T> edge) {
    final int index = getOutEdgeIndex(edge);
    final int nextIndex = (index + 1) % outEdges.size();
    return outEdges.get(nextIndex);
  }

  public int getOutEdgeIndex(
    final Edge<T> edge) {
    return outEdges.indexOf(edge);
  }

  public List<Edge<T>> getOutEdges() {
    return outEdges;
  }

  public List<Edge<T>> getOutEdgesTo(
    final Node<T> node) {
    final List<Edge<T>> edges = new ArrayList<Edge<T>>();
    for (final Edge<T> edge : outEdges) {
      if (edge.getToNode() == node) {
        edges.add(edge);
      }
    }
    return edges;
  }

  public boolean hasAttribute(
    final String name) {
    return attributes.containsKey(name);
  }

  public boolean hasEdge(
    final Edge<T> edge) {
    return edges.contains(edge);
  }

  public boolean hasEdges() {
    if (isRemoved()) {
      return false;
    } else {
      return !getEdges().isEmpty();
    }
  }

  public boolean isRemoved() {
    return coordinates == null;
  }

  void remove() {
    coordinates = null;
    edges = null;
    inEdges = null;
    outEdges = null;
    attributes = null;
  }

  public void remove(
    final Edge<T> edge) {
    if (!isRemoved()) {
      edges.remove(edge);
      inEdges.remove(edge);
      outEdges.remove(edge);
      if (inEdges.isEmpty() && outEdges.isEmpty()) {
        graph.remove(this);
      } else {
        updateAttributes();
      }
    }
  }

  public void setAttribute(
    final String name,
    final Object value) {
    if (attributes.isEmpty()) {
      attributes = new HashMap<String, Object>();
    }
    attributes.put(name, value);
  }

  private void sortEdges() {
    final Iterator<Edge<T>> inIterator = inEdges.iterator();
    final Iterator<Edge<T>> outIterator = outEdges.iterator();
    if (!inIterator.hasNext()) {
      edges.addAll(outEdges);
    } else if (!outIterator.hasNext()) {
      edges.addAll(inEdges);
    } else {
      Edge<T> inEdge = inIterator.next();
      final double inAngle = inEdge.getToAngle();
      Edge<T> outEdge = outIterator.next();
      final double outAngle = outEdge.getFromAngle();
      do {
        boolean nextIn = false;
        boolean nextOut = false;
        if (inEdge == null) {
          edges.add(outEdge);
          nextOut = true;
        } else if (outEdge == null) {
          edges.add(inEdge);
          nextIn = true;
        } else {
          if (outAngle <= inAngle) {
            edges.add(outEdge);
            nextOut = true;
          } else {
            edges.add(inEdge);
            nextIn = true;
          }
        }
        if (nextOut) {
          if (outIterator.hasNext()) {
            outEdge = outIterator.next();
          } else {
            outEdge = null;
          }
        }
        if (nextIn) {
          if (inIterator.hasNext()) {
            inEdge = inIterator.next();
          } else {
            inEdge = null;
          }
        }
      } while (inEdge != null || outEdge != null);
    }
  }

  @Override
  public String toString() {
    return getCoordinates().toString();
  }

  private void updateAttributes() {
    for (final Object attribute : attributes.values()) {
      if (attribute instanceof ObjectAttributeProxy) {
        final ObjectAttributeProxy proxy = (ObjectAttributeProxy)attribute;
        proxy.clearValue();
      }
    }
  }

}
