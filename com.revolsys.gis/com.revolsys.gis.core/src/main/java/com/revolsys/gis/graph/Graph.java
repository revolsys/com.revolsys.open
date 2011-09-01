package com.revolsys.gis.graph;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.namespace.QName;

import com.revolsys.collection.Visitor;
import com.revolsys.comparator.ComparatorProxy;
import com.revolsys.filter.Filter;
import com.revolsys.filter.FilterProxy;
import com.revolsys.filter.FilterUtil;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.visitor.CreateListVisitor;
import com.revolsys.gis.data.visitor.FilterListVisitor;
import com.revolsys.gis.graph.attribute.NodeAttributes;
import com.revolsys.gis.graph.comparator.NodeDistanceComparator;
import com.revolsys.gis.graph.event.EdgeEvent;
import com.revolsys.gis.graph.event.EdgeEventListener;
import com.revolsys.gis.graph.event.EdgeEventListenerList;
import com.revolsys.gis.graph.event.NodeEvent;
import com.revolsys.gis.graph.event.NodeEventListener;
import com.revolsys.gis.graph.event.NodeEventListenerList;
import com.revolsys.gis.graph.filter.IsPointOnLineEdgeFilter;
import com.revolsys.gis.graph.visitor.DeleteEdgeVisitor;
import com.revolsys.gis.graph.visitor.EdgeWithinDistanceVisitor;
import com.revolsys.gis.graph.visitor.NodeWithinDistanceOfCoordinateVisitor;
import com.revolsys.gis.graph.visitor.NodeWithinDistanceOfGeometryVisitor;
import com.revolsys.gis.jts.LineStringUtil;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesDistanceComparator;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.gis.model.coordinates.SimpleCoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

public class Graph<T> {

  private EdgeQuadTree<T> edgeIndex;

  private final EdgeEventListenerList<T> edgeListeners = new EdgeEventListenerList<T>();

  private final Set<Edge<T>> edges = new TreeSet<Edge<T>>();

  private NodeQuadTree<T> nodeIndex;

  private final NodeEventListenerList<T> nodeListeners = new NodeEventListenerList<T>();

  private final Map<Coordinates, Node<T>> nodes = new TreeMap<Coordinates, Node<T>>();

  private CoordinatesPrecisionModel precisionModel = new SimpleCoordinatesPrecisionModel();

  protected void add(final Edge<T> edge) {
    edges.add(edge);
    if (edgeIndex != null) {
      edgeIndex.add(edge);
    }
    edgeListeners.edgeEvent(edge, null, EdgeEvent.EDGE_ADDED, null);
  }

  protected void add(final Node<T> node) {
    nodes.put(node, node);
    if (nodeIndex != null) {
      nodeIndex.add(node);
    }
    nodeListeners.nodeEvent(node, null, null, NodeEvent.NODE_ADDED, null);
  }

  public void add(final NodeEventListener<T> listener) {
    nodeListeners.add(listener);
  }

  public Edge<T> add(final T object, final LineString line) {
    final CoordinatesList points = CoordinatesListUtil.get(line);
    final Coordinates from = points.get(0);

    Coordinates fromDirection = points.get(1);
    int i = 2;
    while (i < points.size() && fromDirection.equals2d(from)) {
      fromDirection = points.get(i);
      i++;
    }
    final Coordinates to = points.get(points.size() - 1);
    i = points.size() - 2;
    Coordinates toDirection = points.get(i);
    while (i > 0 && toDirection.equals2d(to)) {
      toDirection = points.get(i);
      i--;
    }
    final Node<T> fromNode = getNode(from);
    final Node<T> toNode = getNode(to);
    final double fromAngle = from.angle2d(fromDirection);
    final double toAngle = to.angle2d(toDirection);
    final Edge<T> edge = new Edge<T>(this, object, line, fromNode, fromAngle,
      toNode, toAngle);
    add(edge);
    return edge;
  }

  public void addEdgeListener(final EdgeEventListener<T> listener) {
    edgeListeners.add(listener);
  }

  /**
   * Clone the object, setting the line property to the new value.
   * 
   * @param object The object to clone.
   * @param line The line.
   * @return The new object.
   */
  @SuppressWarnings("unchecked")
  protected T clone(final T object, final LineString line) {
    if (object != null) {
      try {
        final Class<? extends Object> clazz = object.getClass();
        final Method method = clazz.getMethod("clone", new Class[0]);
        return (T)method.invoke(object, new Object[0]);
      } catch (final Throwable e) {
        throw new IllegalArgumentException("Cannot clone", e);
      }
    } else {
      return null;
    }
  }

