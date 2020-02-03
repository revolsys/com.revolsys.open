package com.revolsys.swing.map.layer.record.renderer.shape;

import java.awt.geom.PathIterator;

import com.revolsys.geometry.model.LineString;

public class LineStringPathIterator implements PathIterator {
  protected LineString line;

  private boolean done;

  protected int vertexIndex = 0;

  protected int awtType = SEG_MOVETO;

  private int vertexCount;

  /** Shared buffer for coordinate transforms. */
  protected final double[] currentCoordinates = new double[2];

  public LineStringPathIterator() {
    this.done = true;
  }

  public LineStringPathIterator(final LineString line) {
    reset(line);
  }

  @Override
  public int currentSegment(final double[] coordinates) {
    final LineString line = this.line;
    final int vertexIndex = this.vertexIndex;
    line.copyPoint(vertexIndex, 2, coordinates);
    return this.awtType;
  }

  @Override
  public int currentSegment(final float[] coordinates) {
    final LineString line = this.line;
    final int vertexIndex = this.vertexIndex;
    line.copyPoint(vertexIndex, 2, this.currentCoordinates);
    coordinates[0] = (float)this.currentCoordinates[0];
    coordinates[1] = (float)this.currentCoordinates[1];
    return this.awtType;
  }

  @Override
  public int getWindingRule() {
    return WIND_EVEN_ODD;
  }

  @Override
  public boolean isDone() {
    return this.done;
  }

  @Override
  public void next() {
    this.awtType = SEG_LINETO;
    this.vertexIndex++;
    if (this.vertexIndex >= this.vertexCount) {
      this.done = true;
    }
  }

  public LineStringPathIterator reset(final LineString line) {
    this.line = line;
    this.vertexIndex = 0;
    this.vertexCount = line.getVertexCount();
    this.awtType = SEG_MOVETO;
    this.done = this.vertexCount == 0;
    return this;
  }
}
