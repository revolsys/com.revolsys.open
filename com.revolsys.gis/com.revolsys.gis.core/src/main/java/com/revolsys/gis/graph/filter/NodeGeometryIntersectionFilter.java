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

  private GeometryFactory geometryFactory;

  private PreparedGeometry preparedGeometry;

  public NodeGeometryIntersectionFilter() {
  }

  public NodeGeometryIntersectionFilter(final Geometry geometry) {
    setGeometry(geometry);
  }

  public boolean accept(final Node<T> node) {
    final Coordinates coordinates = node;
    final Point point = geometryFactory.createPoint(coordinates);
    final boolean intersects = preparedGeometry.intersects(point);
    return intersects;
  }

  public void setGeometry(final Geometry geometry) {
    this.preparedGeometry = PreparedGeometryFactory.prepare(geometry);
    this.geometryFactory = GeometryFactory.getFactory(geometry);
  }
}
