package com.revolsys.geometry.model.vertex;

import java.awt.geom.PathIterator;
import java.util.Iterator;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryComponent;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;

public interface Vertex extends Point, Iterator<Vertex>, Iterable<Vertex>, GeometryComponent {

  @Override
  Vertex clone();

  default int getAwtType() {
    if (isFrom()) {
      return PathIterator.SEG_MOVETO;
    } else {
      return PathIterator.SEG_LINETO;
    }
  }

  @Override
  default int getAxisCount() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return (byte)geometryFactory.getAxisCount();
  }

  @Override
  default BoundingBox getBoundingBox() {
    return newBoundingBox();
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

  @Override
  default Iterator<Vertex> iterator() {
    return this;
  }
}
