package com.revolsys.geometry.model;

import java.awt.geom.AffineTransform;

import com.revolsys.geometry.model.vertex.Vertex;

public class VertexPathIteratorTransform extends VertexPathIterator {
  private final AffineTransform transform;

  /** Shared buffer for float transforms. */
  private final double[] currentCoordinates = new double[2];

  public VertexPathIteratorTransform(final Vertex vertex, final AffineTransform transform) {
    super(vertex);
    this.transform = transform;
  }

  @Override
  public int currentSegment(final double[] coordinates) {
    final int awtType = this.vertex.getAwtType();
    if (awtType != SEG_CLOSE) {
      final double x = this.vertex.getX();
      final double y = this.vertex.getY();
      this.currentCoordinates[0] = x;
      this.currentCoordinates[1] = y;
      this.transform.transform(this.currentCoordinates, 0, coordinates, 0, 1);
    }
    return awtType;
  }

  @Override
  public int currentSegment(final float[] coordinates) {
    final int awtType = this.vertex.getAwtType();
    if (awtType != SEG_CLOSE) {
      // Uses a double[] to avoid rounding errors in transform
      final double x = this.vertex.getX();
      final double y = this.vertex.getY();
      this.currentCoordinates[0] = x;
      this.currentCoordinates[1] = y;

      this.transform.transform(this.currentCoordinates, 0, coordinates, 0, 1);
    }
    return awtType;
  }
}
