package com.revolsys.gis.graph.attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.EdgePair;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.jts.LineStringUtil;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.vividsolutions.jts.geom.LineString;

public class PseudoNodeAttribute {
  public static final String AUTO_FIX_LENGTH = PseudoNodeAttribute.class.getName()
    + ".autoFixLength";

  private static double autoFixEdgesLessThanLength = 3;

  public static PseudoNodeAttribute create(final Node<DataObject> node,
    final Collection<String> equalExcludeAttributes) {
    return new PseudoNodeAttribute(node, equalExcludeAttributes);
  }

  public static PseudoNodeAttribute getAttribute(final Node<DataObject> node,
    final Collection<String> equalExcludeAttributes) {
    final String attributeName = PseudoNodeAttribute.class.getName();
    if (!node.hasAttribute(attributeName)) {
      final ObjectAttributeProxy<PseudoNodeAttribute, Node<DataObject>> proxy = new InvokeMethodObjectAttributeProxy<PseudoNodeAttribute, Node<DataObject>>(
        PseudoNodeAttribute.class, "create", Node.class, equalExcludeAttributes);
      node.setAttribute(attributeName, proxy);
    }
    final PseudoNodeAttribute value = node.getAttribute(attributeName);
    return value;
  }

  public static double getAutoFixEdgesLessThanLength() {
    return autoFixEdgesLessThanLength;
  }

  public static void setAutoFixEdgesLessThanLength(
    final double autoFixEdgesLessThanLength) {
    PseudoNodeAttribute.autoFixEdgesLessThanLength = autoFixEdgesLessThanLength;
  }

  private Collection<String> equalExcludeAttributes = Collections.emptySet();

  private final Map<QName, List<EdgePair<DataObject>>> typeEdgePairs = new HashMap<QName, List<EdgePair<DataObject>>>();

  private final Map<QName, List<EdgePair<DataObject>>> typeReversedEdgePairs = new HashMap<QName, List<EdgePair<DataObject>>>();

  public PseudoNodeAttribute(final Node<DataObject> node,
    final Collection<String> equalExcludeAttributes) {
    this.equalExcludeAttributes = equalExcludeAttributes;
    final Map<QName, Map<LineString, Set<Edge<DataObject>>>> edgesByTypeNameAndLine = NodeAttributes.getEdgesByTypeNameAndLine(node);
    for (final Entry<QName, Map<LineString, Set<Edge<DataObject>>>> entry : edgesByTypeNameAndLine.entrySet()) {
      final QName typeName = entry.getKey();
      final Map<LineString, Set<Edge<DataObject>>> edgesByLine = entry.getValue();
      init(node, typeName, edgesByLine);
    }
  }

  private boolean attributesEqual(final Edge<DataObject> edge1,
    final Edge<DataObject> edge2) {
    final DataObject object1 = edge1.getObject();
    final DataObject object2 = edge2.getObject();
    return EqualsRegistry.INSTANCE.equals(object1, object2,
      equalExcludeAttributes);
  }

  private EdgePair<DataObject> createEdgePair(final Edge<DataObject> edge1,
    final Edge<DataObject> edge2) {
    if (attributesEqual(edge1, edge2)) {
      return new EdgePair<DataObject>(edge1, edge2);
    } else {
      final double length1 = edge1.getLength();
      final double length2 = edge2.getLength();
      if (length1 < autoFixEdgesLessThanLength) {
        final EdgePair<DataObject> edgePair = new EdgePair<DataObject>(edge1,
          edge2);
        if (length2 < autoFixEdgesLessThanLength) {
          if (length1 <= length2) {
            edgePair.setProperty1(AUTO_FIX_LENGTH, Boolean.TRUE);
          } else {
            edgePair.setProperty2(AUTO_FIX_LENGTH, Boolean.TRUE);
          }
        } else {
          edgePair.setProperty1(AUTO_FIX_LENGTH, Boolean.TRUE);
        }
        return edgePair;
      } else if (length2 < autoFixEdgesLessThanLength) {
        final EdgePair<DataObject> edgePair = new EdgePair<DataObject>(edge1,
          edge2);
        edgePair.setProperty2(AUTO_FIX_LENGTH, Boolean.TRUE);
        return edgePair;
      } else {
        return null;
      }
    }
  }

