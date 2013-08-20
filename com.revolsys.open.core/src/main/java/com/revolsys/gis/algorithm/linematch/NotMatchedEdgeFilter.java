package com.revolsys.gis.algorithm.linematch;

import com.revolsys.filter.Filter;
import com.revolsys.gis.graph.Edge;

public class NotMatchedEdgeFilter implements Filter<Edge<LineSegmentMatch>> {
  private final int index;

  public NotMatchedEdgeFilter(final int index) {
    this.index = index;
  }

  @Override
  public boolean accept(final Edge<LineSegmentMatch> edge) {
    return edge.getObject().hasMatches(index);
  }

}
