package com.revolsys.gis.graph.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.graph.Node;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.prep.PreparedGeometry;
import com.revolsys.jts.geom.prep.PreparedGeometryFactory;

public class NodeGeometryIntersectionFilter<T> implements Filter<Node<T>> {

  private com.revolsys.jts.geom.GeometryFactory geometryFactory;

  private PreparedGeometry preparedGeometry;

  public NodeGeometryIntersectionFilter() {
  }

  public NodeGeometryIntersectionFilter(final Geometry geometry) {
    setGeometry(geometry);
  }

  @Override
  public boolean accept(final Node<T> node) {
    final Coordinates coordinates = node;
    final Point point = geometryFactory.point(coordinates);
    final boolean intersects = preparedGeometry.intersects(point);
    return intersects;
  }

  public void setGeometry(final Geometry geometry) {
    this.preparedGeometry = PreparedGeometryFactory.prepare(geometry);
    this.geometryFactory = GeometryFactory.getFactory(geometry);
  }
}
