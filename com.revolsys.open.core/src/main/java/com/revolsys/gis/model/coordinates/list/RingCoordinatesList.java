package com.revolsys.gis.model.coordinates.list;

import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.impl.LineStringDouble;

public class RingCoordinatesList extends LineStringDouble {

  /**
   *
   */
  private static final long serialVersionUID = 5660399503744099455L;

  public RingCoordinatesList(final LineString coordinatesList) {
    super(coordinatesList.subLine(0, coordinatesList.getVertexCount() - 1));
  }

  @Override
  public RingCoordinatesList clone() {
    throw new UnsupportedOperationException();
  }

  @Override
  public double getCoordinate(final int index, final int axisIndex) {
    if (axisIndex >= getAxisCount()) {
      return Double.NaN;
    } else {
      if (index >= super.getVertexCount()) {
        return super.getCoordinate(index % super.getVertexCount(), axisIndex);
      } else {
        return super.getCoordinate(index, axisIndex);
      }
    }
  }

  @Override
  public int getVertexCount() {
    return super.getVertexCount() + 1;
  }
}
