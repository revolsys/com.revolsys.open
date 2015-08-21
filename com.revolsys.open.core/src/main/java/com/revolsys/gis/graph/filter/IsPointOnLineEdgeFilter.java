package com.revolsys.gis.graph.filter;

import java.util.function.Predicate;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.jts.LineStringUtil;

public class IsPointOnLineEdgeFilter<T> implements Predicate<Node<T>> {

  private final Edge<T> edge;

  private BoundingBox envelope;

  private final double maxDistance;

  public IsPointOnLineEdgeFilter(final Edge<T> edge, final double maxDistance) {
    this.edge = edge;
    this.maxDistance = maxDistance;
    this.envelope = edge.getBoundingBox();
    this.envelope = this.envelope.expand(maxDistance);
  }

  public com.revolsys.geometry.model.BoundingBox getEnvelope() {
    return this.envelope;
  }

  @Override
  public boolean test(final Node<T> node) {
    final LineString line = this.edge.getLine();
    if (!this.edge.hasNode(node)) {
      if (this.envelope.intersects(new BoundingBoxDoubleGf(node))) {
        if (LineStringUtil.isPointOnLine(line, node, this.maxDistance)) {
          return true;
        }
      }
    }
    return false;
  }

}
