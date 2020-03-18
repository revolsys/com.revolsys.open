package com.revolsys.swing.map.layer.record.renderer.shape;

import java.awt.geom.AffineTransform;

import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Polygon;

public class PolygonPathIteratorTransform extends PolygonPathIterator {
  private final AffineTransform transform;

  public PolygonPathIteratorTransform(final Polygon polygon, final AffineTransform transform) {
    super(polygon);
    this.transform = transform;
  }

  @Override
  public int currentSegment(final double[] coordinates) {
    final LineString line = this.ring;
    final int vertexIndex = this.vertexIndex;
    final double[] currentCoordinates = this.currentCoordinates;
    line.copyPoint(vertexIndex, 2, currentCoordinates);
    this.transform.transform(currentCoordinates, 0, coordinates, 0, 1);
    return this.awtType;
  }

  @Override
  public int currentSegment(final float[] coordinates) {
    final LineString line = this.ring;
    final int vertexIndex = this.vertexIndex;
    final double[] currentCoordinates = this.currentCoordinates;
    line.copyPoint(vertexIndex, 2, currentCoordinates);
    this.transform.transform(currentCoordinates, 0, coordinates, 0, 1);
    return this.awtType;
  }

}
