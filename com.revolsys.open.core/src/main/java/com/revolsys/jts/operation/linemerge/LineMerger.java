/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.jts.operation.linemerge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.revolsys.collection.list.Lists;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.planargraph.GraphComponent;
import com.revolsys.jts.planargraph.Node;
import com.revolsys.jts.util.Assert;
import com.revolsys.util.Property;

/**
 * Merges a collection of linear components to form maximal-length linestrings.
 * <p>
 * Merging stops at nodes of degree 1 or degree 3 or more.
 * In other words, all nodes of degree 2 are merged together.
 * The exception is in the case of an isolated loop, which only has degree-2 nodes.
 * In this case one of the nodes is chosen as a starting point.
 * <p>
 * The direction of each
 * merged LineString will be that of the majority of the LineStrings from which it
 * was derived.
 * <p>
 * Any dimension of Geometry is handled - the constituent linework is extracted to
 * form the edges. The edges must be correctly noded; that is, they must only meet
 * at their endpoints.  The LineMerger will accept non-noded input
 * but will not merge non-noded edges.
 * <p>
 * Input lines which are empty or contain only a single unique coordinate are not included
 * in the merging.
 *
 * @version 1.7
 */
public class LineMerger {

  public static List<LineString> merge(final Collection<? extends LineString> lines) {
    if (Property.hasValue(lines)) {
      if (lines.size() == 1) {
        return Lists.array(lines);
      } else {
        final LineMerger lineMerger = new LineMerger(lines);
        return lineMerger.getMergedLineStrings();
      }
    }
    return Collections.emptyList();
  }

  private final LineMergeGraph graph = new LineMergeGraph();

  private List<LineString> mergedLineStrings = null;

  private GeometryFactory factory = null;

  private List<EdgeString> edgeStrings = null;

  /**
   * Creates a new line merger.
   *
   */
  public LineMerger() {

  }

  public LineMerger(final Collection<? extends LineString> lines) {
    add(lines);
  }

  /**
   * Adds a collection of Geometries to be processed. May be called multiple times.
   * Any dimension of Geometry may be added; the constituent linework will be
   * extracted.
   *
   * @param geometries the geometries to be line-merged
   */
  public void add(final Collection<? extends Geometry> geometries) {
    this.mergedLineStrings = null;
    for (final Geometry geometry : geometries) {
      add(geometry);
    }
  }

  /**
   * Adds a Geometry to be processed. May be called multiple times.
   * Any dimension of Geometry may be added; the constituent linework will be
   * extracted.
   *
   * @param geometry geometry to be line-merged
   */
  public void add(final Geometry geometry) {
    for (final LineString line : geometry.getGeometryComponents(LineString.class)) {
      add(line);
    }
  }

  private void add(final LineString lineString) {
    if (this.factory == null) {
      this.factory = lineString.getGeometryFactory();
    }
    this.graph.addEdge(lineString);
  }

  private void buildEdgeStringsForIsolatedLoops() {
    buildEdgeStringsForUnprocessedNodes();
  }

  private void buildEdgeStringsForNonDegree2Nodes() {
    for (final Iterator i = this.graph.getNodes().iterator(); i.hasNext();) {
      final Node node = (Node)i.next();
      if (node.getDegree() != 2) {
        buildEdgeStringsStartingAt(node);
        node.setMarked(true);
      }
    }
  }

  private void buildEdgeStringsForObviousStartNodes() {
    buildEdgeStringsForNonDegree2Nodes();
  }

  private void buildEdgeStringsForUnprocessedNodes() {
    for (final Iterator i = this.graph.getNodes().iterator(); i.hasNext();) {
      final Node node = (Node)i.next();
      if (!node.isMarked()) {
        Assert.isTrue(node.getDegree() == 2);
        buildEdgeStringsStartingAt(node);
        node.setMarked(true);
      }
    }
  }

  private void buildEdgeStringsStartingAt(final Node node) {
    for (final Iterator i = node.getOutEdges().iterator(); i.hasNext();) {
      final LineMergeDirectedEdge directedEdge = (LineMergeDirectedEdge)i.next();
      if (directedEdge.getEdge().isMarked()) {
        continue;
      }
      this.edgeStrings.add(buildEdgeStringStartingWith(directedEdge));
    }
  }

  private EdgeString buildEdgeStringStartingWith(final LineMergeDirectedEdge start) {
    final EdgeString edgeString = new EdgeString(this.factory);
    LineMergeDirectedEdge current = start;
    do {
      edgeString.add(current);
      current.getEdge().setMarked(true);
      current = current.getNext();
    } while (current != null && current != start);
    return edgeString;
  }

  /**
   * Gets the {@link LineString}s created by the merging process.
   *
   * @return the collection of merged LineStrings
   */
  public List<LineString> getMergedLineStrings() {
    merge();
    return this.mergedLineStrings;
  }

  private void merge() {
    if (this.mergedLineStrings != null) {
      return;
    }

    // reset marks (this allows incremental processing)
    GraphComponent.setMarked(this.graph.nodeIterator(), false);
    GraphComponent.setMarked(this.graph.edgeIterator(), false);

    this.edgeStrings = new ArrayList<EdgeString>();
    buildEdgeStringsForObviousStartNodes();
    buildEdgeStringsForIsolatedLoops();
    this.mergedLineStrings = new ArrayList<LineString>();
    for (final EdgeString edgeString : this.edgeStrings) {
      final LineString line = edgeString.toLineString();
      this.mergedLineStrings.add(line);
    }
  }
}
