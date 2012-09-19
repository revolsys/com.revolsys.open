package com.revolsys.gis.graph;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.LineString;

public class EdgeLineList extends AbstractList<LineString> {

  private final Graph<?> graph;

  private List<Integer> edgeIds;

  public EdgeLineList(final Graph<?> graph) {
    this(graph, new ArrayList<Integer>());
  }

  public EdgeLineList(final Graph<?> graph, final List<Integer> edgeIds) {
    this.graph = graph;
    this.edgeIds = edgeIds;
  }


  @Override
  public LineString get(final int index) {
    final Integer edgeId = edgeIds.get(index);
    return graph.getEdgeLine(edgeId);
  }


  @Override
  public int size() {
    return edgeIds.size();
  }

}
