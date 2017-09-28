package com.revolsys.geometry.model.editor;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.vertex.Vertex;

public interface GeometryEditor<GE extends GeometryEditor<?>> extends Geometry {
  GeometryEditor<?> appendVertex(int[] geometryId, Point point);

  GeometryEditor<?> deleteVertex(int[] vertexId);

  Iterable<GE> editors();

  GeometryEditor<?> insertVertex(int[] vertexId, Point newPoint);

  boolean isModified();

  default GeometryEditor<?> move(final double deltaX, final double deltaY) {
    for (final Vertex vertex : vertices()) {
      double x = vertex.getX();
      x += deltaX;
      vertex.setX(x);

      double y = vertex.getY();
      y += deltaY;
      vertex.setY(y);
    }
    return this;
  }

  Geometry newGeometry();

  GeometryEditor<?> setAxisCount(int axisCount);

  GeometryEditor<?> setCoordinate(int[] vertexId, int axisIndex, double coordinate);

  GeometryEditor<?> setM(int[] vertexId, double m);

  GeometryEditor<?> setVertex(int[] vertexId, Point newPoint);

  GeometryEditor<?> setX(int[] vertexId, double x);

  GeometryEditor<?> setY(int[] vertexId, double y);

  GeometryEditor<?> setZ(int[] vertexId, double z);
}
