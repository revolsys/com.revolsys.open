package com.revolsys.gis.graph.filter;

import java.util.function.Predicate;

import com.revolsys.gis.graph.Node;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;

public class NodeGeometryIntersectionFilter<T> implements Predicate<Node<T>> {

  private GeometryFactory geometryFactory;

  private Geometry preparedGeometry;

  public NodeGeometryIntersectionFilter() {
  }

  public NodeGeometryIntersectionFilter(final Geometry geometry) {
    setGeometry(geometry);
  }

  public void setGeometry(final Geometry geometry) {
    this.preparedGeometry = geometry.prepare();
    this.geometryFactory = geometry.getGeometryFactory();
  }

  @Override
  public boolean test(final Node<T> node) {
    final Point coordinates = node;
    final Point point = this.geometryFactory.point(coordinates);
    final boolean intersects = this.preparedGeometry.intersects(point);
    return intersects;
  }
}
