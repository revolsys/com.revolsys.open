package com.revolsys.swing.map.layer.record.renderer.shape;

import java.awt.geom.AffineTransform;

import com.revolsys.geometry.model.LineString;

public class LineStringPathIteratorTransform extends LineStringPathIterator {
  private AffineTransform transform;

  public LineStringPathIteratorTransform() {
  }

  public LineStringPathIteratorTransform(final LineString line, final AffineTransform transform) {
    reset(line, transform);
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

  public LineStringPathIteratorTransform reset(final LineString line,
    final AffineTransform transform) {
    reset(line);
    this.transform = transform;
    return this;
  }

}
