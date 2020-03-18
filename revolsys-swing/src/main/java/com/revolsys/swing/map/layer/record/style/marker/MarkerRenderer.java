package com.revolsys.swing.map.layer.record.style.marker;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.io.BaseCloseable;

public interface MarkerRenderer extends BaseCloseable {

  void renderMarker(double modelX, double modelY, double orientation);

  void renderMarkerGeometry(Geometry geometry);

  default void renderMarkerPoint(final Point point) {
    final double modelX = point.getX();
    final double modelY = point.getY();
    renderMarker(modelX, modelY, 0);
  }

  default void renderMarkerPoint(final Point point, final double orientation) {
    final double x = point.getX();
    final double y = point.getY();
    renderMarker(x, y, orientation);
  }

  default void renderMarkers(final Iterable<? extends Point> points) {
    for (final Point point : points) {
      final double modelX = point.getX();
      final double modelY = point.getY();
      renderMarker(modelX, modelY, 0);
    }
  }

  void renderMarkerSegments(Geometry geometry);

  default void renderMarkerVertex(final Vertex vertex) {
    final double orientation = vertex.getOrientaton();
    renderMarkerPoint(vertex, orientation);
  }

  void renderMarkerVertices(Geometry geometry);

}
