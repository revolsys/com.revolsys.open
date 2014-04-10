package com.revolsys.gis.model.coordinates.list;

import com.revolsys.jts.geom.CoordinatesList;

public class CoordinateSequenceCoordinateList extends AbstractCoordinatesList {

  /**
   * 
   */
  private static final long serialVersionUID = 872633273329727308L;

  private final CoordinatesList coordinateSequence;

  public CoordinateSequenceCoordinateList(
    final CoordinatesList coordinateSequence) {
    this.coordinateSequence = coordinateSequence;
  }

  @Override
  public AbstractCoordinatesList clone() {
    return new CoordinateSequenceCoordinateList(coordinateSequence);
  }

  @Override
  public byte getNumAxis() {
    return (byte)coordinateSequence.getDimension();
  }

  @Override
  public double getValue(final int index, final int axisIndex) {
    return coordinateSequence.getOrdinate(index, axisIndex);
  }

  @Override
  public void setValue(final int index, final int axisIndex, final double value) {
    coordinateSequence.setOrdinate(index, axisIndex, value);
  }

  @Override
  public int size() {
    return coordinateSequence.size();
  }

}
