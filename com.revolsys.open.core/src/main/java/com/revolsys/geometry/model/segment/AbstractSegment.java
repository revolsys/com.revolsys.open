package com.revolsys.geometry.model.segment;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.IteratorReader;
import com.revolsys.io.Reader;

public abstract class AbstractSegment extends AbstractLineSegment implements Segment {
  private static final long serialVersionUID = 1L;

  protected final Geometry geometry;

  public AbstractSegment(final Geometry geometry) {
    this.geometry = geometry;
  }

  @Override
  public AbstractSegment clone() {
    return (AbstractSegment)super.clone();
  }

  @Override
  public int getAxisCount() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return (byte)geometryFactory.getAxisCount();
  }

  @Override
  public double[] getCoordinates() {
    final int axisCount = getAxisCount();
    final double[] coordinates = new double[axisCount * 2];
    for (int vertexIndex = 0; vertexIndex < 2; vertexIndex++) {
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        coordinates[vertexIndex * axisCount + axisIndex] = getCoordinate(vertexIndex, axisIndex);
      }
    }
    return coordinates;
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
  public int getPartIndex() {
    return -1;
  }

  @Override
  public int getRingIndex() {
    return -1;
  }

  @Override
  public int getSegmentIndex() {
    final int[] vertexId = getSegmentId();
    return vertexId[vertexId.length - 1];
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public Reader<Segment> reader() {
    return new IteratorReader<Segment>(this);
  }

}
