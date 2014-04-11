package com.revolsys.jts.geom;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.model.coordinates.AbstractCoordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;

public abstract class AbstractVertex extends AbstractCoordinates implements
  Vertex {

  protected final Geometry geometry;

  public AbstractVertex(final Geometry geometry) {
    this.geometry = geometry;
  }

  @Override
  public AbstractVertex clone() {
    return (AbstractVertex)super.clone();
  }

  @Override
  public DoubleCoordinates cloneCoordinates() {
    return new DoubleCoordinates(this);
  }

  @Override
  public BoundingBox getBoundingBox() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return new BoundingBox(geometryFactory, this);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> V getGeometry() {
    return (V)geometry;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return geometry.getGeometryFactory();
  }

  @Override
  public byte getNumAxis() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return (byte)geometryFactory.getNumAxis();
  }

  @Override
  public int getPartIndex() {
    return -1;
  }

  @Override
  public int getRingIndex() {
    return -1;
  }

  @Override
  public int getSrid() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.getSrid();
  }

  @Override
  public abstract int[] getVertexId();

  @Override
  public int getVertexIndex() {
    final int[] vertexId = getVertexId();
    return vertexId[vertexId.length - 1];
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public final void setValue(final int index, final double value) {
    throw new UnsupportedOperationException("Cannot modify a geometry vertex");
  }

  @Override
  public Point toPoint() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.createPoint(this);
  }

}