  public boolean contains(final Edge<T> e) {
    return edges.contains(e);
  }

  public void copyEdges(final Collection<Edge<T>> target) {
    target.addAll(this.edges);
  }

  public void copyEdges(final Filter<Edge<T>> filter,
    final Collection<Edge<T>> target) {
    if (filter == null) {
      target.addAll(edges);
    } else {
      FilterUtil.filterCopy(edges, target, filter);
    }
  }

  public void copyNodes(final Collection<Node<T>> target) {
    target.addAll(nodes.values());
  }

  public void copyNodes(final Filter<Node<T>> filter,
    final Collection<Node<T>> target) {
    if (filter == null) {
      copyNodes(target);
    } else {
      FilterUtil.filterCopy(nodes.values(), target, filter);
    }
  }

  public Edge<T> createEdge(final GeometryFactory geometryFactory,
    final T object, final CoordinatesList points) {
    final LineString newLine = geometryFactory.createLineString(points);
    final T newObject = clone(object, newLine);
    final Edge<T> newEdge = add(newObject, newLine);
    return newEdge;
  }

  public void deleteEdges(final Filter<Edge<T>> filter) {
    final DeleteEdgeVisitor<T> visitor = new DeleteEdgeVisitor<T>();
    visitEdges(filter, visitor);
  }

  public Iterable<Edge<T>> edges() {
    return edges;
  }

  public List<T> getObjects() {
    List<T> objects = new ArrayList<T>();
    for (Edge<T> edge : edges) {
      if (!edge.isRemoved()) {
        final T object = edge.getObject();
        objects.add(object);
      }
    }
    return objects;
  }

  public List<Edge<T>> findEdges(final EdgeVisitor<T> visitor) {
    final CreateListVisitor<Edge<T>> results = new CreateListVisitor<Edge<T>>();
    queryEdges(visitor, results);
    final List<Edge<T>> edges = results.getList();
    Collections.sort(edges);
    return edges;
  }

  public List<Edge<T>> findEdges(final Node<T> node, final double distance) {
    return EdgeWithinDistanceVisitor.edgesWithinDistance(this, node, distance);
  }

  /**
   * Find the node by point coordinates returning the node if it exists, null
   * otherwise.
   * 
   * @param point The point coordinates to find the node for.
   * @return The nod or null if not found.
   */
  public Node<T> findNode(final Coordinates point) {
    return nodes.get(point);
  }

  /**
   * Find the nodes <= the distance of the specified point coordinates.
   * 
   * @param point The point coordinates.
   * @param distance The distance.
   * @return The list of nodes.
   */
  public List<Node<T>> findNodes(final Coordinates point, final double distance) {
    final CreateListVisitor<Node<T>> results = new CreateListVisitor<Node<T>>();
    final Visitor<Node<T>> visitor = new NodeWithinDistanceOfCoordinateVisitor<T>(
      point, distance, results);
    final Envelope envelope = new BoundingBox(point);
    envelope.expandBy(distance);
    getNodeIndex().query(envelope, visitor);
    final List<Node<T>> nodes = results.getList();
    Collections.sort(nodes);
    return nodes;
  }

  /**
   * Find all the nodes <= the distance of the edge, that are "on" the line.
   * 
   * @param edge The edge.
   * @param distance The distance.
   * @return The nodes
   * @see IsPointOnLineEdgeFilter
   */
  public List<Node<T>> findNodes(final Edge<T> edge, final double distance) {
    final IsPointOnLineEdgeFilter<T> filter = new IsPointOnLineEdgeFilter<T>(
      edge, distance);

    final Node<T> fromNode = edge.getFromNode();

    final Comparator<Node<T>> comparator = new NodeDistanceComparator<T>(
      fromNode);

    final Envelope envelope = filter.getEnvelope();
    return getNodes(filter, comparator, envelope);

  }

  /**
   * Find the nodes <= the distance of the specified geometry.
   * 
   * @param geometry The geometry.
   * @param distance The distance.
   * @return The list of nodes.
   */
  public List<Node<T>> findNodes(final Geometry geometry, final double distance) {
    final CreateListVisitor<Node<T>> results = new CreateListVisitor<Node<T>>();
    final Visitor<Node<T>> visitor = new NodeWithinDistanceOfGeometryVisitor<T>(
      geometry, distance, results);
    final Envelope envelope = geometry.getEnvelopeInternal();
    envelope.expandBy(distance);
    getNodeIndex().query(envelope, visitor);
    final List<Node<T>> nodes = results.getList();
    Collections.sort(nodes);
    return nodes;
  }

