package com.revolsys.gis.graph.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.graph.Node;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.Point;

public class NodeGeometryIntersectionFilter<T> implements Filter<Node<T>> {

  private com.revolsys.jts.geom.GeometryFactory geometryFactory;

  private Geometry preparedGeometry;

  public NodeGeometryIntersectionFilter() {
  }

  public NodeGeometryIntersectionFilter(final Geometry geometry) {
    setGeometry(geometry);
  }

  @Override
  public boolean accept(final Node<T> node) {
    final Point coordinates = node;
    final Point point = geometryFactory.point(coordinates);
    final boolean intersects = preparedGeometry.intersects(point);
    return intersects;
  }

  public void setGeometry(final Geometry geometry) {
    this.preparedGeometry = geometry.prepare();
    this.geometryFactory = geometry.getGeometryFactory();
  }
}
