package com.revolsys.gis.model.coordinates.list;

public class ReverseCoordinatesList extends AbstractCoordinatesList {
  /**
   * 
   */
  private static final long serialVersionUID = 8489637345116642703L;

  private final CoordinatesList coordinateList;

  public ReverseCoordinatesList(final CoordinatesList coordinateList) {
    this.coordinateList = coordinateList;
  }

  @Override
  public ReverseCoordinatesList clone() {
    return (ReverseCoordinatesList)super.clone();
  }

  @Override
  public byte getNumAxis() {
    return coordinateList.getNumAxis();
  }

  @Override
  public double getValue(final int index, final int axisIndex) {
    return coordinateList.getValue(size() - index - 1, axisIndex);
  }

  @Override
  public CoordinatesList reverse() {
    return coordinateList;
  }

  @Override
  public void setValue(final int index, final int axisIndex, final double value) {
    coordinateList.setValue(size() - index - 1, axisIndex, value);
  }

  @Override
  public int size() {
    return coordinateList.size();
  }
}
