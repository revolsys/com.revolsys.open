package com.revolsys.gis.graph;

import java.lang.ref.WeakReference;
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
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PreDestroy;

import com.revolsys.collection.IntHashMap;
import com.revolsys.collection.Visitor;
import com.revolsys.collection.bplus.BPlusTreeMap;
import com.revolsys.comparator.ComparatorProxy;
import com.revolsys.filter.Filter;
import com.revolsys.filter.FilterProxy;
import com.revolsys.filter.FilterUtil;
import com.revolsys.gis.algorithm.index.IdObjectIndex;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.graph.attribute.NodeAttributes;
import com.revolsys.gis.graph.attribute.ObjectAttributeProxy;
import com.revolsys.gis.graph.comparator.NodeDistanceComparator;
import com.revolsys.gis.graph.event.EdgeEvent;
import com.revolsys.gis.graph.event.EdgeEventListener;
import com.revolsys.gis.graph.event.EdgeEventListenerList;
import com.revolsys.gis.graph.event.NodeEvent;
import com.revolsys.gis.graph.event.NodeEventListener;
import com.revolsys.gis.graph.event.NodeEventListenerList;
import com.revolsys.gis.graph.filter.IsPointOnLineEdgeFilter;
import com.revolsys.gis.graph.visitor.DeleteEdgeVisitor;
import com.revolsys.gis.graph.visitor.EdgeWithinDistance;
import com.revolsys.gis.graph.visitor.NodeWithinBoundingBoxVisitor;
import com.revolsys.gis.graph.visitor.NodeWithinDistanceOfCoordinateVisitor;
import com.revolsys.gis.graph.visitor.NodeWithinDistanceOfGeometryVisitor;
import com.revolsys.gis.jts.GeometryEditUtil;
import com.revolsys.gis.jts.LineStringUtil;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.gis.model.coordinates.comparator.CoordinatesDistanceComparator;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.io.page.PageValueManager;
import com.revolsys.io.page.SerializablePageValueManager;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.util.MathUtil;
import com.revolsys.visitor.CreateListVisitor;

public class Graph<T> {

  private static final AtomicInteger GRAPH_IDS = new AtomicInteger();

  private static Map<Integer, WeakReference<Graph<?>>> graphs = new HashMap<Integer, WeakReference<Graph<?>>>();

  @SuppressWarnings("unchecked")
  public static <V> Graph<V> getGraph(final int id) {
    final WeakReference<Graph<?>> reference = graphs.get(id);
    if (reference == null) {
      return null;
    } else {
      final Graph<?> graph = reference.get();
      if (graph == null) {
        graphs.remove(id);
        return null;
      } else {
        return (Graph<V>)graph;
      }
    }
  }

  private final int id = GRAPH_IDS.incrementAndGet();

  private int maxEdgesInMemory = Integer.MAX_VALUE;

  private boolean inMemory = true;

  private IdObjectIndex<Edge<T>> edgeIndex;

  private Map<Integer, LineString> edgeLinesById = new IntHashMap<LineString>();

  private final EdgeEventListenerList<T> edgeListeners = new EdgeEventListenerList<T>();

  private Map<Integer, T> edgeObjectsById = new IntHashMap<T>();

  private Map<Integer, Map<String, Object>> edgeAttributesById = new IntHashMap<Map<String, Object>>();

  private Map<Integer, Map<String, Object>> nodeAttributesById = new IntHashMap<Map<String, Object>>();

  private Map<Integer, Edge<T>> edgesById = new IntHashMap<Edge<T>>();

  private final Map<Edge<T>, Integer> edgeIds = new TreeMap<Edge<T>, Integer>();

  private GeometryFactory geometryFactory = GeometryFactory.getFactory();

  private int nextEdgeId;

  private int nextNodeId;

  private IdObjectIndex<Node<T>> nodeIndex;

  private final NodeEventListenerList<T> nodeListeners = new NodeEventListenerList<T>();

  private Map<Coordinates, Integer> nodesIdsByCoordinates = new TreeMap<Coordinates, Integer>();

  private Map<Integer, Node<T>> nodesById = new IntHashMap<Node<T>>();

  private GeometryFactory precisionModel = GeometryFactory.getFactory();

  public Graph() {
    this(true);
  }

  protected Graph(final boolean storeLines) {
    graphs.put(id, new WeakReference<Graph<?>>(this));
    if (!storeLines) {
      edgeLinesById = null;
    }
  }

  public void add(final NodeEventListener<T> listener) {
    nodeListeners.add(listener);
  }

  protected Edge<T> addEdge(final T object, final Coordinates from,
    final Coordinates to) {
    return addEdge(object, null, from, to);
  }

  public Edge<T> addEdge(final T object, final LineString line) {
    final CoordinatesList points = CoordinatesListUtil.get(line);
    final Coordinates from = points.get(0);
    final Coordinates to = points.get(points.size() - 1);
    return addEdge(object, line, from, to);
  }

