package com.revolsys.geometry.model;

import java.awt.geom.AffineTransform;

import com.revolsys.geometry.model.vertex.Vertex;

public class VertexPathIteratorTransform extends VertexPathIterator {
  private final AffineTransform transform;

  public VertexPathIteratorTransform(final Vertex vertex, final AffineTransform transform) {
    super(vertex);
    this.transform = transform;
  }

  @Override
  public int currentSegment(final double[] coordinates) {
    final int type = super.currentSegment(coordinates);
    this.transform.transform(coordinates, 0, coordinates, 0, 1);
    return type;
  }

  @Override
  public int currentSegment(final float[] coordinates) {
    final int type = super.currentSegment(coordinates);
    this.transform.transform(coordinates, 0, coordinates, 0, 1);
    return type;
  }
}