  /**
   * Find all the nodes <= the distance of the node.
   * 
   * @param node The node.
   * @param distance The distance.
   * @return The nodes.
   */
  public List<Node<T>> findNodes(final Node<T> node, final double distance) {
    final Coordinates point = node;
    return findNodes(point, distance);
  }

  public List<Node<T>> findNodesOfDegree(final int degree) {
    final List<Node<T>> nodesFound = new ArrayList<Node<T>>();
    for (final Node<T> node : getNodes()) {
      if (node.getDegree() == degree) {
        nodesFound.add(node);
      }
    }
    return nodesFound;
  }

  public double getClosestDistance(final Node<T> node, final double maxDistance) {
    final List<Node<T>> nodes = findNodes(node, maxDistance);
    double closestDistance = Double.MAX_VALUE;
    for (final Node<T> matchNode : nodes) {
      if (matchNode != node) {
        final double distance = node.distance(matchNode);
        if (distance < closestDistance) {
          closestDistance = distance;
        }
      }
    }
    return closestDistance;
  }

  public EdgeQuadTree<T> getEdgeIndex() {
    if (edgeIndex == null) {
      edgeIndex = new EdgeQuadTree<T>();
      edgeIndex.addAll(edges);
    }
    return edgeIndex;
  }

  public List<Edge<T>> getEdges() {
    final ArrayList<Edge<T>> targetEdges = new ArrayList<Edge<T>>();
    copyEdges(targetEdges);
    return targetEdges;
  }

  public List<Edge<T>> getEdges(final Comparator<Edge<T>> comparator) {
    final List<Edge<T>> targetEdges = getEdges();
    if (comparator != null) {
      Collections.sort(targetEdges, comparator);
    }
    return targetEdges;
  }

  public List<Edge<T>> getEdges(final Edge<T> edge) {
    final Envelope envelope = edge.getEnvelope();
    final EdgeQuadTree<T> edgeIndex = getEdgeIndex();
    return edgeIndex.query(envelope);
  }

  public List<Edge<T>> getEdges(final Filter<Edge<T>> filter) {
    if (filter == null) {
      return getEdges();
    } else {
      final List<Edge<T>> filteredEdges = FilterUtil.filter(edges, filter);
      return filteredEdges;
    }
  }

  public List<Edge<T>> getEdges(final Filter<Edge<T>> filter,
    final Comparator<Edge<T>> comparator) {
    final List<Edge<T>> targetEdges = getEdges(filter);
    if (comparator != null) {
      Collections.sort(targetEdges, comparator);
    }
    return targetEdges;
  }

  public List<Edge<T>> getEdges(final Filter<Edge<T>> filter,
    final Comparator<Edge<T>> comparator, final Envelope envelope) {
    final FilterListVisitor<Edge<T>> results = new FilterListVisitor<Edge<T>>(
      filter);
    final EdgeQuadTree<T> edgeIndex = getEdgeIndex();
    edgeIndex.query(envelope, results);
    final List<Edge<T>> targetEdges = results.getResults();
    if (comparator == null) {
      Collections.sort(targetEdges);
    } else {
      Collections.sort(targetEdges, comparator);
    }
    return targetEdges;

  }

  public List<Edge<T>> getEdges(final Filter<Edge<T>> filter,
    final Comparator<Edge<T>> comparator, final Geometry geometry) {
    final Envelope envelope = geometry.getEnvelopeInternal();
    return getEdges(filter, comparator, envelope);
  }

  public List<Edge<T>> getEdges(final Filter<Edge<T>> filter,
    final Envelope envelope) {
    final FilterListVisitor<Edge<T>> results = new FilterListVisitor<Edge<T>>(
      filter);
    final EdgeQuadTree<T> edgeIndex = getEdgeIndex();
    edgeIndex.query(envelope, results);
    final List<Edge<T>> edges = results.getResults();
    Collections.sort(edges);
    return edges;

  }

  public List<Edge<T>> getEdges(final Filter<Edge<T>> filter,
    final Geometry geometry) {
    final Envelope envelope = geometry.getEnvelopeInternal();
    return getEdges(filter, envelope);
  }

  /**
   * Get the node by point coordinates, creating one if it did not exist.
   * 
   * @param point The point coordinates to get the node for.
   * @return The node.
   */
  public Node<T> getNode(final Coordinates point) {
    Node<T> node = findNode(point);
    if (node == null) {
      node = new Node<T>(this, point);
      add(node);
    }
    return node;
  }

