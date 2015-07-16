package com.revolsys.gis.algorithm.linematch;

import java.util.function.Predicate;

import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Node;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;

public class LineSegmentMatchWithinDistanceFilter implements Predicate<Edge<LineSegmentMatch>> {
  private BoundingBox boundingBox;

  private final double maxDistance;

  private final Node<LineSegmentMatch> node;

  public LineSegmentMatchWithinDistanceFilter(final Node<LineSegmentMatch> node,
    final double maxDistance) {
    this.node = node;
    this.maxDistance = maxDistance;
    this.boundingBox = new BoundingBoxDoubleGf(node);
    this.boundingBox = this.boundingBox.expand(maxDistance);
  }

  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  @Override
  public boolean test(final Edge<LineSegmentMatch> edge) {
    if (!edge.hasNode(this.node) && edge.distance(this.node) < this.maxDistance) {
      return true;
    } else {
      return false;
    }
  }
}