  /**
   * Actually add the edge.
   * 
   * @param object
   * @param line
   * @param from
   * @param to
   * @return
   */
  protected Edge<T> addEdge(final T object, final LineString line,
    final Coordinates from, final Coordinates to) {
    if (inMemory && getEdgeCount() >= maxEdgesInMemory) {
      edgeAttributesById = BPlusTreeMap.createIntSeralizableTempDisk(edgeAttributesById);
      // TODO edgIds
      // TODO edgeIndex
      edgeLinesById = BPlusTreeMap.createIntSeralizableTempDisk(edgeLinesById);
      edgeObjectsById = BPlusTreeMap.createIntSeralizableTempDisk(edgeObjectsById);
      edgesById = BPlusTreeMap.createIntSeralizableTempDisk(edgesById);

      // TODO nodeIndex
      nodeAttributesById = BPlusTreeMap.createIntSeralizableTempDisk(nodeAttributesById);
      nodesById = BPlusTreeMap.createIntSeralizableTempDisk(nodesById);
      nodesIdsByCoordinates = BPlusTreeMap.createTempDisk(
        nodesIdsByCoordinates, new SerializablePageValueManager<Coordinates>(),
        PageValueManager.INT);
      inMemory = false;
    }
    final Node<T> fromNode = getNode(from);
    final Node<T> toNode = getNode(to);
    final int edgeId = ++nextEdgeId;
    final Edge<T> edge = new Edge<T>(edgeId, this, fromNode, toNode);
    if (edgeLinesById != null) {
      edgeLinesById.put(edgeId, line);
    }
    edgeObjectsById.put(edgeId, object);
    edgesById.put(edgeId, edge);
    edgeIds.put(edge, edgeId);
    if (edgeIndex != null) {
      edgeIndex.add(edge);
    }
    edgeListeners.edgeEvent(edge, null, EdgeEvent.EDGE_ADDED, null);
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

  @PreDestroy
  public void close() {
    edgeAttributesById.clear();
    edgeIds.clear();
    // TODO edgeIndex
    edgeLinesById.clear();
    edgeObjectsById.clear();
    edgesById.clear();

    // TODO nodeIndex.clear();
    nodeAttributesById.clear();
    nodesIdsByCoordinates.clear();
  }

  public boolean contains(final Edge<T> edge) {
    if (edge.getGraph() == this) {
      final int id = edge.getId();
      return edgesById.containsKey(id);
    }
    return false;
  }

  public Edge<T> createEdge(
    final com.revolsys.jts.geom.GeometryFactory geometryFactory,
    final T object, final CoordinatesList points) {
    final LineString newLine = geometryFactory.lineString(points);
    final T newObject = clone(object, newLine);
    final Edge<T> newEdge = addEdge(newObject, newLine);
    return newEdge;
  }

  public void deleteEdges(final Filter<Edge<T>> filter) {
    final DeleteEdgeVisitor<T> visitor = new DeleteEdgeVisitor<T>();
    visitEdges(filter, visitor);
  }

  protected void evict(final Edge<T> edge) {
    // TODO
  }

  protected void evict(final Node<T> node) {
    // TODO
  }

  public List<Edge<T>> findEdges(final Coordinates point, final double distance) {
    return EdgeWithinDistance.edgesWithinDistance(this, point, distance);
  }

  public List<Edge<T>> findEdges(final EdgeVisitor<T> visitor) {
    final CreateListVisitor<Edge<T>> results = new CreateListVisitor<Edge<T>>();
    queryEdges(visitor, results);
    final List<Edge<T>> edges = results.getList();
    Collections.sort(edges);
    return edges;
  }

  /**
   * Find the node by point coordinates returning the node if it exists, null
   * otherwise.
   * 
   * @param point The point coordinates to find the node for.
   * @return The nod or null if not found.
   */
  public Node<T> findNode(final Coordinates point) {
    final Integer nodeId = nodesIdsByCoordinates.get(point);
    if (nodeId == null) {
      return null;
    } else {
      return getNode(nodeId);
    }
  }

  public List<Node<T>> findNodes(BoundingBox boundingBox) {
    boundingBox = boundingBox.convert(getGeometryFactory());
    return NodeWithinBoundingBoxVisitor.getNodes(this, boundingBox);
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
    BoundingBox envelope = new Envelope(point);
    envelope = envelope.expand(distance);
    getNodeIndex().visit(envelope, visitor);
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

    final com.revolsys.jts.geom.BoundingBox envelope = filter.getEnvelope();
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
    if (geometry == null) {
      return Collections.emptyList();
    } else {
      final CreateListVisitor<Node<T>> results = new CreateListVisitor<Node<T>>();
      final Visitor<Node<T>> visitor = new NodeWithinDistanceOfGeometryVisitor<T>(
        geometry, distance, results);
      BoundingBox envelope = geometry.getBoundingBox();
      envelope = envelope.expand(distance);
      getNodeIndex().visit(envelope, visitor);
      final List<Node<T>> nodes = results.getList();
      Collections.sort(nodes);
      return nodes;
    }
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

  public Edge<T> getEdge(final int edgeId) {
    return edgesById.get(edgeId);
  }

  @SuppressWarnings("unchecked")
  protected <V> V getEdgeAttribute(final int edgeId, final String name) {
    final Map<String, Object> edgeAttributes = edgeAttributesById.get(edgeId);
    if (edgeAttributes == null) {
      return null;
    } else {
      Object value = edgeAttributes.get(name);
      if (value instanceof ObjectAttributeProxy) {
        final ObjectAttributeProxy<V, Edge<T>> proxy = (ObjectAttributeProxy<V, Edge<T>>)value;
        final Edge<T> edge = getEdge(edgeId);
        value = proxy.getValue(edge);
      }
      return (V)value;
    }
  }

  protected Map<String, Object> getEdgeAttributes(final int edgeId) {
    final Map<String, Object> edgeAttributes = edgeAttributesById.get(edgeId);
    if (edgeAttributes == null) {
      return Collections.emptyMap();
    } else {
      return Collections.unmodifiableMap(edgeAttributes);
    }
  }

  public int getEdgeCount() {
    return edgesById.size();
  }

  public Collection<Integer> getEdgeIds() {
    return edgesById.keySet();
  }

  public int[] getEdgeIds(final Collection<Edge<T>> edges) {
    final int[] edgeIds = new int[edges.size()];
    int i = 0;
    for (final Edge<T> edge : edges) {
      edgeIds[i] = edge.getId();
      i++;
    }
    return edgeIds;
  }

  public IdObjectIndex<Edge<T>> getEdgeIndex() {
    if (edgeIndex == null) {
      edgeIndex = new EdgeQuadTree<T>(this);
    }
    return edgeIndex;
  }

  public LineString getEdgeLine(final int edgeId) {
    return edgeLinesById.get(edgeId);
  }

  public List<LineString> getEdgeLines() {
    final List<Integer> edgeIds = new ArrayList<Integer>(edgesById.keySet());
    return new EdgeLineList(this, edgeIds);
  }

  public T getEdgeObject(final int edgeId) {
    return edgeObjectsById.get(edgeId);
  }

  public List<Edge<T>> getEdges() {
    final List<Integer> edgeIds = new ArrayList<Integer>(edgesById.keySet());
    return new EdgeList<T>(this, edgeIds);
  }

  public List<Edge<T>> getEdges(final Comparator<Edge<T>> comparator) {
    final List<Edge<T>> targetEdges = getEdges();
    if (comparator != null) {
      Collections.sort(targetEdges, comparator);
    }
    return targetEdges;
  }

  public List<Edge<T>> getEdges(final Edge<T> edge) {
    final com.revolsys.jts.geom.BoundingBox envelope = edge.getEnvelope();
    final IdObjectIndex<Edge<T>> edgeIndex = getEdgeIndex();
    return edgeIndex.query(envelope);
  }

  public List<Edge<T>> getEdges(final Filter<Edge<T>> filter) {
    final List<Edge<T>> edges = new EdgeList<T>(this);
    for (final Integer edgeId : getEdgeIds()) {
      final Edge<T> edge = getEdge(edgeId);
      if (FilterUtil.matches(filter, edge)) {
        edges.add(edge);
      }
    }
    return edges;
  }

  public List<Edge<T>> getEdges(final Filter<Edge<T>> filter,
    final com.revolsys.jts.geom.BoundingBox envelope) {
    final CreateListVisitor<Edge<T>> results = new CreateListVisitor<Edge<T>>(
      filter);
    final IdObjectIndex<Edge<T>> edgeIndex = getEdgeIndex();
    edgeIndex.visit(envelope, results);
    final List<Edge<T>> edges = results.getList();
    Collections.sort(edges);
    return edges;

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
    final Comparator<Edge<T>> comparator,
    final com.revolsys.jts.geom.BoundingBox envelope) {
    final CreateListVisitor<Edge<T>> results = new CreateListVisitor<Edge<T>>(
      filter);
    final IdObjectIndex<Edge<T>> edgeIndex = getEdgeIndex();
    edgeIndex.visit(envelope, results);
    final List<Edge<T>> targetEdges = results.getList();
    if (comparator == null) {
      Collections.sort(targetEdges);
    } else {
      Collections.sort(targetEdges, comparator);
    }
    return targetEdges;

  }

  public List<Edge<T>> getEdges(final Filter<Edge<T>> filter,
    final Comparator<Edge<T>> comparator, final Geometry geometry) {
    final com.revolsys.jts.geom.BoundingBox envelope = geometry.getBoundingBox();
    return getEdges(filter, comparator, envelope);
  }

  public List<Edge<T>> getEdges(final Filter<Edge<T>> filter,
    final Geometry geometry) {
    final com.revolsys.jts.geom.BoundingBox envelope = geometry.getBoundingBox();
    return getEdges(filter, envelope);
  }

  public List<Edge<T>> getEdges(final int... ids) {
    final List<Edge<T>> edges = new ArrayList<Edge<T>>();
    for (final int edgeId : ids) {
      final Edge<T> edge = getEdge(edgeId);
      if (edge != null) {
        edges.add(edge);
      }
    }
    return edges;
  }

  public List<Edge<T>> getEdges(final List<Integer> ids) {
    final List<Edge<T>> edges = new ArrayList<Edge<T>>();
    for (final int edgeId : ids) {
      final Edge<T> edge = getEdge(edgeId);
      if (edge != null) {
        edges.add(edge);
      }
    }
    return edges;
  }

  public com.revolsys.jts.geom.GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  public int getId() {
    return id;
  }

  public int getMaxEdgesInMemory() {
    return maxEdgesInMemory;
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
      final int nodeId = ++nextNodeId;
      node = new Node<T>(nodeId, this, point);
      nodesIdsByCoordinates.put(new DoubleCoordinates(node, 2), nodeId);
      nodesById.put(nodeId, node);
      if (nodeIndex != null) {
        nodeIndex.add(node);
      }
      nodeListeners.nodeEvent(node, null, null, NodeEvent.NODE_ADDED, null);
    }
    return node;
  }

  public Node<T> getNode(final int nodeId) {
    return nodesById.get(nodeId);
  }

  @SuppressWarnings("unchecked")
  protected <V> V getNodeAttribute(final int nodeId, final String name) {
    final Map<String, Object> nodeAttributes = nodeAttributesById.get(nodeId);
    if (nodeAttributes == null) {
      return null;
    } else {
      Object value = nodeAttributes.get(name);
      if (value instanceof ObjectAttributeProxy) {
        final ObjectAttributeProxy<V, Node<T>> proxy = (ObjectAttributeProxy<V, Node<T>>)value;
        final Node<T> node = getNode(nodeId);
        value = proxy.getValue(node);
      }
      return (V)value;
    }
  }

  protected Map<String, Object> getNodeAttributes(final int nodeId) {
    final Map<String, Object> nodeAttributes = nodeAttributesById.get(nodeId);
    if (nodeAttributes == null) {
      return Collections.emptyMap();
    } else {
      return Collections.unmodifiableMap(nodeAttributes);
    }
  }

  public Collection<Integer> getNodeIds() {
    return nodesById.keySet();
  }

  public IdObjectIndex<Node<T>> getNodeIndex() {
    if (nodeIndex == null) {
      nodeIndex = new NodeQuadTree<T>(this);
    }
    return nodeIndex;
  }

  public List<Node<T>> getNodes() {
    final List<Integer> nodeIds = new ArrayList<Integer>(
      nodesIdsByCoordinates.values());
    return new NodeList<T>(this, nodeIds);
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
    final com.revolsys.jts.geom.BoundingBox envelope) {
    return getNodes(filter, null, envelope);

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
    final Comparator<Node<T>> comparator,
    final com.revolsys.jts.geom.BoundingBox envelope) {
    final CreateListVisitor<Node<T>> results = new CreateListVisitor<Node<T>>(
      filter);
    final IdObjectIndex<Node<T>> nodeIndex = getNodeIndex();
    nodeIndex.visit(envelope, results);
    final List<Node<T>> nodes = results.getList();
    if (comparator == null) {
      Collections.sort(nodes);
    } else {
      Collections.sort(nodes, comparator);
    }
    return nodes;

  }

  public List<Node<T>> getNodes(final List<Integer> nodeIds) {
    final List<Node<T>> nodes = new ArrayList<Node<T>>();
    for (final int nodeId : nodeIds) {
      final Node<T> node = getNode(nodeId);
      if (node != null) {
        nodes.add(node);
      }
    }
    return nodes;

  }

  public List<T> getObjects() {
    final List<T> objects = new ArrayList<T>();
    for (final Edge<T> edge : getEdges()) {
      if (edge != null && !edge.isRemoved()) {
        final T object = edge.getObject();
        objects.add(object);
      }
    }
    return objects;
  }

  public GeometryFactory getPrecisionModel() {
    return precisionModel;
  }

  /**
   * Get the type name for the edge.
   * 
   * @param edge The edge.
   * @return The type name.
   */
  public String getTypeName(final Edge<T> edge) {
    final Object object = edge.getObject();
    if (object == null) {
      return null;
    } else {
      final String className = edge.getClass().getName();
      return className;
    }
  }

  public boolean hasEdge(final Edge<T> edge) {
    final Node<T> fromNode = edge.getFromNode();
    final Node<T> toNode = edge.getToNode();
    return hasEdgeBetween(fromNode, toNode);
  }

  public boolean hasEdgeBetween(final Coordinates fromPoint,
    final Coordinates toPoint) {
    final Node<T> fromNode = findNode(fromPoint);
    if (fromNode == null) {
      return false;
    } else {
      final Node<T> toNode = findNode(toPoint);
      if (toNode == null) {
        return false;
      } else {
        return fromNode.hasEdgeTo(toNode);
      }
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
      final Map<String, Object> attributes1 = edge1.getAttributes();
      final Map<String, Object> attributes2 = edge2.getAttributes();
      final T object1 = edge1.getObject();
      final LineString line1 = edge1.getLine();
      final LineString line2 = edge2.getLine();

      final LineString newLine = LineStringUtil.merge(node, line1, line2);

      final T mergedObject = clone(object1, newLine);
      final Edge<T> newEdge = addEdge(mergedObject, newLine);
      newEdge.setAttributes(attributes2);
      newEdge.setAttributes(attributes1);
      remove(edge1);
      remove(edge2);
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

  public void moveNode(final String typePath, final Node<DataObject> fromNode,
    final Node<DataObject> toNode, final Coordinates newPoint) {
    if (!fromNode.isRemoved() && !toNode.isRemoved()) {
      if (!fromNode.equals(toNode)) {
        final List<Edge<DataObject>> edges = NodeAttributes.getEdgesByType(
          fromNode, typePath);

        for (final Edge<DataObject> edge : edges) {
          if (!edge.isRemoved()) {
            final LineString line = edge.getLine();
            LineString newLine;
            if (CoordinatesUtil.get(line).equals(fromNode)) {
              newLine = LineStringUtil.subLineString(line, newPoint, 1,
                line.getVertexCount() - 1, null);
            } else {
              newLine = LineStringUtil.subLineString(line, null, 0,
                line.getVertexCount() - 1, newPoint);
            }
            final Graph<DataObject> graph = edge.getGraph();
            graph.replaceEdge(edge, newLine);
          }
        }
      }
    }
  }

  public boolean moveNodesToMidpoint(final String typePath,
    final Node<DataObject> node1, final Node<DataObject> node2) {
    final Coordinates point1 = node1.get3dCoordinates(typePath);
    final Coordinates point2 = node2.get3dCoordinates(typePath);

    final Graph<DataObject> graph = node1.getGraph();
    final Coordinates midPoint = LineSegmentUtil.midPoint(
      GeometryFactory.getFactory(0, 3, 1000.0, 1.0), node2, node1);
    final double x = midPoint.getX();
    final double y = midPoint.getY();
    final double z1 = point1.getZ();
    final double z2 = point2.getZ();
    double z;
    if (z1 == 0 || MathUtil.isNanOrInfinite(z1)) {
      z = z2;
    } else if (z2 == 0 || MathUtil.isNanOrInfinite(z1)) {
      z = z1;
    } else {
      z = Double.NaN;
    }
    final Coordinates newPoint = new DoubleCoordinates(x, y, z);
    final Node<DataObject> newNode = graph.getNode(midPoint);
    if (!Node.hasEdgesBetween(typePath, node1, newNode)
      && !Node.hasEdgesBetween(typePath, node2, newNode)) {
      if (node1.equals2d(newNode)) {
        moveNode(typePath, node2, node1, newPoint);
      } else if (node2.equals2d(newNode)) {
        moveNode(typePath, node1, node2, newPoint);
      } else {
        moveNode(typePath, node1, newNode, newPoint);
        moveNode(typePath, node2, newNode, newPoint);
      }
      return true;
    } else {
      return false;
    }
  }

  public boolean movePointsWithinTolerance(
    final Map<Coordinates, Coordinates> movedNodes, final double maxDistance,
    final Node<T> node1) {
    final Graph<T> graph1 = node1.getGraph();
    List<Node<T>> nodes2 = findNodes(node1, maxDistance);
    if (nodes2.isEmpty()) {
      nodes2 = findNodes(node1, maxDistance * 2);
      if (nodes2.size() == 1) {
        final Node<T> node2 = nodes2.get(0);
        if (graph1.findNode(node2) == null) {
          final List<Edge<T>> inEdges = node2.getInEdges();
          final List<Edge<T>> outEdges = node2.getOutEdges();
          if (inEdges.size() == 1 && outEdges.size() == 1) {
            final Edge<T> inEdge = inEdges.get(0);
            if (inEdge.distance(node1) < maxDistance) {
              moveToMidpoint(movedNodes, graph1, node1, node2);
              return true;
            }
            final Edge<T> outEdge = outEdges.get(0);
            if (outEdge.distance(node1) < maxDistance) {
              moveToMidpoint(movedNodes, graph1, node1, node2);
              return true;
            }
          }
        }
      }
    } else if (nodes2.size() == 1) {
      final Node<T> node2 = nodes2.get(0);
      if (graph1.findNode(node2) == null) {
        moveToMidpoint(movedNodes, graph1, node1, node2);
      }
    }
    return true;
  }

  public void moveToMidpoint(final Map<Coordinates, Coordinates> movedNodes,
    final Graph<T> graph1, final Node<T> node1, final Node<T> node2) {
    final GeometryFactory precisionModel = graph1.getPrecisionModel();
    final Coordinates midPoint = LineSegmentUtil.midPoint(precisionModel,
      node1, node2);
    if (!node1.equals2d(midPoint)) {
      if (movedNodes != null) {
        movedNodes.put(node1.cloneCoordinates(), midPoint);
      }
      node1.move(midPoint);
    }
    if (!node2.equals2d(midPoint)) {
      if (movedNodes != null) {
        movedNodes.put(node2.cloneCoordinates(), midPoint);
      }
      node2.move(midPoint);
    }
  }

  public void nodeMoved(final Node<T> node, final Node<T> newNode) {
  }

  public Iterable<Node<T>> nodes() {
    return getNodes();
  }

  public void queryEdges(final EdgeVisitor<T> visitor) {
    final com.revolsys.jts.geom.BoundingBox env = visitor.getEnvelope();
    final IdObjectIndex<Edge<T>> index = getEdgeIndex();
    index.visit(env, visitor);
  }

  public void queryEdges(final EdgeVisitor<T> visitor,
    final Visitor<Edge<T>> matchVisitor) {
    visitor.setVisitor(matchVisitor);
    queryEdges(visitor);
  }

  public void remove(final Edge<T> edge) {
    if (!edge.isRemoved()) {
      edgeListeners.edgeEvent(edge, null, EdgeEvent.EDGE_REMOVED, null);
      final int edgeId = edge.getId();
      edgeIds.remove(edge);
      edgesById.remove(edgeId);
      edgeAttributesById.remove(edgeId);
      if (edgeLinesById != null) {
        edgeLinesById.remove(edgeId);
      }
      edgeObjectsById.remove(edgeId);
      if (edgeIndex != null) {
        edgeIndex.remove(edge);
      }
      edge.removeInternal();
    }
  }

  public void remove(final EdgeEventListener<T> listener) {
    edgeListeners.remove(listener);
  }

  public void remove(final Node<T> node) {
    if (!node.isRemoved()) {
      final ArrayList<Edge<T>> edges = new ArrayList<Edge<T>>(node.getEdges());
      for (final Edge<T> edge : edges) {
        remove(edge);
      }
      nodeListeners.nodeEvent(node, null, null, NodeEvent.NODE_REMOVED, null);
      final int nodeId = node.getId();
      nodesById.remove(nodeId);
      nodeAttributesById.remove(nodeId);
      nodesIdsByCoordinates.remove(node);
      if (nodeIndex != null) {
        nodeIndex.remove(node);
      }

      node.remove();
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
      for (int i = 0; i < lines.getGeometryCount(); i++) {
        final LineString line = (LineString)lines.getGeometry(i);
        final T newObject = clone(object, line);
        final Edge<T> newEdge = addEdge(newObject, line);
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
      final T newObject = clone(object, line);
      final Edge<T> newEdge = addEdge(newObject, line);
      remove(edge);
      return newEdge;
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
        final Edge<T> newEdge = addEdge(newObject, line);
        edges.add(newEdge);
      }
      return edges;
    } else {
      return Collections.emptyList();
    }
  }

  protected void setEdgeAttribute(final int edgeId, final String name,
    final Object value) {
    Map<String, Object> attributes = edgeAttributesById.get(edgeId);
    if (attributes == null) {
      attributes = new HashMap<String, Object>();
      edgeAttributesById.put(edgeId, attributes);
    }
    attributes.put(name, value);
  }

  protected void setEdgeAttributes(final int edgeId,
    final Map<String, Object> attributes) {
    Map<String, Object> edgeAttributes = edgeAttributesById.get(edgeId);
    if (edgeAttributes == null) {
      edgeAttributes = new HashMap<String, Object>();
      edgeAttributesById.put(edgeId, edgeAttributes);
    }
    edgeAttributes.putAll(attributes);
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
    setPrecisionModel(geometryFactory);
  }

  public void setMaxEdgesInMemory(final int maxEdgesInMemory) {
    this.maxEdgesInMemory = maxEdgesInMemory;
  }

  protected void setNodeAttribute(final int edgeId, final String name,
    final Object value) {
    Map<String, Object> nodeAttributes = nodeAttributesById.get(edgeId);
    if (nodeAttributes == null) {
      nodeAttributes = new HashMap<String, Object>();
      nodeAttributesById.put(edgeId, nodeAttributes);
    }
    nodeAttributes.put(name, value);
  }

  protected void setNodeAttributes(final int edgeId,
    final Map<String, Object> attributes) {
    Map<String, Object> nodeAttributes = nodeAttributesById.get(edgeId);
    if (nodeAttributes == null) {
      nodeAttributes = new HashMap<String, Object>();
      nodeAttributesById.put(edgeId, nodeAttributes);
    }
    nodeAttributes.putAll(attributes);
  }

  public void setPrecisionModel(final GeometryFactory precisionModel) {
    this.precisionModel = precisionModel;
  }

  public <V extends Coordinates> List<Edge<T>> splitEdge(final Edge<T> edge,
    final Collection<V> nodes) {
    return splitEdge(edge, nodes, 0.0);
  }

  public <V extends Coordinates> List<Edge<T>> splitEdge(final Edge<T> edge,
    final Collection<V> splitPoints, final double maxDistance) {
    final Collection<V> nodes = new ArrayList<V>(splitPoints);
    if (edge.isRemoved()) {
      return Collections.emptyList();
    } else {
      final LineString line = edge.getLine();
      final CoordinatesList points = CoordinatesListUtil.get(line);
      final Set<Integer> splitVertices = new TreeSet<Integer>();
      final Set<Integer> splitIndexes = new TreeSet<Integer>();

      for (final Iterator<V> nodeIter = nodes.iterator(); nodeIter.hasNext();) {
        final Coordinates node = nodeIter.next();
        final double distance = points.distance(0, node);
        if (distance < maxDistance) {
          nodeIter.remove();
        }
      }
      final Map<Coordinates, Double> nodeDistanceMap = new HashMap<Coordinates, Double>();
      final Map<Coordinates, Integer> nodeSegment = new HashMap<Coordinates, Integer>();

      for (int i = 1; i < points.size() && !nodes.isEmpty(); i++) {
        for (final Iterator<V> nodeIter = nodes.iterator(); nodeIter.hasNext();) {
          final Coordinates node = nodeIter.next();
          final double nodeDistance = points.distance(i, node);
          if (nodeDistance < maxDistance) {
            if (i < points.size() - 1) {
              splitVertices.add(i);
              splitIndexes.add(i);
            }
            nodeDistanceMap.remove(node);
            nodeSegment.remove(node);
            nodeIter.remove();
          } else {
            final int segmentIndex = i - 1;
            final double x = node.getX();
            final double y = node.getY();
            final double x1 = points.getX(segmentIndex);
            final double y1 = points.getY(segmentIndex);
            final double x2 = points.getX(i);
            final double y2 = points.getY(i);
            final double segmentDistance = LineSegmentUtil.distance(x1, y1, x2,
              y2, x, y);
            if (segmentDistance == 0) {
              nodeDistanceMap.put(node, segmentDistance);
              nodeSegment.put(node, segmentIndex);
              nodeIter.remove();
            } else {
              final double projectionFactor = LineSegmentUtil.projectionFactor(
                x1, y1, x2, y2, x, y);
              if (projectionFactor >= 0.0 && projectionFactor <= 1.0) {
                final Double closestDistance = nodeDistanceMap.get(node);
                if (closestDistance == null) {
                  nodeSegment.put(node, segmentIndex);
                  nodeDistanceMap.put(node, segmentDistance);
                } else if (closestDistance.compareTo(segmentDistance) > 0) {
                  nodeSegment.put(node, segmentIndex);
                  nodeDistanceMap.put(node, segmentDistance);
                }
              }
            }
          }
        }
      }
      final T object = edge.getObject();
      final com.revolsys.jts.geom.GeometryFactory geometryFactory = GeometryFactory.getFactory(line);
      final Map<Integer, Set<Coordinates>> segmentSplitNodes = new TreeMap<Integer, Set<Coordinates>>();
      for (final Entry<Coordinates, Integer> entry : nodeSegment.entrySet()) {
        final Coordinates node = entry.getKey();
        final Integer index = entry.getValue();
        Set<Coordinates> splitNodes = segmentSplitNodes.get(index);
        if (splitNodes == null) {
          final Coordinates point = points.get(index);
          splitNodes = new TreeSet<Coordinates>(
            new CoordinatesDistanceComparator(point));
          segmentSplitNodes.put(index, splitNodes);
          splitIndexes.add(index);
        }
        splitNodes.add(node);
        nodes.remove(node);
      }
      if (nodes.isEmpty()) {
        final List<CoordinatesList> newLines = new ArrayList<CoordinatesList>();
        int startIndex = 0;
        Coordinates startPoint = null;
        for (final Integer index : splitIndexes) {
          if (splitVertices.contains(index)) {
            final CoordinatesList newPoints = CoordinatesListUtil.subList(
              points, startPoint, startIndex, index - startIndex + 1, null);
            newLines.add(newPoints);
            startPoint = null;
            startIndex = index;
          }
          final Set<Coordinates> splitNodes = segmentSplitNodes.get(index);
          if (splitNodes != null) {
            for (final Coordinates splitPoint : splitNodes) {
              final Node<T> node = getNode(splitPoint);
              final String typePath = edge.getTypeName();
              Coordinates point = splitPoint;
              double splitPointZ = splitPoint.getZ();
              if (splitPointZ == 0 || Double.isNaN(splitPointZ)) {
                if (splitPoint instanceof Node<?>) {
                  final Node<?> splitNode = (Node<?>)splitPoint;
                  point = splitNode.get3dCoordinates(typePath);
                  splitPointZ = point.getZ();
                }
                if (splitPointZ == 0 || Double.isNaN(splitPointZ)) {
                  point = node.get3dCoordinates(typePath);
                }
                if (splitPointZ == 0 || Double.isNaN(splitPointZ)) {
                  final Coordinates p1 = points.get(index);
                  final Coordinates p2 = points.get(index + 1);
                  final double z = LineSegmentUtil.getElevation(p1, p2, point);
                  point = new DoubleCoordinates(point.getX(), point.getY(), z);
                }
              }

              final CoordinatesList newPoints;
              if (startIndex > index) {
                final Coordinates[] coordinateArray = {
                  startPoint, point
                };
                newPoints = new DoubleCoordinatesList(points.getAxisCount(),
                  coordinateArray);
              } else {
                newPoints = CoordinatesListUtil.subList(points, startPoint,
                  startIndex, index - startIndex + 1, point);
              }
              newLines.add(newPoints);
              startPoint = point;
              startIndex = index + 1;
            }
          }
        }
        final CoordinatesList newPoints = CoordinatesListUtil.subList(points,
          startPoint, startIndex);
        newLines.add(newPoints);

        if (newLines.size() > 1) {
          final List<Edge<T>> newEdges = new ArrayList<Edge<T>>();
          for (final CoordinatesList edgePoints : newLines) {
            final Edge<T> newEdge = createEdge(geometryFactory, object,
              edgePoints);
            newEdges.add(newEdge);
          }
          edge.remove();
          return newEdges;
        } else {
          return Collections.singletonList(edge);
        }
      } else {
        return Collections.singletonList(edge);
      }
    }
  }

  public List<Edge<T>> splitEdge(final Edge<T> edge, final Coordinates... nodes) {
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
              final LineString newLine = GeometryEditUtil.insertVertex(line, 0,
                point);
              lines = Collections.singletonList(newLine);
            } else {
              return Collections.singletonList(edge);
            }
          }
        } else if (coordinateIndex == line.getVertexCount() - 1) {
          if (coordinateDistance == 0) {
            return Collections.singletonList(edge);
          } else if (segmentDistance == 0) {
            lines = LineStringUtil.split(line, segmentIndex, point);
          } else {
            final Coordinates cn = points.get(line.getVertexCount() - 1);
            Coordinates cn1;
            int i = line.getVertexCount() - 2;
            do {
              cn1 = points.get(i);
              i++;
            } while (cn1.equals(cn));
            if (CoordinatesUtil.isAcute(cn1, cn, point)) {
              lines = LineStringUtil.split(line, segmentIndex, point);
            } else if (edge.getToNode().getDegree() == 1) {
              final LineString newLine = GeometryEditUtil.insertVertex(line,
                line.getVertexCount(), point);
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
    final LinkedList<Edge<T>> edges = new LinkedList<Edge<T>>(getEdges(filter));
    if (comparator != null) {
      Collections.sort(edges, comparator);
    }
    final EdgeEventListener<T> listener = new EdgeEventListener<T>() {
      @Override
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
          if (comparator != null) {
            edges.remove(edge);
          }
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

  public void visitEdges(final Visitor<Edge<T>> visitor,
    final com.revolsys.jts.geom.BoundingBox envelope) {
    final IdObjectIndex<Edge<T>> edgeIndex = getEdgeIndex();
    edgeIndex.visit(envelope, visitor);
  }

  // TODO make this work with cached nodes
  public void visitNodes(final Filter<Node<T>> filter,
    final Comparator<Node<T>> comparator, final Visitor<Node<T>> visitor) {
    final List<Node<T>> nodes = new LinkedList<Node<T>>();
    if (filter == null) {
      nodes.addAll(getNodes());
    } else {
      for (final Node<T> node : getNodes()) {
        if (filter.accept(node)) {
          nodes.add(node);
        }
      }
    }
    if (comparator != null) {
      Collections.sort(nodes, comparator);
    }

    final NodeEventListener<T> listener = new NodeEventListener<T>() {
      @Override
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
