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
  public int getNumAxis() {
    return (byte)coordinateSequence.getNumAxis();
  }

  @Override
  public double getValue(final int index, final int axisIndex) {
    return coordinateSequence.getValue(index, axisIndex);
  }

  @Override
  public void setValue(final int index, final int axisIndex, final double value) {
    coordinateSequence.setValue(index, axisIndex, value);
  }

  @Override
  public int size() {
    return coordinateSequence.size();
  }

}
