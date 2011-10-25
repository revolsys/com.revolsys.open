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

import javax.xml.namespace.QName;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.graph.attribute.NodeAttributes;
import com.revolsys.gis.graph.attribute.ObjectAttributeProxy;
import com.revolsys.gis.model.coordinates.AbstractCoordinates;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class Node<T> extends AbstractCoordinates {
  public static List<Coordinates> getCoordinates(
    final Collection<Node<DataObject>> nodes) {
    final List<Coordinates> points = new ArrayList<Coordinates>(nodes.size());
    for (final Node<DataObject> node : nodes) {
      final Coordinates point = node.clone();
      points.add(point);
    }
    return points;
  }

  public static <V> int getEdgeIndex(final List<Edge<V>> edges,
    final Edge<V> edge) {
    return edges.indexOf(edge);
  }

  public static <T> Collection<Edge<T>> getEdgesBetween(final Node<T> node0,
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

  public static <T> Collection<Edge<T>> getEdgesBetween(final QName typeName,
    final Node<T> node0, final Node<T> node1) {
    final Collection<Edge<T>> edges = getEdgesBetween(node0, node1);
    for (final Iterator<Edge<T>> edgeIter = edges.iterator(); edgeIter.hasNext();) {
      final Edge<T> edge = edgeIter.next();
      if (!edge.getTypeName().equals(typeName)) {
        edgeIter.remove();
      }
    }
    return edges;
  }

  public static <V> Edge<V> getNextEdge(final List<Edge<V>> edges,
    final Edge<V> edge) {
    final int index = getEdgeIndex(edges, edge);
    final int nextIndex = (index + 1) % edges.size();
    return edges.get(nextIndex);
  }

  public static <T> boolean hasEdgesBetween(final QName typeName,
    final Node<T> node0, final Node<T> node1) {
    if (node1 == null) {
      return false;
    }
    if (node0 == node1) {
      for (final Edge<T> edge : node0.getEdges()) {
        if (edge.getTypeName().equals(typeName)) {
          if (edge.getFromNode() == edge.getToNode()) {
            return true;
          }
        }
      }
    } else {
      for (final Edge<T> edge : node0.getEdges()) {
        if (edge.getTypeName().equals(typeName)) {
          if (edge.hasNode(node1)) {
            return true;
          }
        }
      }
      for (final Edge<T> edge : node1.getEdges()) {
        if (edge.getTypeName().equals(typeName)) {
          if (edge.hasNode(node0)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private Map<String, Object> attributes = Collections.emptyMap();

  private List<Edge<T>> edges = new ArrayList<Edge<T>>();

  private final Graph<T> graph;

  private List<Edge<T>> inEdges = new ArrayList<Edge<T>>();

  private List<Edge<T>> outEdges = new ArrayList<Edge<T>>();

  private final int id;

  private double x;

  private double y;

  protected Node(final int nodeId, final Graph<T> graph, final Coordinates point) {
    this.id = nodeId;
    this.graph = graph;
    this.x = point.getX();
    this.y = point.getY();
  }

  protected void addInEdge(final Edge<T> edge) {
    edges.clear();
    inEdges.add(edge);
    final EdgeToAngleComparator<T> comparator = EdgeToAngleComparator.get();
    Collections.sort(inEdges, comparator);
    updateAttributes();
  }

  protected void addOutEdge(final Edge<T> edge) {
    edges.clear();
    outEdges.add(edge);
    final EdgeFromAngleComparator<T> comparator = EdgeFromAngleComparator.get();
    Collections.sort(outEdges, comparator);
    updateAttributes();
  }

  @Override
  public Coordinates clone() {
    return new DoubleCoordinates(x, y);
  }

  public int compareTo(final Node<T> node) {
    return compareTo((Coordinates)node);
  }

  public boolean equalsCoordinate(final double x, final double y) {
    return this.x == x && this.y == y;
  }

  public Coordinates get3dCoordinates(final QName typeName) {
    if (!isRemoved()) {

      final List<Edge<T>> edges = NodeAttributes.getEdgesByType(this, typeName);
      if (!edges.isEmpty()) {
        Coordinates coordinates = null;
        for (final Edge<T> edge : edges) {
          final LineString line = edge.getLine();
          final CoordinatesList points = CoordinatesListUtil.get(line);
          Coordinates point = null;
          if (edge.getFromNode() == this) {
            point = points.get(0);
          } else if (edge.getToNode() == this) {
            point = points.get(points.size() - 1);
          }
          if (point != null) {
            final double z = point.getZ();
            if (z == 0 || Double.isNaN(z)) {
              coordinates = point;
            } else {
              return point;
            }
          }
        }
        return coordinates;
      }
    }
    return this;

  }

  @SuppressWarnings("unchecked")
  public <D> D getAttribute(final String name) {
    Object value = attributes.get(name);
    if (value instanceof ObjectAttributeProxy) {
      final ObjectAttributeProxy<D,Node<T>> proxy = (ObjectAttributeProxy<D,Node<T>>)value;
      value = proxy.getValue(this);
    }
    return (D)value;
  }

  public Map<String, Object> getAttributes() {
    return attributes;
  }

  public int getDegree() {
    return inEdges.size() + outEdges.size();
  }

  /**
   * Get the distance between this node and the geometry.
   * 
   * @param geometry The geometry.
   * @return The distance.
   */
  public double getDistance(final Geometry geometry) {
    final GeometryFactory factory = GeometryFactory.getFactory(geometry);
    final Point point = factory.createPoint(this);
    return point.distance(geometry);
  }

  public int getEdgeIndex(final Edge<T> edge) {
    final List<Edge<T>> edges = getEdges();
    return getEdgeIndex(edges, edge);
  }

  public List<Edge<T>> getEdges() {
    if (edges != null && edges.isEmpty()) {
      sortEdges();
    }
    return new ArrayList<Edge<T>>(edges);
  }

  public Collection<Edge<T>> getEdgesTo(final Node<T> node) {
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
  public List<Edge<T>> getEdgesWithoutAttribute(final String attributeName) {
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

  public int getId() {
    return id;
  }

  public int getInEdgeIndex(final Edge<T> edge) {
    return inEdges.indexOf(edge);
  }

  public List<Edge<T>> getInEdges() {
    return inEdges;
  }

  public Edge<T> getNextEdge(final Edge<T> edge) {
    final List<Edge<T>> edges = getEdges();
    return getNextEdge(edges, edge);
  }

  public Edge<T> getNextInEdge(final Edge<T> edge) {
    final int index = getInEdgeIndex(edge);
    final int nextIndex = (index + 1) % inEdges.size();
    return inEdges.get(nextIndex);
  }

  public Edge<T> getNextOutEdge(final Edge<T> edge) {
    final int index = getOutEdgeIndex(edge);
    final int nextIndex = (index + 1) % outEdges.size();
    return outEdges.get(nextIndex);
  }

  public int getOutEdgeIndex(final Edge<T> edge) {
    return outEdges.indexOf(edge);
  }

  public List<Edge<T>> getOutEdges() {
    return outEdges;
  }

  public List<Edge<T>> getOutEdgesTo(final Node<T> node) {
    final List<Edge<T>> edges = new ArrayList<Edge<T>>();
    for (final Edge<T> edge : outEdges) {
      if (edge.getToNode() == node) {
        edges.add(edge);
      }
    }
    return edges;
  }

  public double getValue(final int index) {
    switch (index) {
      case 0:
        return x;
      case 1:
        return y;

      default:
        return Double.NaN;
    }
  }

  public boolean hasAttribute(final String name) {
    return attributes.containsKey(name);
  }

  public boolean hasEdge(final Edge<T> edge) {
    return edges.contains(edge);
  }

  public boolean hasEdges() {
    if (isRemoved()) {
      return false;
    } else {
      return !getEdges().isEmpty();
    }
  }

  public boolean hasEdgeTo(final Node<T> node) {
    if (node == this) {
      return false;
    } else {
      for (final Edge<T> edge : getEdges()) {
        if (edge.hasNode(node)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    return id;
  }

  public boolean isRemoved() {
    return edges == null;
  }

  public boolean move(final Coordinates newCoordinates) {
    final Node<T> newNode = graph.getNode(newCoordinates);
    if (equals(newNode)) {
      return false;
    } else {
      graph.moveNode(this, newNode);
      return true;
    }
  }

  void remove() {
    edges = null;
    inEdges = null;
    outEdges = null;
    attributes = null;
  }

  public void remove(final Edge<T> edge) {
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

  public void setAttribute(final String name, final Object value) {
    if (attributes.isEmpty()) {
      attributes = new HashMap<String, Object>();
    }
    attributes.put(name, value);
  }

  public void setValue(final int index, final double value) {
    switch (index) {
      case 0:
        x = value;
      break;
      case 1:
        y = value;
      break;
    }
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
    if (isRemoved()) {
      return "removed:" + getCoordinates().toString();
    } else {
      return super.toString();
    }
  }

  private void updateAttributes() {
    for (final Object attribute : attributes.values()) {
      if (attribute instanceof ObjectAttributeProxy) {
        @SuppressWarnings("unchecked")
        final ObjectAttributeProxy<Object,Node<T>> proxy = (ObjectAttributeProxy<Object,Node<T>>)attribute;
        proxy.clearValue();
      }
    }
  }

}
