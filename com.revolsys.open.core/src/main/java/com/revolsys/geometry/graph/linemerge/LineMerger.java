package com.revolsys.geometry.graph.linemerge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import com.revolsys.collection.list.Lists;
import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.util.Property;

public class LineMerger {

  public static List<LineString> merge(final Geometry... geometries) {
    if (geometries == null) {
      return Collections.emptyList();
    } else {
      final LineMerger lineMerger = new LineMerger(geometries);
      return lineMerger.getLineStrings();
    }
  }

  public static List<LineString> merge(final Iterable<? extends Geometry> geometries) {
    if (Property.hasValue(geometries)) {
      final LineMerger lineMerger = new LineMerger(geometries);
      return lineMerger.getLineStrings();
    } else {
      return Collections.emptyList();
    }
  }

  private final LineStringsGraph graph = new LineStringsGraph();

  private boolean merged = true;

  /**
   * Creates a new line merger.
   *
   */
  public LineMerger() {
  }

  public LineMerger(final Geometry... geometries) {
    addAll(geometries);
  }

  public LineMerger(final Iterable<? extends Geometry> lines) {
    addAll(lines);
  }

  public LineMerger(final LineString line) {
    add(line);
  }

  /**
   * Adds a Geometry to be processed. May be called multiple times.
   * Any dimension of Geometry may be added; the constituent linework will be
   * extracted.
   *
   * @param geometry geometry to be line-merged
   */
  public void add(final Geometry geometry) {
    final List<LineString> lines = geometry.getGeometryComponents(LineString.class);
    for (final LineString line : lines) {
      add(line);
    }
  }

  public void add(final LineString lineString) {
    if (lineString != null) {
      if (this.graph.getGeometryFactory() == GeometryFactory.DEFAULT_3D) {
        this.graph.setGeometryFactory(lineString.getGeometryFactory());
      }
      this.merged = false;
      this.graph.addEdge(lineString);
    }
  }

  public void addAll(final Geometry... geometries) {
    for (final Geometry geometry : geometries) {
      add(geometry);
    }
  }

  /**
   * Adds a collection of Geometries to be processed. May be called multiple times.
   * Any dimension of Geometry may be added; the constituent linework will be
   * extracted.
   *
   * @param geometries the geometries to be line-merged
   */
  public void addAll(final Iterable<? extends Geometry> geometries) {
    for (final Geometry geometry : geometries) {
      add(geometry);
    }
  }

  public LineStringsGraph getGraph() {
    return this.graph;
  }

  public Lineal getLineal() {
    final GeometryFactory geometryFactory = this.graph.getGeometryFactory();
    return getLineal(geometryFactory);
  }

  public Lineal getLineal(final GeometryFactory geometryFactory) {
    final List<LineString> lines = getLineStrings();
    return geometryFactory.lineal(lines);
  }

  /**
   * Gets the {@link LineString}s created by the merging process.
   *
   * @return the collection of merged LineStrings
   */
  public List<LineString> getLineStrings() {
    merge();
    final List<LineString> edgeLines = this.graph.getEdgeLines();
    return Lists.toArray(edgeLines);
  }

  private void merge() {
    if (!this.merged) {
      mergeDegree2FromEnds();
      this.merged = true;
    }
  }

  private void mergeDegree2FromEnds() {
    final Predicate<Node<LineString>> filter = (node) -> {
      return !node.isRemoved() && node.getDegree() != 2;
    };
    for (final Node<LineString> node : this.graph.getNodes(filter)) {
      if (!node.isRemoved()) {
        for (final Edge<LineString> edge : Lists.toArray(node.getEdges())) {
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
              final boolean forwards = currentEdge.getEnd(currentNode).isFrom();
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

  public void removeAll(final Iterable<LineString> lines) {
    this.merged = false;
    this.graph.removeEdges(lines);
  }
}
