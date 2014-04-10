package com.revolsys.jts.geom;

public class MultiPointVertex extends AbstractVertex {
  private int partIndex;

  public MultiPointVertex(final MultiPoint geometry, final int partIndex) {
    super(geometry);
    this.partIndex = partIndex;
  }

  public int getPartIndex() {
    return partIndex;
  }

  @Override
  public double getValue(final int vertexIndex) {
    final MultiPoint geometry = getGeometry();
    return geometry.getCoordinate(this.partIndex, vertexIndex);
  }

  @Override
  public int[] getVertexId() {
    return new int[] {
      this.partIndex, 0
    };
  }

  @Override
  public int getVertexIndex() {
    return 0;
  }

  public void setPartIndex(final int partIndex) {
    this.partIndex = partIndex;
  }
}
