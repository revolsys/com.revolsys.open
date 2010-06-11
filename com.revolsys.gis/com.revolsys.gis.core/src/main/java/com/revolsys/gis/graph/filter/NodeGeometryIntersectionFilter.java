package com.revolsys.gis.graph.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;

public class NodeGeometryIntersectionFilter<T> implements Filter<Node<T>> {

  private final Geometry geometry;

  private final GeometryFactory geometryFactory;

  private final PreparedGeometry preparedGeometry;

  public NodeGeometryIntersectionFilter(
    final Geometry geometry) {
    this.geometry = geometry;
    this.preparedGeometry = PreparedGeometryFactory.prepare(geometry);
    this.geometryFactory = GeometryFactory.getFactory(geometry);
  }

  public boolean accept(
    final Node<T> node) {
    final Coordinates coordinates = node.getCoordinates();
    final Point point = geometryFactory.createPoint(coordinates);
    final boolean intersects = preparedGeometry.intersects(point);
    return intersects;
  }
}
