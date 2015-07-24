package com.revolsys.gis.graph.linemerge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.revolsys.collection.list.Lists;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Node;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineString;
import com.revolsys.util.Property;

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

  private final LineStringsGraph graph = new LineStringsGraph();

  private boolean merged = true;

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
    this.merged = false;
    this.graph.addEdge(lineString);
  }

  public LineStringsGraph getGraph() {
    return this.graph;
  }

  /**
   * Gets the {@link LineString}s created by the merging process.
   *
   * @return the collection of merged LineStrings
   */
  public List<LineString> getMergedLineStrings() {
    merge();
    return this.graph.getEdgeLines();
  }

  private void merge() {
    if (!this.merged) {
      mergeDegree2();
      this.merged = true;
    }
  }

  private void mergeDegree2() {
    for (final Node<LineString> node : this.graph.getNodes((node) -> {
      return !node.isRemoved() && node.getDegree() != 2;
    })) {
      if (!node.isRemoved()) {
        for (final Edge<LineString> edge : Lists.array(node.getEdges())) {
          if (!edge.isRemoved()) {
            final List<LineString> lines = new ArrayList<>();
            final List<Boolean> lineForwards = new ArrayList<>();
            double forwardsLength = 0;
            double reverseLength = 0;
            Edge<LineString> previousEdge = null;
            Node<LineString> currentNode = node;
            Edge<LineString> currentEdge = edge;
            do {
              final LineString line = currentEdge.getLine();
              final double length = line.getLength();
              final boolean forwards = currentEdge.isForwards(currentNode);
              if (forwards) {
                forwardsLength += length;
              } else {
                reverseLength += length;
              }
              lines.add(line);
              lineForwards.add(forwards);
              currentNode = currentEdge.getOppositeNode(currentNode);
              previousEdge = currentEdge;
              currentEdge = currentNode.getNextEdge(previousEdge);
            } while (currentNode.getDegree() == 2);
            if (lines.size() > 1) {
              LineString mergedLine = null;
              final boolean mergeForwards = forwardsLength >= reverseLength;
              int i = 0;
              for (LineString line : lines) {
                final boolean forwards = lineForwards.get(i);
                if (forwards != mergeForwards) {
                  line = line.reverse();
                }
                if (mergedLine == null) {
                  mergedLine = line;
                } else {
                  mergedLine = mergedLine.merge(line);
                }
                i++;
              }
              this.graph.addEdge(mergedLine);
              this.graph.removeEdges(lines);
            }
          }
        }
      }
    }
  }

  public void remove(final LineString lineString) {
    this.merged = false;
    this.graph.removeEdge(lineString);
  }

  public void remove(final List<LineString> lines) {
    this.merged = false;
    this.graph.removeEdges(lines);
  }
}