  public NodeQuadTree<T> getNodeIndex() {
    if (nodeIndex == null) {
      nodeIndex = new NodeQuadTree<T>();
      nodeIndex.addAll(nodes.values());
    }
    return nodeIndex;
  }

  public List<Node<T>> getNodes() {
    final ArrayList<Node<T>> targetNodes = new ArrayList<Node<T>>();
    copyNodes(targetNodes);
    return targetNodes;
  }

  public List<Node<T>> getNodes(final Comparator<Node<T>> comparator) {
    final List<Node<T>> targetNodes = getNodes();
    if (comparator != null) {
      Collections.sort(targetNodes, comparator);
    }
    return targetNodes;
  }

  public List<Node<T>> getNodes(final Filter<Node<T>> filter) {
    if (filter == null) {
      return getNodes();
    } else {
      final List<Node<T>> filteredNodes = FilterUtil.filter(getNodes(), filter);
      return filteredNodes;
    }
  }

  public List<Node<T>> getNodes(final Filter<Node<T>> filter,
    final Comparator<Node<T>> comparator) {
    final List<Node<T>> targetNodes = getNodes(filter);
    if (comparator != null) {
      Collections.sort(targetNodes, comparator);
    }
    return targetNodes;
  }

  public List<Node<T>> getNodes(final Filter<Node<T>> filter,
    final Comparator<Node<T>> comparator, final Envelope envelope) {
    final FilterListVisitor<Node<T>> results = new FilterListVisitor<Node<T>>(
      filter);
    final NodeQuadTree<T> nodeIndex = getNodeIndex();
    nodeIndex.query(envelope, results);
    final List<Node<T>> nodes = results.getResults();
    if (comparator == null) {
      Collections.sort(nodes);
    } else {
      Collections.sort(nodes, comparator);
    }
    return nodes;

  }

  public List<Node<T>> getNodes(final Filter<Node<T>> filter,
    final Envelope envelope) {
    return getNodes(filter, null, envelope);

  }

  public CoordinatesPrecisionModel getPrecisionModel() {
    return precisionModel;
  }

  /**
   * Get the type name for the edge.
   * 
   * @param edge The edge.
   * @return The type name.
   */
  public QName getTypeName(final Edge<T> edge) {
    final Object object = edge.getObject();
    if (object == null) {
      return null;
    } else {
      final String className = edge.getClass().getName();
      return new QName(className);
    }
  }

  /**
   * Merge the two edges at the node.
   * 
   * @param node
   * @param edge1
   * @param edge2
   */
  public Edge<T> merge(final Node<T> node, final Edge<T> edge1,
    final Edge<T> edge2) {
    if (edge1 != edge2 && edge1.hasNode(node) && edge2.hasNode(node)) {
      final T object1 = edge1.getObject();
      final LineString line1 = edge1.getLine();
      remove(edge1);

      final LineString line2 = edge2.getLine();
      remove(edge2);

      final LineString newLine = LineStringUtil.merge(line1, line2);

      final T mergedObject = clone(object1, newLine);
      final Edge<T> newEdge = add(mergedObject, newLine);
      return newEdge;
    } else {
      return null;
    }

  }

  /**
   * Merge the two edges into a single edge, removing the old edges and node if
   * required from the graph and adding a new edge to the graph.
   * 
   * @param reversedEdges The list of edges that need to be reversed.
   * @param node The node to remove.
   * @param edge1 The first edge to merge.
   * @param edge2 The second edge to merge.
   * @return The new edge.
   */
  public Edge<T> mergeEdges(final Edge<T> edge1, final Edge<T> edge2) {
    final LineString line1 = edge1.getLine();
    final LineString line2 = edge2.getLine();

    final LineString newLine = LineStringUtil.merge(line1, line2);
    final Edge<T> newEdge = replaceEdge(edge1, newLine);
    remove(edge2);
    return newEdge;
  }

  /**
   * Move the node and all its edges to the new node.
   * 
   * @param node The node to move.
   * @param newNode The new node to move it to
   */
  public void moveNode(final Node<T> node, final Node<T> newNode) {
    if (!node.isRemoved() && !newNode.isRemoved()) {
      if (!node.equals(newNode)) {
        final Coordinates newPoint = newNode;
        final List<Edge<T>> edges = new ArrayList<Edge<T>>(node.getEdges());
        for (final Edge<T> edge : edges) {
          if (!edge.isRemoved()) {
            final LineString line = edge.getLine();
            LineString newLine;
            if (edge.isForwards(node)) {
              newLine = LineStringUtil.subLineString(line, newPoint, 1,
                line.getNumPoints() - 1, null);
            } else {
              newLine = LineStringUtil.subLineString(line, null, 0,
                line.getNumPoints() - 1, newPoint);
            }
            replaceEdge(edge, newLine);
          }
        }
      }
    }
  }

