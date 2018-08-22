package com.revolsys.geometry.model.awtshape;

import java.awt.geom.AffineTransform;

import com.revolsys.geometry.model.LineString;

public class LineStringPathIteratorTransform extends LineStringPathIterator {
  private final AffineTransform transform;

  public LineStringPathIteratorTransform(final LineString line, final AffineTransform transform) {
    super(line);
    this.transform = transform;
  }

  @Override
  public int currentSegment(final double[] coordinates) {
    final LineString line = this.line;
    final int vertexIndex = this.vertexIndex;
    final double[] currentCoordinates = this.currentCoordinates;
    line.copyPoint(vertexIndex, 2, currentCoordinates);
    this.transform.transform(currentCoordinates, 0, coordinates, 0, 1);
    return this.awtType;
  }

  @Override
  public int currentSegment(final float[] coordinates) {
    final LineString line = this.line;
    final int vertexIndex = this.vertexIndex;
    final double[] currentCoordinates = this.currentCoordinates;
    line.copyPoint(vertexIndex, 2, currentCoordinates);
    this.transform.transform(currentCoordinates, 0, coordinates, 0, 1);
    return this.awtType;
  }

}
