package com.revolsys.geometry.model.vertex;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.impl.AbstractPoint;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.io.IteratorReader;
import com.revolsys.io.Reader;

public abstract class AbstractVertex extends AbstractPoint implements Vertex {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  protected final Geometry geometry;

  public AbstractVertex(final Geometry geometry) {
    this.geometry = geometry;
  }

  @Override
  public AbstractVertex clone() {
    return (AbstractVertex)super.clone();
  }

  @Override
  public int getAxisCount() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return (byte)geometryFactory.getAxisCount();
  }

  @Override
  public BoundingBox getBoundingBox() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return new BoundingBoxDoubleGf(geometryFactory, this);
  }

  @Override
  public double getCoordinate(final int axisIndex) {
    return 0;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> V getGeometry() {
    return (V)this.geometry;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometry.getGeometryFactory();
  }

  @Override
  public Vertex getLineNext() {
    return null;
  }

  @Override
  public Vertex getLinePrevious() {
    return null;
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
  public boolean isFrom() {
    return false;
  }

  @Override
  public boolean isTo() {
    return false;
  }

  @Override
  public Reader<Vertex> reader() {
    return new IteratorReader<Vertex>(this);
  }
}
