package com.revolsys.gis.graph.attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.property.DirectionalAttributes;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.EdgePair;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.jts.LineStringUtil;
import com.revolsys.gis.util.NoOp;
import com.vividsolutions.jts.geom.LineString;

public class PseudoNodeAttribute {
  private final Set<String> equalExcludeAttributes = new HashSet<String>();

  private final List<EdgePair<DataObject>> edgePairs = new ArrayList<EdgePair<DataObject>>();

  private final List<EdgePair<DataObject>> reversedEdgePairs = new ArrayList<EdgePair<DataObject>>();

  private final QName typeName;

  public PseudoNodeAttribute(final Node<DataObject> node, final QName typeName,
    final Collection<String> equalExcludeAttributes) {
    this.typeName = typeName;
    this.equalExcludeAttributes.addAll(equalExcludeAttributes);
    final Map<QName, Map<LineString, Set<Edge<DataObject>>>> edgesByTypeNameAndLine = NodeAttributes.getEdgesByTypeNameAndLine(node);
    final Map<LineString, Set<Edge<DataObject>>> edgesByLine = edgesByTypeNameAndLine.get(typeName);
    init(node, edgesByLine);
  }

  // TODO add node to can merge for loops!
  private EdgePair<DataObject> createEdgePair(Node<DataObject> node, final Edge<DataObject> edge1,
    final Edge<DataObject> edge2) {
    final DataObject object1 = edge1.getObject();
    final DataObject object2 = edge2.getObject();
    if (DirectionalAttributes.canMergeObjects(node, object1, object2,
      equalExcludeAttributes)) {
      return new EdgePair<DataObject>(edge1, edge2);
    } else {
      return null;
    }
  }

  public List<EdgePair<DataObject>> getEdgePairs() {
    return edgePairs;
  }

  public List<EdgePair<DataObject>> getReversedEdgePairs() {
    return reversedEdgePairs;
  }

  public QName getTypeName() {
    return typeName;
  }

  private void init(final Node<DataObject> node,
    final Map<LineString, Set<Edge<DataObject>>> edgesByLine) {
    if (isPseudoNode(node, edgesByLine)) {

    }
  }

  private boolean isPseudoNode(final Node<DataObject> node,
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
            final EdgePair<DataObject> edgePair = createEdgePair(node, edge1, edge2);
            if (edgePair != null) {
              if (edge1.isForwards(node) == edge2.isForwards(node)) {
                reversedEdgePairs.add(edgePair);
              } else {
                edgePairs.add(edgePair);
              }
              return true;
            }
          } else {
            final List<Edge<DataObject>> unmatchedEdges1 = new ArrayList<Edge<DataObject>>(
              edges1);
            final List<Edge<DataObject>> unmatchedEdges2 = new ArrayList<Edge<DataObject>>(
              edges2);
            // Find non-reversed matches
            matchEdges(node, unmatchedEdges1, unmatchedEdges2, edgePairs, false);
            if (unmatchedEdges2.isEmpty()) {
              return true;
            } else {
              // Find reversed matches
              matchEdges(node, unmatchedEdges1, unmatchedEdges2,
                reversedEdgePairs, true);
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
          final EdgePair<DataObject> edgePair = createEdgePair(node,edge1, edge2);
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
