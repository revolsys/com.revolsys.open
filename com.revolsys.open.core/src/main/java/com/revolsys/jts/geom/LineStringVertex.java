package com.revolsys.jts.geom;

public class LineStringVertex extends AbstractVertex {
  private int vertexIndex;

  protected LineStringVertex(final Geometry geometry, final int vertexIndex) {
    super(geometry);
    this.vertexIndex = vertexIndex;
  }

  @Override
  public double getValue(final int index) {
    final LineString line = getGeometry();
    return line.getCoordinate(vertexIndex, index);
  }

  @Override
  public int[] getVertexId() {
    return new int[] {
      vertexIndex
    };
  }

  public void setVertexIndex(final int vertexIndex) {
    this.vertexIndex = vertexIndex;
  }
}