  public Map<QName, List<EdgePair<DataObject>>> getTypeEdgePairs() {
    return typeEdgePairs;
  }

  public Map<QName, List<EdgePair<DataObject>>> getTypeReversedEdgePairs() {
    return typeReversedEdgePairs;
  }

  private void init(final Node<DataObject> node, final QName typeName,
    final Map<LineString, Set<Edge<DataObject>>> edgesByLine) {
    if (isPseudoNode(node, typeName, edgesByLine)) {

    }
  }

  private boolean isPseudoNode(final Node<DataObject> node,
    final QName typeName,
    final Map<LineString, Set<Edge<DataObject>>> edgesByLine) {
    final Set<LineString> lines = edgesByLine.keySet();
    if (!LineStringUtil.hasLoop(lines)) {
      if (edgesByLine.size() == 2) {
        final Iterator<Set<Edge<DataObject>>> edgeIter = edgesByLine.values()
          .iterator();
        final Set<Edge<DataObject>> edges1 = edgeIter.next();
        final Set<Edge<DataObject>> edges2 = edgeIter.next();
        final int size1 = edges1.size();
        final int size2 = edges2.size();
        if (size1 == size2) {
          if (size1 == 1) {
            final Edge<DataObject> edge1 = edges1.iterator().next();
            final Edge<DataObject> edge2 = edges2.iterator().next();
            final EdgePair<DataObject> edgePair = createEdgePair(edge1, edge2);
            if (edgePair != null) {

              final List<EdgePair<DataObject>> pairedEdges = Collections.singletonList(edgePair);
              if (edge1.isForwards(node) == edge2.isForwards(node)) {
                typeReversedEdgePairs.put(typeName, pairedEdges);
              } else {
                typeEdgePairs.put(typeName, pairedEdges);
              }
              return true;
            }
          } else {
            final List<EdgePair<DataObject>> pairedEdges = new ArrayList<EdgePair<DataObject>>();
            typeEdgePairs.put(typeName, pairedEdges);
            final List<Edge<DataObject>> unmatchedEdges1 = new ArrayList<Edge<DataObject>>(
              edges1);
            final List<Edge<DataObject>> unmatchedEdges2 = new ArrayList<Edge<DataObject>>(
              edges2);
            // Find non-reversed matches
            matchEdges(node, unmatchedEdges1, unmatchedEdges2, pairedEdges,
              false);
            if (unmatchedEdges2.isEmpty()) {
              return true;
            } else {
              // Find reversed matches
              final List<EdgePair<DataObject>> reversedPairedEdges = new ArrayList<EdgePair<DataObject>>();
              matchEdges(node, unmatchedEdges1, unmatchedEdges2,
                reversedPairedEdges, true);
              typeReversedEdgePairs.put(typeName, reversedPairedEdges);
              if (unmatchedEdges2.isEmpty()) {
                return true;
              }
            }
          }
        }
      }
    }
    return false;
  }

  private void matchEdges(final Node<DataObject> node,
    final List<Edge<DataObject>> edges1, final List<Edge<DataObject>> edges2,
    final List<EdgePair<DataObject>> pairedEdges, final boolean reversed) {
    final Iterator<Edge<DataObject>> edgeIter1 = edges1.iterator();
    while (edgeIter1.hasNext()) {
      final Edge<DataObject> edge1 = edgeIter1.next();
      boolean matched = false;
      final Iterator<Edge<DataObject>> edgeIter2 = edges2.iterator();
      while (!matched && edgeIter2.hasNext()) {
        final Edge<DataObject> edge2 = edgeIter2.next();
        boolean match = false;
        if (edge1.isForwards(node) == edge2.isForwards(node)) {
          match = reversed;
        } else {
          match = !reversed;
        }
        if (match) {
          final EdgePair<DataObject> edgePair = createEdgePair(edge1, edge2);
          if (edgePair != null) {
            matched = true;
            edgeIter1.remove();
            edgeIter2.remove();
            pairedEdges.add(edgePair);
          }
        }
      }
    }
  }
}
