package com.revolsys.gis.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import com.revolsys.filter.Filter;
import com.revolsys.gis.algorithm.linematch.LineSegmentMatch;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectUtil;
import com.revolsys.gis.jts.LineStringUtil;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;

public class Edge<T> implements AttributedObject {

  public static <T> void addEdgeToEdgesByLine(
    final Node<T> node,
    final Map<LineString, Set<Edge<T>>> lineEdgeMap,
    final Edge<T> edge) {
    LineString line = edge.getLine();
    for (final Entry<LineString, Set<Edge<T>>> entry : lineEdgeMap.entrySet()) {
      final LineString keyLine = entry.getKey();
      if (LineStringUtil.equalsIgnoreDirection2d(line, keyLine)) {
        final Set<Edge<T>> edges = entry.getValue();
        edges.add(edge);
        return;
      }
    }
    final HashSet<Edge<T>> edges = new HashSet<Edge<T>>();
    if (edge.isForwards(node)) {
      line = LineStringUtil.reverse(line);
    }
    edges.add(edge);
    lineEdgeMap.put(line, edges);
  }

  public static <T> List<Edge<T>> getEdges(
    final List<Edge<T>> edges,
    final Filter<Edge<T>> filter) {
    final List<Edge<T>> filteredEdges = new ArrayList<Edge<T>>();
    for (final Edge<T> edge : edges) {
      if (filter.accept(edge)) {
        filteredEdges.add(edge);
      }
    }
    return filteredEdges;
  }

  public static <T> Set<Edge<T>> getEdges(
    final Map<LineString, Set<Edge<T>>> lineEdgeMap,
    final LineString line) {
    for (final Entry<LineString, Set<Edge<T>>> entry : lineEdgeMap.entrySet()) {
      final LineString keyLine = entry.getKey();
      if (LineStringUtil.equalsIgnoreDirection2d(line, keyLine)) {
        final Set<Edge<T>> edges = entry.getValue();
        return edges;
      }
    }
    return null;
  }

  public static <T> Set<Edge<T>> getEdges(
    final Collection<Edge<T>> edges,
    final LineString line) {
    final Set<Edge<T>> newEdges = new LinkedHashSet<Edge<T>>();
    for (final Edge<T> edge : edges) {
      if (LineStringUtil.equalsIgnoreDirection2d(line, edge.getLine())) {
        newEdges.add(edge);
      }
    }
    return newEdges;
  }

  public static <T> Map<LineString, Set<Edge<T>>> getEdgesByLine(
    final Node<T> node,
    final List<Edge<T>> edges) {
    final Map<LineString, Set<Edge<T>>> edgesByLine = new HashMap<LineString, Set<Edge<T>>>();
    for (final Edge<T> edge : edges) {
      addEdgeToEdgesByLine(node, edgesByLine, edge);
    }
    return edgesByLine;
  }

  public static <T> List<Edge<T>> getEdgesMatchingObjectFilter(
    final List<Edge<T>> edges,
    final Filter<T> filter) {
    final List<Edge<T>> filteredEdges = new ArrayList<Edge<T>>();
    for (final Edge<T> edge : edges) {
      if (!edge.isRemoved()) {
        final T object = edge.getObject();
        if (filter.accept(object)) {
          filteredEdges.add(edge);
        }
      }
    }
    return filteredEdges;
  }

  /**
   * Get the list of objects from the collection of edges.
   * 
   * @param <T> The type of the objects.
   * @param edges The collection of edges.
   * @return The collection of edges.
   */
  public static <T> List<T> getObjects(
    final Collection<Edge<T>> edges) {
    final List<T> objects = new ArrayList<T>();
    for (final Edge<T> edge : edges) {
      final T object = edge.getObject();
      objects.add(object);
    }
    return objects;
  }