  public void moveNode(final QName typeName, final Node<DataObject> fromNode,
    final Node<DataObject> toNode, Coordinates newPoint) {
    if (!fromNode.isRemoved() && !toNode.isRemoved()) {
      if (!fromNode.equals(toNode)) {
        final List<Edge<DataObject>> edges = NodeAttributes.getEdgesByType(
          fromNode, typeName);

        for (final Edge<DataObject> edge : edges) {
          if (!edge.isRemoved()) {
            final LineString line = edge.getLine();
            LineString newLine;
            if (CoordinatesUtil.get(line).equals(fromNode)) {
              newLine = LineStringUtil.subLineString(line, newPoint, 1,
                line.getNumPoints() - 1, null);
            } else {
              newLine = LineStringUtil.subLineString(line, null, 0,
                line.getNumPoints() - 1, newPoint);
            }
            final Graph<DataObject> graph = edge.getGraph();
            graph.replaceEdge(edge, newLine);
          }
        }
      }
    }
  }

  public boolean moveNodesToMidpoint(final QName typeName,
    final Node<DataObject> node1, final Node<DataObject> node2) {
    Coordinates point1 = node1.get3dCoordinates(typeName);
    Coordinates point2 = node2.get3dCoordinates(typeName);

    final Graph<DataObject> graph = node1.getGraph();
    final Coordinates midPoint = LineSegmentUtil.midPoint(
      new SimpleCoordinatesPrecisionModel(1000, 1), node2, node1);
    Coordinates newPoint = new DoubleCoordinates(3);
    newPoint.setX(midPoint.getX());
    newPoint.setY(midPoint.getY());
    final double z1 = point1.getZ();
    final double z2 = point2.getZ();
    if (z1 == 0 || Double.isNaN(z1)) {
      newPoint.setZ(z2);
    } else if (z2 == 0 || Double.isNaN(z2)) {
      newPoint.setZ(z1);
    }
    final Node<DataObject> newNode = graph.getNode(midPoint);
    if (!Node.hasEdgesBetween(typeName, node1, newNode)
      && !Node.hasEdgesBetween(typeName, node2, newNode)) {
      if (node1.equals2d(newNode)) {
        moveNode(typeName, node2, node1, newPoint);
      } else if (node2.equals2d(newNode)) {
        moveNode(typeName, node1, node2, newPoint);
      } else {
        moveNode(typeName, node1, newNode, newPoint);
        moveNode(typeName, node2, newNode, newPoint);
      }
      return true;
    } else {
      return false;
    }
  }

  public Iterable<Node<T>> nodes() {
    return nodes.values();
  }

  public void queryEdges(final EdgeVisitor<T> visitor) {
    final Envelope env = visitor.getEnvelope();
    final EdgeQuadTree<T> index = getEdgeIndex();
    index.query(env, visitor);
  }

  public void queryEdges(final EdgeVisitor<T> visitor,
    final Visitor<Edge<T>> matchVisitor) {
    visitor.setVisitor(matchVisitor);
    queryEdges(visitor);
  }

  public void remove(final Edge<T> edge) {
    if (!edge.isRemoved()) {
      edgeListeners.edgeEvent(edge, null, EdgeEvent.EDGE_REMOVED, null);
      edges.remove(edge);
      if (edgeIndex != null) {
        edgeIndex.remove(edge);
      }
      edge.remove(this);
    }
  }

  public void remove(final EdgeEventListener<T> listener) {
    edgeListeners.remove(listener);
  }

  public void remove(final Node<T> node) {
    if (!node.isRemoved()) {
      nodeListeners.nodeEvent(node, null, null, NodeEvent.NODE_REMOVED, null);
      nodes.remove(node);
      if (nodeIndex != null) {
        nodeIndex.remove(node);
      }
      final ArrayList<Edge<T>> edges = new ArrayList<Edge<T>>(node.getEdges());
      node.remove();
      for (final Edge<T> edge : edges) {
        remove(edge);
      }
    }
  }

  public void remove(final NodeEventListener<T> listener) {
    nodeListeners.remove(listener);
  }

