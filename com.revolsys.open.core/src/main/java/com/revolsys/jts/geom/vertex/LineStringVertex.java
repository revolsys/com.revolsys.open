package com.revolsys.jts.geom.vertex;

import com.revolsys.jts.geom.LineString;

public class LineStringVertex extends AbstractVertex {
  private int vertexIndex;

  public LineStringVertex(final LineString line, final int vertexIndex) {
    super(line);
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
