package com.revolsys.geometry.graph.visitor;

import java.util.function.Predicate;

import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;

public class EdgeWithinDistanceFilter<T> implements Predicate<Edge<T>> {

  private final Geometry geometry;

  private final double maxDistance;

  public EdgeWithinDistanceFilter(final Geometry geometry, final double maxDistance) {
    this.geometry = geometry;
    this.maxDistance = maxDistance;
  }

  @Override
  public boolean test(final Edge<T> edge) {
    final LineString line = edge.getLine();
    final double distance = line.distance(this.geometry);
    if (distance <= this.maxDistance) {
      return true;
    } else {
      return false;
    }
  }
}