  public List<Edge<T>> replaceEdge(final Edge<T> edge, final Geometry lines) {
    if (!edge.isRemoved()) {
      final List<Edge<T>> edges = new ArrayList<Edge<T>>();
      final T object = edge.getObject();
      remove(edge);
      for (int i = 0; i < lines.getNumGeometries(); i++) {
        final LineString line = (LineString)lines.getGeometryN(i);
        final T newObject = clone(object, line);
        final Edge<T> newEdge = add(newObject, line);
        edges.add(newEdge);
      }
      return edges;
    } else {
      return Collections.emptyList();
    }
  }

  public Edge<T> replaceEdge(final Edge<T> edge, final LineString line) {
    if (!edge.isRemoved()) {
      final T object = edge.getObject();
      remove(edge);
      final T newObject = clone(object, line);
      return add(newObject, line);
    } else {
      return null;
    }
  }

  public List<Edge<T>> replaceEdge(final Edge<T> edge,
    final List<LineString> lines) {
    if (!edge.isRemoved()) {
      final List<Edge<T>> edges = new ArrayList<Edge<T>>();
      final T object = edge.getObject();
      remove(edge);
      for (final LineString line : lines) {
        final T newObject = clone(object, line);
        final Edge<T> newEdge = add(newObject, line);
        edges.add(newEdge);
      }
      return edges;
    } else {
      return Collections.emptyList();
    }
  }

  public void setPrecisionModel(final CoordinatesPrecisionModel precisionModel) {
    this.precisionModel = precisionModel;
  }

  public List<Edge<T>> splitEdge(final Edge<T> edge,
    final Collection<Node<T>> nodes) {
    final List<Edge<T>> newEdges = new ArrayList<Edge<T>>();
    if (edge.isRemoved()) {
      return Collections.emptyList();
    } else {
      final LineString line = edge.getLine();
      final CoordinatesList points = CoordinatesListUtil.get(line);
      final Set<Integer> splitVertices = new TreeSet<Integer>();
      final Set<Integer> splitIndexes = new TreeSet<Integer>();

      for (final Iterator<Node<T>> nodeIter = nodes.iterator(); nodeIter.hasNext();) {
        final Node<T> node = nodeIter.next();
        if (points.equal2d(0, node)) {
          nodeIter.remove();
        }
      }
      final Map<Node<T>, Double> nodeDistance = new HashMap<Node<T>, Double>();
      final Map<Node<T>, Integer> nodeSegment = new HashMap<Node<T>, Integer>();

      for (int i = 1; i < points.size() && !nodes.isEmpty(); i++) {
        for (final Iterator<Node<T>> nodeIter = nodes.iterator(); nodeIter.hasNext();) {
          final Node<T> node = nodeIter.next();
          if (points.equal2d(i, node)) {
            if (i < points.size() - 1) {
              splitVertices.add(i);
              splitIndexes.add(i);
            }
            nodeIter.remove();
          } else {
            final int segmentIndex = i - 1;
            final double x = node.getX();
            final double y = node.getY();
            final double x1 = points.getX(segmentIndex);
            final double y1 = points.getY(segmentIndex);
            final double x2 = points.getX(i);
            final double y2 = points.getY(i);
            final double distance = LineSegmentUtil.distance(x1, y1, x2, y2, x,
              y);
            if (distance == 0) {
              nodeDistance.put(node, distance);
              nodeSegment.put(node, segmentIndex);
              nodeIter.remove();
            } else {
              final double projectionFactor = LineSegmentUtil.projectionFactor(
                x1, y1, x2, y2, x, y);
              if (projectionFactor >= 0.0 && projectionFactor <= 1.0) {
                final Double closestDistance = nodeDistance.get(node);
                if (closestDistance == null) {
                  nodeSegment.put(node, segmentIndex);
                  nodeDistance.put(node, distance);
                } else if (closestDistance.compareTo(distance) > 1) {
                  nodeSegment.put(node, segmentIndex);
                  nodeDistance.put(node, distance);
                }
              }
            }
          }
        }
      }
      final T object = edge.getObject();
      final GeometryFactory geometryFactory = GeometryFactory.getFactory(line);
      final Map<Integer, Set<Node<T>>> segmentSplitNodes = new TreeMap<Integer, Set<Node<T>>>();
      for (final Entry<Node<T>, Integer> entry : nodeSegment.entrySet()) {
        final Node<T> node = entry.getKey();
        final Integer index = entry.getValue();
        Set<Node<T>> splitNodes = segmentSplitNodes.get(index);
        if (splitNodes == null) {
          final Coordinates point = points.get(index);
          splitNodes = new TreeSet<Node<T>>(
            new CoordinatesDistanceComparator<T>(point));
          segmentSplitNodes.put(index, splitNodes);
          splitIndexes.add(index);
        }
        splitNodes.add(node);
        nodes.remove(node);
      }
      if (nodes.isEmpty()) {
        int startIndex = 0;
        Coordinates startPoint = null;
        for (final Integer index : splitIndexes) {
          if (splitVertices.contains(index)) {
            final CoordinatesList newPoints = CoordinatesListUtil.subList(
              points, startPoint, startIndex, index - startIndex + 1, null);
            final Edge<T> newEdge = createEdge(geometryFactory, object,
              newPoints);
            newEdges.add(newEdge);
            startPoint = null;
            startIndex = index;
          }
          final Set<Node<T>> splitNodes = segmentSplitNodes.get(index);
          if (splitNodes != null) {
            for (final Node<T> node : splitNodes) {
              final QName typeName = edge.getTypeName();
              final Coordinates point = node.get3dCoordinates(typeName);
              final CoordinatesList newPoints;
              if (startIndex > index) {
                newPoints = CoordinatesListUtil.create(points.getNumAxis(),
                  startPoint, point);
              } else {
                newPoints = CoordinatesListUtil.subList(points, startPoint,
                  startIndex, index - startIndex + 1, point);
              }
              final Edge<T> newEdge = createEdge(geometryFactory, object,
                newPoints);
              newEdges.add(newEdge);
              startPoint = point;
              startIndex = index + 1;
            }
          }
        }
        final CoordinatesList newPoints = CoordinatesListUtil.subList(points,
          startPoint, startIndex);
        final Edge<T> newEdge = createEdge(geometryFactory, object, newPoints);
        newEdges.add(newEdge);
        edge.remove();
        return newEdges;
      } else {
        return Collections.singletonList(edge);
      }
    }
  }

