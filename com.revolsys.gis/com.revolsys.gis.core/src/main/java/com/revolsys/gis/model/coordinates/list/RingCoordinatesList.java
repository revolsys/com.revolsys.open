package com.revolsys.gis.model.coordinates.list;

public class RingCoordinatesList extends DoubleCoordinatesList {

  public RingCoordinatesList(
    final CoordinatesList coordinatesList) {
    super(coordinatesList.subList(0, coordinatesList.size() - 1));
  }

  @Override
  public RingCoordinatesList clone() {
    throw new UnsupportedOperationException();
  }

  @Override
  public double getValue(
    final int index,
    final int axisIndex) {
    if (axisIndex >= getNumAxis()) {
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
  public void setValue(
    final int index,
    final int axisIndex,
    final double value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int size() {
    return super.size() + 1;
  }
}
