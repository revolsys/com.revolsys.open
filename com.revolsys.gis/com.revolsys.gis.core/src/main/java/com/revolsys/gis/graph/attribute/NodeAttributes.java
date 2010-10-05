package com.revolsys.gis.graph.attribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.namespace.QName;

import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.graph.comparator.NumberComparator;
import com.revolsys.gis.jts.LineStringUtil;
import com.vividsolutions.jts.geom.LineString;

public class NodeAttributes {
  protected static class Methods {
    public static Set<Double> edgeAngles(
      final Node<?> node) {
      final Set<Double> angles = new TreeSet<Double>(
        new NumberComparator<Double>());
      for (final Edge<?> edge : node.getInEdges()) {
        final double toAngle = edge.getToAngle();
        angles.add(toAngle);
      }
      for (final Edge<?> edge : node.getOutEdges()) {
        final double fromAngle = edge.getFromAngle();
        angles.add(fromAngle);
      }
      return angles;
    }

    public static <T> Map<LineString, Map<QName, Set<Edge<T>>>> edgesByLineAndTypeName(
      final Node<T> node) {
      final List<Edge<T>> edges = node.getEdges();
      final Map<LineString, Map<QName, Set<Edge<T>>>> lineEdgeMap = new HashMap<LineString, Map<QName, Set<Edge<T>>>>();
      for (final Edge<T> edge : new HashSet<Edge<T>>(edges)) {
        LineString line = edge.getLine();
        Map<QName, Set<Edge<T>>> edgesByType = edgesByTypeForLine(lineEdgeMap,
          line);
        if (edgesByType == null) {
          edgesByType = new HashMap<QName, Set<Edge<T>>>();
          if (!edge.isForwards(node)) {
            line = LineStringUtil.reverse(line);
          }
          lineEdgeMap.put(line, edgesByType);
        }
        Set<Edge<T>> typeEdges = edgesByType.get(edge.getTypeName());
        if (typeEdges == null) {
          typeEdges = new HashSet<Edge<T>>();
          final QName typeName = edge.getTypeName();
          edgesByType.put(typeName, typeEdges);
        }
        typeEdges.add(edge);
      }
      return lineEdgeMap;
    }

    public static <T> Map<QName, List<Edge<T>>> edgesByType(
      final Node<T> node) {
      final Map<QName, List<Edge<T>>> edgesByType = new HashMap<QName, List<Edge<T>>>();
      for (final Edge<T> edge : node.getEdges()) {
        final QName typeName = edge.getTypeName();
        List<Edge<T>> typeEdges = edgesByType.get(typeName);
        if (typeEdges == null) {
          typeEdges = new ArrayList<Edge<T>>();
          edgesByType.put(typeName, typeEdges);
        }
        typeEdges.add(edge);
      }
      return edgesByType;
    }

    private static <T> Map<QName, Set<Edge<T>>> edgesByTypeForLine(
      final Map<LineString, Map<QName, Set<Edge<T>>>> lineEdgeMap,
      final LineString line) {
      for (final Entry<LineString, Map<QName, Set<Edge<T>>>> entry : lineEdgeMap.entrySet()) {
        final LineString keyLine = entry.getKey();
        if (LineStringUtil.equalsIgnoreDirection2d(line, keyLine)) {
          return entry.getValue();
        }
      }
      return null;
    }

    public static <T> Map<QName, Map<LineString, Set<Edge<T>>>> edgesByTypeNameAndLine(
      final Node<T> node) {
      final List<Edge<T>> edges = node.getEdges();
      final Map<QName, Map<LineString, Set<Edge<T>>>> typeLineEdgeMap = new HashMap<QName, Map<LineString, Set<Edge<T>>>>();
      for (final Edge<T> edge : new HashSet<Edge<T>>(edges)) {
        final QName typeName = edge.getTypeName();
        Map<LineString, Set<Edge<T>>> lineEdgeMap = typeLineEdgeMap.get(typeName);
        if (lineEdgeMap == null) {
          lineEdgeMap = new HashMap<LineString, Set<Edge<T>>>();
          typeLineEdgeMap.put(typeName, lineEdgeMap);
        }

        Edge.addEdgeToEdgesByLine(node, lineEdgeMap, edge);
      }
      return typeLineEdgeMap;
    }

    public static Set<QName> edgeTypeNames(
      final Node<?> node) {
      final Set<QName> typeNames = new HashSet<QName>();
      for (final Edge<?> edge : node.getEdges()) {
        final QName typeName = edge.getTypeName();
        typeNames.add(typeName);
      }
      return typeNames;
    }
  }

  private static String EDGE_ANGLES = "edgeAngles";

  private static String EDGE_TYPE_NAMES = "edgeTypeNames";

  private static String EDGES_BY_LINE_AND_TYPE_NAME = "edgesByLineAndTypeName";

  private static String EDGES_BY_TYPE = "edgesByType";

  private static String EDGES_BY_TYPE_NAME_AND_LINE = "edgesByTypeNameAndLine";

  @SuppressWarnings("unchecked")
  private static <T, V> V getAttribute(
    final Node<T> node,
    final String name) {
    final String attributeName = NodeAttributes.class.getName() + "." + name;
    if (!node.hasAttribute(attributeName)) {
      final ObjectAttributeProxy<T, V> proxy = new InvokeMethodObjectAttributeProxy<T, V>(
        Methods.class, name, Node.class);
      node.setAttribute(attributeName, proxy);
    }
    final V value = (V)node.getAttribute(attributeName);
    return value;
  }

  public static Set<Double> getEdgeAngles(
    final Node<?> node) {
    return getAttribute(node, EDGE_ANGLES);
  }

  /**
   * Get the map of edge angles, which contains a map of type names to the list
   * of edges with that angle and type name.
   * 
   * @param <T>
   * @param node The node.
   * @return The map.
   */
  public static <T> Map<LineString, Map<QName, Set<Edge<T>>>> getEdgesByLineAndTypeName(
    final Node<T> node) {
    return getAttribute(node, EDGES_BY_LINE_AND_TYPE_NAME);
  }

  public static <T> Map<QName, List<Edge<T>>> getEdgesByType(
    final Node<T> node) {
    return getAttribute(node, EDGES_BY_TYPE);
  }

  public static <T> List<Edge<T>> getEdgesByType(
    final Node<T> node,
    final QName typeName) {
    final Map<QName, List<Edge<T>>> edgesByType = getEdgesByType(node);
    final List<Edge<T>> edges = edgesByType.get(typeName);
    if (edges != null) {
      return new ArrayList<Edge<T>>(edges);
    }
    return Collections.emptyList();
  }

  public static <T> Map<QName, Map<LineString, Set<Edge<T>>>> getEdgesByTypeNameAndLine(
    final Node<T> node) {
    return getAttribute(node, EDGES_BY_TYPE_NAME_AND_LINE);
  }

  public static Set<QName> getEdgeTypeNames(
    final Node<? extends Object> node) {
    return getAttribute(node, EDGE_TYPE_NAMES);
  }

}
