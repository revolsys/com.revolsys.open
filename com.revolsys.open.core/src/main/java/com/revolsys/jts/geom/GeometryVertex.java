package com.revolsys.jts.geom;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.gis.model.coordinates.AbstractCoordinates;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.geometry.util.GeometryEditUtil;

/**
 * @author Paul Austin <paul.austin@revolsys.com>
 */
public class GeometryVertex extends AbstractCoordinates {

  private final Geometry geometry;

  private Coordinates coordinates;

  private int[] vertexId;

  public GeometryVertex(final Geometry geometry, final int... vertexId) {
    this.geometry = geometry;
    setVertexId(vertexId);
  }

  @Override
  public GeometryVertex clone() {
    return (GeometryVertex)super.clone();
  }

  @Override
  public BoundingBox getBoundingBox() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return new BoundingBox(geometryFactory, this);
  }

  public Geometry getGeometry() {
    return geometry;
  }

  public GeometryFactory getGeometryFactory() {
    return geometry.getGeometryFactory();
  }

  @Override
  public byte getNumAxis() {
    final com.revolsys.jts.geom.GeometryFactory geometryFactory = getGeometryFactory();
    return (byte)geometryFactory.getNumAxis();
  }

  @Override
  public int getSrid() {
    final com.revolsys.jts.geom.GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.getSrid();
  }

  @Override
  public double getValue(final int index) {
    if (coordinates == null) {
      return Double.NaN;
    } else {
      return coordinates.getValue(index);
    }
  }

  public int[] getVertexId() {
    return vertexId;
  }

  @Override
  public void setValue(final int index, final double value) {
    throw new UnsupportedOperationException("Cannot modify a geometry vertex");
  }

  void setVertexId(final int... vertexId) {
    this.vertexId = vertexId;
    this.coordinates = GeometryEditUtil.getVertex(geometry, vertexId);
  }

  public Point toPoint() {
    final com.revolsys.jts.geom.GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.createPoint(this);
  }

}
