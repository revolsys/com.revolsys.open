package com.revolsys.geometry.model.vertex;

import java.util.Iterator;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryComponent;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.io.IteratorReader;
import com.revolsys.io.Reader;

public interface Vertex extends Point, Iterator<Vertex>, GeometryComponent {

  @Override
  Vertex clone();

  @Override
  default int getAxisCount() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return (byte)geometryFactory.getAxisCount();
  }

  @Override
  default BoundingBox getBoundingBox() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return new BoundingBoxDoubleGf(geometryFactory, this);
  }

  @Override
  default double getCoordinate(final int axisIndex) {
    return 0;
  }

  <V extends Geometry> V getGeometry();

  default Vertex getLineNext() {
    return null;
  }

  default Vertex getLinePrevious() {
    return null;
  }

  default int getPartIndex() {
    return -1;
  }

  default int getRingIndex() {
    return -1;
  }

  int[] getVertexId();

  default int getVertexIndex() {
    final int[] vertexId = getVertexId();
    return vertexId[vertexId.length - 1];
  }

  @Override
  default boolean isEmpty() {
    return false;
  }

  default boolean isFrom() {
    return false;
  }

  default boolean isTo() {
    return false;
  }

  default Reader<Vertex> reader() {
    return new IteratorReader<Vertex>(this);
  }
}