  public List<Edge<T>> splitEdge(final Edge<T> edge, final Node<T>... nodes) {
    return splitEdge(edge, Arrays.asList(nodes));
  }

  public List<Edge<T>> splitEdge(final Edge<T> edge, final Node<T> node) {
    if (!edge.isRemoved()) {
      final Coordinates point = node;
      final LineString line = edge.getLine();
      final CoordinatesList points = CoordinatesListUtil.get(line);

      final Map<String, Number> result = CoordinatesListUtil.findClosestSegmentAndCoordinate(
        points, point);
      final int segmentIndex = result.get("segmentIndex").intValue();
      if (segmentIndex != -1) {
        List<LineString> lines;
        final int coordinateIndex = result.get("coordinateIndex").intValue();
        final int coordinateDistance = result.get("coordinateDistance")
          .intValue();
        final int segmentDistance = result.get("segmentDistance").intValue();
        if (coordinateIndex == 0) {
          if (coordinateDistance == 0) {
            return Collections.singletonList(edge);
          } else if (segmentDistance == 0) {
            lines = LineStringUtil.split(line, segmentIndex, point);
          } else {
            final Coordinates c0 = points.get(0);
            Coordinates c1;
            int i = 1;
            do {
              c1 = points.get(i);
              i++;
            } while (c1.equals(c0));
            if (CoordinatesUtil.isAcute(c1, c0, point)) {
              lines = LineStringUtil.split(line, 0, point);
            } else if (edge.getFromNode().getDegree() == 1) {
              final LineString newLine = LineStringUtil.insert(line, 0, point);
              lines = Collections.singletonList(newLine);
            } else {
              return Collections.singletonList(edge);
            }
          }
        } else if (coordinateIndex == line.getNumPoints() - 1) {
          if (coordinateDistance == 0) {
            return Collections.singletonList(edge);
          } else if (segmentDistance == 0) {
            lines = LineStringUtil.split(line, segmentIndex, point);
          } else {
            final Coordinates cn = points.get(line.getNumPoints() - 1);
            Coordinates cn1;
            int i = line.getNumPoints() - 2;
            do {
              cn1 = points.get(i);
              i++;
            } while (cn1.equals(cn));
            if (CoordinatesUtil.isAcute(cn1, cn, point)) {
              lines = LineStringUtil.split(line, segmentIndex, point);
            } else if (edge.getToNode().getDegree() == 1) {
              final LineString newLine = LineStringUtil.insert(line,
                line.getNumPoints(), point);
              lines = Collections.singletonList(newLine);
            } else {
              return Collections.singletonList(edge);
            }
          }
        } else {
          lines = LineStringUtil.split(line, segmentIndex, point);
        }
        final List<Edge<T>> newEdges = replaceEdge(edge, lines);
        return newEdges;
      }
      return Collections.singletonList(edge);
    } else {
      return Collections.emptyList();
    }
  }

