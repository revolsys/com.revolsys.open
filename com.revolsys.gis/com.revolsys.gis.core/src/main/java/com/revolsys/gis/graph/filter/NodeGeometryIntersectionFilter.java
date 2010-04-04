package com.revolsys.gis.graph.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.graph.Node;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;

public class NodeGeometryIntersectionFilter<T> implements Filter<Node<T>> {

  private final Geometry geometry;

  private final PreparedGeometry preparedGeometry;

  public NodeGeometryIntersectionFilter(
    final Geometry geometry) {
    this.geometry = geometry;
    this.preparedGeometry = PreparedGeometryFactory.prepare(geometry);
  }

  public boolean accept(
    final Node<T> node) {
    final GeometryFactory factory = geometry.getFactory();
    final Coordinate coordinate = node.getCoordinate();
    final Point point = factory.createPoint(coordinate);
    final boolean intersects = preparedGeometry.intersects(point);
    return intersects;
  }
}
