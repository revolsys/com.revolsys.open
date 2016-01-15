package com.revolsys.geometry.model;

import java.awt.geom.PathIterator;

import com.revolsys.geometry.model.vertex.Vertex;

public class VertexPathIterator implements PathIterator {
  protected final Vertex vertex;

  private boolean done = false;

  public VertexPathIterator(final Vertex vertex) {
    this.vertex = vertex;
    next();
  }

  @Override
  public int currentSegment(final double[] coordinates) {
    final double x = this.vertex.getX();
    final double y = this.vertex.getY();
    coordinates[0] = x;
    coordinates[1] = y;
    return this.vertex.getAwtType();
  }

  @Override
  public int currentSegment(final float[] coordinates) {
    final double x = this.vertex.getX();
    final double y = this.vertex.getY();
    coordinates[0] = (float)x;
    coordinates[1] = (float)y;
    return this.vertex.getAwtType();
  }

  public Vertex getVertex() {
    return this.vertex;
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
    if (this.vertex.hasNext()) {
      this.vertex.next();
    } else {
      this.done = true;
    }
  }
}
