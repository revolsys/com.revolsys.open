package com.revolsys.gis.graph;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.revolsys.filter.Filter;
import com.revolsys.gis.algorithm.linematch.LineSegmentMatch;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.jts.LineStringUtil;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;

public class Edge<T> implements AttributedObject, Comparable<Edge<T>>,
  Externalizable {

  public static <T> void addEdgeToEdgesByLine(
    final Map<LineString, Set<Edge<T>>> lineEdgeMap, final Edge<T> edge) {
    final LineString line = edge.getLine();
    for (final Entry<LineString, Set<Edge<T>>> entry : lineEdgeMap.entrySet()) {
      final LineString keyLine = entry.getKey();
      if (LineStringUtil.equalsIgnoreDirection2d(line, keyLine)) {
        final Set<Edge<T>> edges = entry.getValue();
        edges.add(edge);
        return;
      }
    }
    final HashSet<Edge<T>> edges = new HashSet<Edge<T>>();
    edges.add(edge);
    lineEdgeMap.put(line, edges);
  }

  public static <T> void addEdgeToEdgesByLine(final Node<T> node,
    final Map<LineString, Set<Edge<T>>> lineEdgeMap, final Edge<T> edge) {
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

  public static <T> Set<Edge<T>> getEdges(final Collection<Edge<T>> edges,
    final LineString line) {
    final Set<Edge<T>> newEdges = new LinkedHashSet<Edge<T>>();
    for (final Edge<T> edge : edges) {
      if (LineStringUtil.equalsIgnoreDirection2d(line, edge.getLine())) {
        newEdges.add(edge);
      }
    }
    return newEdges;
  }

  public static <T> List<Edge<T>> getEdges(final List<Edge<T>> edges,
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
    final Map<LineString, Set<Edge<T>>> lineEdgeMap, final LineString line) {
    for (final Entry<LineString, Set<Edge<T>>> entry : lineEdgeMap.entrySet()) {
      final LineString keyLine = entry.getKey();
      if (LineStringUtil.equalsIgnoreDirection2d(line, keyLine)) {
        final Set<Edge<T>> edges = entry.getValue();
        return edges;
      }
    }
    return null;
  }

  public static <T> Map<LineString, Set<Edge<T>>> getEdgesByLine(
    final List<Edge<T>> edges) {
    final Map<LineString, Set<Edge<T>>> edgesByLine = new HashMap<LineString, Set<Edge<T>>>();
    for (final Edge<T> edge : edges) {
      addEdgeToEdgesByLine(edgesByLine, edge);
    }
    return edgesByLine;
  }

  public static <T> Map<LineString, Set<Edge<T>>> getEdgesByLine(
    final Node<T> node, final List<Edge<T>> edges) {
    final Map<LineString, Set<Edge<T>>> edgesByLine = new HashMap<LineString, Set<Edge<T>>>();
    for (final Edge<T> edge : edges) {
      addEdgeToEdgesByLine(node, edgesByLine, edge);
    }
    return edgesByLine;
  }

  public static <T> List<Edge<T>> getEdgesMatchingObjectFilter(
    final List<Edge<T>> edges, final Filter<T> filter) {
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
  public static <T> List<T> getObjects(final Collection<Edge<T>> edges) {
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
  public static <T> Map<String, List<Edge<T>>> getTypeNameEdgesMap(
    final List<Edge<T>> edges) {
    final Map<String, List<Edge<T>>> edgesByTypeName = new HashMap<String, List<Edge<T>>>();
    for (final Edge<T> edge : edges) {
      final String typePath = edge.getTypeName();
      List<Edge<T>> typeEdges = edgesByTypeName.get(typePath);
      if (typeEdges == null) {
        typeEdges = new ArrayList<Edge<T>>();
        edgesByTypeName.put(typePath, typeEdges);
      }
      typeEdges.add(edge);
    }
    return edgesByTypeName;
  }

  public static <T> boolean hasEdgeMatchingObjectFilter(
    final List<Edge<T>> edges, final Filter<T> filter) {
    for (final Edge<T> edge : edges) {
      final T object = edge.getObject();
      if (filter.accept(object)) {
        return true;
      }
    }
    return false;
  }

  public static <T> void remove(final Collection<Edge<T>> edges) {
    for (final Edge<T> edge : edges) {
      edge.remove();
    }
  }

  public static <T> void setEdgesAttribute(final List<Edge<T>> edges,
    final String attributeName, final Object value) {
    for (final Edge<T> edge : edges) {
      edge.setAttribute(attributeName, value);
    }
  }

  private int fromNodeId;

  /** The graph the edge is part of. */
  private Graph<T> graph;

  private int id;

  private int toNodeId;

  public Edge() {
  }

  public Edge(final int id, final Graph<T> graph, final Node<T> fromNode,
    final Node<T> toNode) {
    this.id = id;
    this.graph = graph;
    this.fromNodeId = fromNode.getId();
    this.toNodeId = toNode.getId();
    fromNode.addOutEdge(this);
    toNode.addInEdge(this);
  }

  @Override
  public int compareTo(final Edge<T> edge) {
    if (this == edge) {
      return 0;
    } else if (isRemoved()) {
      return 1;
    } else if (edge.isRemoved()) {
      return -1;
    } else {
      final Node<T> otherFromNode = edge.getFromNode();
      final Node<T> fromNode = getFromNode();
      final int fromCompare = fromNode.compareTo(otherFromNode);
      if (fromCompare == 0) {
        final Node<T> otherToNode = edge.getToNode();
        final Node<T> toNode = getToNode();
        final int toCompare = toNode.compareTo(otherToNode);
        if (toCompare == 0) {
          final double otherLength = edge.getLength();
          final double length = getLength();
          final int lengthCompare = Double.compare(length, otherLength);
          if (lengthCompare == 0) {
            final String name = toSuperString();
            final String otherName = edge.toSuperString();
            final int nameCompare = name.compareTo(otherName);
            if (nameCompare == 0) {
              return ((Integer)id).compareTo(edge.id);
            } else {
              return nameCompare;
            }
          }
          return lengthCompare;
        } else {
          return toCompare;
        }
      } else {
        return fromCompare;
      }
    }
  }

  public double distance(final Coordinates point) {
    return LineStringUtil.distance(point, getLine());
  }

  public double distance(final Edge<LineSegmentMatch> edge) {
    return getLine().distance(edge.getLine());
  }

  public double distance(final Node<T> node) {
    final Coordinates point = node;
    return distance(point);
  }

  @Override
  protected void finalize() throws Throwable {
    if (graph != null) {
      graph.evict(this);
    }
    super.finalize();
  }

  public double getAngle(final Node<T> node) {
    if (node.getGraph() == graph) {
      final int nodeId = node.getId();
      if (nodeId == fromNodeId) {
        return getFromAngle();
      } else if (nodeId == toNodeId) {
        return getToAngle();

      }
    }
    return Double.NaN;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V> V getAttribute(final String name) {
    if (graph == null) {
      return null;
    } else {
      return (V)graph.getEdgeAttribute(id, name);
    }
  }

  @Override
  public Map<String, Object> getAttributes() {
    return graph.getEdgeAttributes(id);
  }

  public BoundingBox getBoundingBox() {
    return BoundingBox.getBoundingBox(getLine());
  }

  public Collection<Node<T>> getCommonNodes(final Edge<T> edge) {
    final Collection<Node<T>> nodes1 = getNodes();
    final Collection<Node<T>> nodes2 = edge.getNodes();
    nodes1.retainAll(nodes2);
    return nodes1;
  }

  public List<Edge<T>> getEdgesToNextJunctionNode(final Node<T> node) {
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
    return getLine().getEnvelopeInternal();
  }

  public double getFromAngle() {
    final LineString line = getLine();
    final CoordinatesList points = CoordinatesListUtil.get(line);
    return CoordinatesListUtil.angleToNext(points, 0);
  }

  public Node<T> getFromNode() {
    return graph.getNode(fromNodeId);
  }

  public Graph<T> getGraph() {
    return graph;
  }

  public int getId() {
    return id;
  }

  public double getLength() {
    final LineString line = getLine();
    return line.getLength();
  }

  public LineString getLine() {
    return graph.getEdgeLine(id);
  }

  public Node<T> getNextJunctionNode(final Node<T> node) {
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
    nodes.add(getFromNode());
    nodes.add(getToNode());
    return nodes;
  }

  public T getObject() {
    return graph.getEdgeObject(id);
  }

  public Node<T> getOppositeNode(final Node<T> node) {
    if (node.getGraph() == node.getGraph()) {
      final int nodeId = node.getId();
      if (fromNodeId == nodeId) {
        return getToNode();
      } else if (toNodeId == nodeId) {
        return getFromNode();
      }
    }
    return null;
  }

  public double getToAngle() {
    final LineString line = getLine();
    if (line == null) {
      return Double.NaN;
    } else {
      final CoordinatesList points = CoordinatesListUtil.get(line);
      return CoordinatesListUtil.angleToPrevious(points, points.size() - 1);
    }
  }

  public Node<T> getToNode() {
    return graph.getNode(toNodeId);
  }

  public String getTypeName() {
    return graph.getTypeName(this);
  }

  @Override
  public int hashCode() {
    return id;
  }

  public boolean hasNode(final Node<T> node) {
    if (node.getGraph() == graph) {
      final int nodeId = node.getId();
      if (fromNodeId == nodeId) {
        return true;
      } else if (toNodeId == nodeId) {
        return true;
      }
    }
    return false;

  }

  /**
   * Get the direction of the edge from the specified node. If the node is at
   * the start of the edge then return true. If the node is at the end of the
   * edge return false. Otherwise an exception is thrown.
   * 
   * @param node The node to test the direction from.
   * @return True if the node is at the start of the edge.
   */
  public boolean isForwards(final Node<T> node) {
    if (node.getGraph() == graph) {
      final int nodeId = node.getId();
      if (fromNodeId == nodeId) {
        return true;
      } else if (toNodeId == nodeId) {
        return false;
      }
    }
    throw new IllegalArgumentException("Node " + node
      + " is not part of the edge.");
  }

  public boolean isLessThanDistance(final Coordinates point,
    final double distance) {
    return LineStringUtil.distance(point, getLine(), distance) < distance;
  }

  public boolean isLessThanDistance(final Node<T> node, final double distance) {
    final Coordinates point = node;
    return isLessThanDistance(point, distance);
  }

  public boolean isRemoved() {
    return graph == null;
  }

  public boolean isWithinDistance(final Coordinates point, final double distance) {
    return LineStringUtil.distance(point, getLine(), distance) <= distance;
  }

  public boolean isWithinDistance(final Node<T> node, final double distance) {
    final Coordinates point = node;
    return isWithinDistance(point, distance);
  }

  @Override
  public void readExternal(final ObjectInput in) throws IOException,
    ClassNotFoundException {
    final int graphId = in.readInt();
    graph = Graph.getGraph(graphId);
    id = in.readInt();
    fromNodeId = in.readInt();
    toNodeId = in.readInt();
  }

  public void remove() {
    if (graph != null) {
      graph.remove(this);
    }
  }

  void removeInternal() {
    final Node<T> fromNode = graph.getNode(fromNodeId);
    if (fromNode != null) {
      fromNode.remove(this);
    }
    final Node<T> toNode = graph.getNode(toNodeId);
    if (toNode != null) {
      toNode.remove(this);
    }
    graph = null;
  }

  public List<Edge<T>> replace(final LineString... lines) {
    return replace(Arrays.asList(lines));
  }

  public List<Edge<T>> replace(final List<LineString> lines) {
    if (isRemoved()) {
      return Collections.emptyList();
    } else {
      final Graph<T> graph = getGraph();
      return graph.replaceEdge(this, lines);
    }
  }

  @Override
  public void setAttribute(final String name, final Object value) {
    graph.setEdgeAttribute(id, name, value);
  }

  public void setAttributes(final Map<String, Object> attributes) {
    graph.setEdgeAttributes(id, attributes);
  }

  public <V extends Coordinates> List<Edge<T>> split(
    final Collection<V> splitPoints) {
    return graph.splitEdge(this, splitPoints);
  }

  public <V extends Coordinates> List<Edge<T>> split(
    final Collection<V> points, final double maxDistance) {
    return graph.splitEdge(this, points, maxDistance);
  }

  public List<Edge<T>> split(final Coordinates... points) {
    return split(Arrays.asList(points));
  }

  public List<Edge<T>> split(final List<Coordinates> points) {
    final Graph<T> graph = getGraph();
    return graph.splitEdge(this, points);

  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer();
    sb.append(' ');
    if (isRemoved()) {
      return "Removed Edge";
    } else {
      sb.append(getTypeName().toString());
      sb.append(id);
      sb.append('{');
      sb.append(fromNodeId);
      sb.append(',');
      sb.append(toNodeId);
      sb.append("}\tLINESTRING(");
      final Node<T> fromNode = getFromNode();
      sb.append(fromNode.getX());
      sb.append(" ");
      sb.append(fromNode.getY());
      sb.append(",");
      final Node<T> toNode = getToNode();
      sb.append(toNode.getX());
      sb.append(" ");
      sb.append(toNode.getY());
      sb.append(")");
    }
    return sb.toString();
  }

  private String toSuperString() {
    return super.toString();
  }

  public boolean touches(final Edge<T> edge) {
    final Collection<Node<T>> nodes1 = getCommonNodes(edge);
    return !nodes1.isEmpty();
  }

  @Override
  public void writeExternal(final ObjectOutput out) throws IOException {
    final int graphId = graph.getId();
    out.writeInt(graphId);
    out.writeInt(id);
    out.writeInt(fromNodeId);
    out.writeInt(toNodeId);

  }
}