  public void visitEdges(final Comparator<Edge<T>> comparator,
    final Visitor<Edge<T>> visitor) {
    visitEdges(null, comparator, visitor);
  }

  public void visitEdges(final Filter<Edge<T>> filter,
    final Comparator<Edge<T>> comparator, final Visitor<Edge<T>> visitor) {
    final LinkedList<Edge<T>> edges = new LinkedList<Edge<T>>();
    copyEdges(filter, edges);
    if (comparator != null) {
      Collections.sort(edges, comparator);
    }
    final EdgeEventListener<T> listener = new EdgeEventListener<T>() {
      public void edgeEvent(final EdgeEvent<T> edgeEvent) {
        final Edge<T> edge = edgeEvent.getEdge();
        final String action = edgeEvent.getAction();
        if (action.equals(EdgeEvent.EDGE_ADDED)) {
          edges.addFirst(edge);
          if (comparator == null) {
            Collections.sort(edges);
          } else {
            Collections.sort(edges, comparator);
          }
        } else if (action.equals(EdgeEvent.EDGE_REMOVED)) {
          edges.remove(edge);
        }
      }
    };
    edgeListeners.add(listener);
    try {
      while (!edges.isEmpty()) {
        final Edge<T> edge = edges.remove(0);
        if (!edge.isRemoved()) {
          if (!visitor.visit(edge)) {
            return;
          }
        }
      }
    } finally {
      edgeListeners.remove(listener);
    }
  }

  public void visitEdges(final Filter<Edge<T>> filter,
    final Visitor<Edge<T>> visitor) {
    visitEdges(filter, null, visitor);
  }

  @SuppressWarnings("unchecked")
  public void visitEdges(final Visitor<Edge<T>> visitor) {
    Filter<Edge<T>> filter = null;
    if (visitor instanceof FilterProxy) {
      filter = ((FilterProxy<Edge<T>>)visitor).getFilter();
    }
    Comparator<Edge<T>> comparator = null;
    if (visitor instanceof ComparatorProxy) {
      comparator = ((ComparatorProxy<Edge<T>>)visitor).getComparator();
    }
    visitEdges(filter, comparator, visitor);
  }

  public void visitNodes(final Filter<Node<T>> filter,
    final Comparator<Node<T>> comparator, final Visitor<Node<T>> visitor) {
    final List<Node<T>> nodes = new LinkedList<Node<T>>();
    copyNodes(filter, nodes);
    if (comparator != null) {
      Collections.sort(nodes, comparator);
    }

    final NodeEventListener<T> listener = new NodeEventListener<T>() {
      public void nodeEvent(final NodeEvent<T> nodeEvent) {
        final Node<T> node = nodeEvent.getNode();
        final String action = nodeEvent.getAction();
        if (action.equals(NodeEvent.NODE_ADDED)) {
          nodes.add(node);
          if (comparator == null) {
            Collections.sort(nodes);
          } else {
            Collections.sort(nodes, comparator);
          }
        } else if (action.equals(NodeEvent.NODE_REMOVED)) {
          nodes.remove(node);
        }
      }
    };
    nodeListeners.add(listener);
    try {
      while (!nodes.isEmpty()) {
        final Node<T> node = nodes.remove(0);
        if (!node.isRemoved()) {
          if (!visitor.visit(node)) {
            return;
          }
        }
      }
    } finally {
      nodeListeners.remove(listener);
    }
  }

  public void visitNodes(final Filter<Node<T>> filter,
    final Visitor<Node<T>> visitor) {
    visitNodes(filter, null, visitor);
  }

  @SuppressWarnings("unchecked")
  public void visitNodes(final Visitor<Node<T>> visitor) {
    Filter<Node<T>> filter = null;
    if (visitor instanceof FilterProxy) {
      filter = ((FilterProxy<Node<T>>)visitor).getFilter();
    }
    Comparator<Node<T>> comparator = null;
    if (visitor instanceof ComparatorProxy) {
      comparator = ((ComparatorProxy<Node<T>>)visitor).getComparator();
    }
    visitNodes(filter, comparator, visitor);
  }

  public void visitNodes(final Visitor<Node<T>> visitor,
    final Comparator<Node<T>> comparator) {
    visitNodes(null, comparator, visitor);
  }
}