  /**
   * Get the map of type name to list of edges.
   * 
   * @param <T> The type of object stored in the edge.
   * @param edges The list of edges.
   * @return The map of type name to list of edges.
   */
  public static <T> Map<QName, List<Edge<T>>> getTypeNameEdgesMap(
    final List<Edge<T>> edges) {
    final Map<QName, List<Edge<T>>> edgesByTypeName = new HashMap<QName, List<Edge<T>>>();
    for (final Edge<T> edge : edges) {
      final QName typeName = edge.getTypeName();
      List<Edge<T>> typeEdges = edgesByTypeName.get(typeName);
      if (typeEdges == null) {
        typeEdges = new ArrayList<Edge<T>>();
        edgesByTypeName.put(typeName, typeEdges);
      }
      typeEdges.add(edge);
    }
    return edgesByTypeName;
  }

  public static <T> boolean hasEdgeMatchingObjectFilter(
    final List<Edge<T>> edges,
    final Filter<T> filter) {
    for (final Edge<T> edge : edges) {
      final T object = edge.getObject();
      if (filter.accept(object)) {
        return true;
      }
    }
    return false;
  }

  public static <T> void setEdgesAttribute(
    final List<Edge<T>> edges,
    final String attributeName,
    final Object value) {
    for (final Edge<T> edge : edges) {
      edge.setAttribute(attributeName, value);
    }
  }

  /** additional attributes stored on the edge. */
  private Map<String, Object> attributes = Collections.emptyMap();

  /** The angle of the line at the fromNode. */
  private final double fromAngle;

  /** The node the edge goes from. */
  private Node<T> fromNode;

  /** The graph the edge is part of. */
  private final Graph<T> graph;

  private double length;

  /** The line geometry between the from and to nodes. */
  private LineString line;

  /** The object representing the edge. */
  private T object;

  /** The angle of the line at the toNode. */
  private final double toAngle;

  /** The node the edge goes to. */
  private Node<T> toNode;

  public Edge(
    final Graph<T> graph,
    final T object,
    final LineString line,
    final Node<T> fromNode,
    final double fromAngle,
    final Node<T> toNode,
    final double toAngle) {
    this.graph = graph;
    this.fromNode = fromNode;
    this.fromAngle = fromAngle;
    this.toNode = toNode;
    this.toAngle = toAngle;
    this.object = object;
    this.line = line;
    this.length = line.getLength();
    attributes.clear();
    fromNode.addOutEdge(this);
    toNode.addInEdge(this);
  }

  public double distance(
    final Coordinates point) {
    return LineStringUtil.distance(point, line);
  }

  public double distance(
    final Node<T> node) {
    final Coordinates point = node.getCoordinates();
    return distance(point);
  }

  public double getAngle(
    final Node<T> node) {
    if (node == fromNode) {
      return fromAngle;
    } else {
      return toAngle;
    }
  }

  /*
   * (non-Javadoc)
   * @see com.revolsys.gis.graph.AttributedObject#getAttribute(java.lang.String)
   */
  @SuppressWarnings("unchecked")
  public <A> A getAttribute(
    final String name) {
    return (A)attributes.get(name);
  }

  /*
   * (non-Javadoc)
   * @see com.revolsys.gis.graph.AttributedObject#getAttributes()
   */
  public Map<String, Object> getAttributes() {
    return Collections.unmodifiableMap(attributes);
  }

  public Collection<Node<T>> getCommonNodes(
    final Edge<DataObject> edge) {
    final Collection<Node<T>> nodes1 = getNodes();
    final Collection<Node<DataObject>> nodes2 = edge.getNodes();
    nodes1.retainAll(nodes2);
    return nodes1;
  }

  public List<Edge<T>> getEdgesToNextJunctionNode(
    final Node<T> node) {
    final List<Edge<T>> edges = new ArrayList<Edge<T>>();
    edges.add(this);
    Edge<T> currentEdge = this;
    Node<T> currentNode = getOppositeNode(node);
    while (currentNode.getDegree() == 2) {
      currentEdge = currentNode.getNextEdge(currentEdge);
      final Node<T> nextNode = currentEdge.getOppositeNode(currentNode);
      if (nextNode != currentNode) {
        currentNode = nextNode;
        edges.add(currentEdge);
      } else {
        return edges;
      }
    }
    return edges;
  }

  public Envelope getEnvelope() {
    return line.getEnvelopeInternal();
  }

  public double getFromAngle() {
    return fromAngle;
  }

