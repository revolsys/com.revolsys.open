package com.revolsys.jts.geom;

import com.revolsys.gis.jts.GeometryEditUtil;

/**
 * @author Paul Austin <paul.austin@revolsys.com>
 */
public class VertexImpl extends AbstractVertex {

  private Coordinates coordinates;

  private int[] vertexId;

  public VertexImpl(final Geometry geometry, final int... vertexId) {
    super(geometry);
    setVertexId(vertexId);
  }

  @Override
  public double getValue(final int index) {
    if (coordinates == null) {
      return Double.NaN;
    } else {
      return coordinates.getValue(index);
    }
  }

  @Override
  public int[] getVertexId() {
    return vertexId;
  }

  @Override
  public boolean isEmpty() {
    return coordinates == null;
  }

  void setVertexId(final int... vertexId) {
    this.vertexId = vertexId;
    this.coordinates = GeometryEditUtil.getVertex(geometry, vertexId);
  }
}
