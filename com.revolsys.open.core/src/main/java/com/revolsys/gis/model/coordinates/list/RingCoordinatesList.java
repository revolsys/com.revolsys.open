package com.revolsys.gis.model.coordinates.list;

import com.revolsys.jts.geom.CoordinatesList;

public class RingCoordinatesList extends DoubleCoordinatesList {

  /**
   * 
   */
  private static final long serialVersionUID = 5660399503744099455L;

  public RingCoordinatesList(final CoordinatesList coordinatesList) {
    super(coordinatesList.subList(0, coordinatesList.size() - 1));
  }

  @Override
  public RingCoordinatesList clone() {
    throw new UnsupportedOperationException();
  }

  @Override
  public double getValue(final int index, final int axisIndex) {
    if (axisIndex >= getAxisCount()) {
      return Double.NaN;
    } else {
      if (index >= super.size()) {
        return super.getValue(index % super.size(), axisIndex);
      } else {
        return super.getValue(index, axisIndex);
      }
    }
  }

  @Override
  public int size() {
    return super.size() + 1;
  }
}