  public Node<T> getFromNode() {
    return fromNode;
  }

  public Graph<T> getGraph() {
    return graph;
  }

  public double getLength() {
    return length;
  }

  public LineString getLine() {
    return line;
  }

  public Node<T> getNextJunctionNode(
    final Node<T> node) {
    Edge<T> currentEdge = this;
    Node<T> currentNode = getOppositeNode(node);
    while (currentNode.getDegree() == 2) {
      currentEdge = currentNode.getNextEdge(currentEdge);
      final Node<T> nextNode = currentEdge.getOppositeNode(currentNode);
      if (nextNode != currentNode) {
        currentNode = nextNode;
      } else {
        return currentNode;
      }
    }
    return currentNode;
  }

  public Collection<Node<T>> getNodes() {
    final LinkedHashSet<Node<T>> nodes = new LinkedHashSet<Node<T>>();
    nodes.add(fromNode);
    nodes.add(toNode);
    return nodes;
  }

  public T getObject() {
    return object;
  }

  public Node<T> getOppositeNode(
    final Node<T> node) {
    if (fromNode == node) {
      return toNode;
    } else if (toNode == node) {
      return fromNode;
    } else {
      return null;
    }
  }

  public double getToAngle() {
    return toAngle;
  }

  public Node<T> getToNode() {
    return toNode;
  }

  public QName getTypeName() {
    return graph.getTypeName(this);
  }

  public boolean hasNode(
    final Node<T> node) {
    if (fromNode == node) {
      return true;
    } else if (toNode == node) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Get the direction of the edge from the specified node. If the node is at
   * the start of the edge then return true. If the node is at the end of the
   * edge return false. Otherwise an exception is thrown.
   * 
   * @param node The node to test the direction from.
   * @return True if the node is at the start of the edge.
   */
  public boolean isForwards(
    final Node<T> node) {
    if (fromNode == node) {
      return true;
    } else if (toNode == node) {
      return false;
    } else {
      throw new IllegalArgumentException("Node " + node
        + " is not part of the edge.");
    }
  }

  public boolean isLessThanDistance(
    final Coordinates point,
    final double distance) {
    return LineStringUtil.distance(point, line, distance) < distance;
  }

  public boolean isLessThanDistance(
    final Node<T> node,
    final double distance) {
    final Coordinates point = node.getCoordinates();
    return isLessThanDistance(point, distance);
  }

  public boolean isRemoved() {
    return fromNode == null;
  }

  public boolean isWithinDistance(
    final Coordinates point,
    final double distance) {
    return LineStringUtil.distance(point, line, distance) <= distance;
  }

  public boolean isWithinDistance(
    final Node<T> node,
    final double distance) {
    final Coordinates point = node.getCoordinates();
    return isWithinDistance(point, distance);
  }

  void remove() {
    if (fromNode != null) {
      fromNode.remove(this);
    }
    if (toNode != null) {
      toNode.remove(this);
    }
    fromNode = null;
    toNode = null;
    line = null;
    object = null;
    length = 0;
  }

  /*
   * (non-Javadoc)
   * @see com.revolsys.gis.graph.AttributedObject#setAttribute(java.lang.String,
   * java.lang.Object)
   */
  public void setAttribute(
    final String name,
    final Object value) {
    if (attributes.isEmpty()) {
      attributes = new HashMap<String, Object>();
    }
    attributes.put(name, value);
  }

  @Override
  public String toString() {
    if (isRemoved()) {
      return "Removed";
    } else {
      final StringBuffer sb = new StringBuffer(getTypeName().toString());
      sb.append("LINESTRING(");
      sb.append(fromNode.getCoordinates().toString().replaceAll(",", " "));
      sb.append(",");
      sb.append(toNode.getCoordinates().toString().replaceAll(",", " "));
      sb.append(")");
      return sb.toString();
    }
  }

  public boolean touches(
    final Edge<DataObject> edge) {
    final Collection<Node<T>> nodes1 = getCommonNodes(edge);
    return !nodes1.isEmpty();
  }

  public double distance(
    Edge<LineSegmentMatch> edge) {
    return getLine().distance(edge.getLine());
  }
}
