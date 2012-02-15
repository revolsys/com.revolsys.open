package com.revolsys.gis.jts;

public class SimpleLineSegment extends AbstractLineSegment {

  private final int dimension;

  private final double[] endCoordinates;

  private final double[] startCoordinates;

  public SimpleLineSegment(final CoordinateSequenceIndexLineSegment segment) {
    this(segment.getStartCoordinates(), segment.getEndCoordinates());
  }

  public SimpleLineSegment(final double[] startCoordinates,
    final double[] endCoordinates) {
    this.dimension = startCoordinates.length;
    this.startCoordinates = startCoordinates.clone();
    this.endCoordinates = endCoordinates.clone();
  }

  public int getDimension() {
    return dimension;
  }

  @Override
  public double[] getEndCoordinates() {
    return endCoordinates.clone();
  }

  public double getEndOrdinate(final int ordinateIndex) {
    return endCoordinates[ordinateIndex];
  }

  @Override
  public double[] getStartCoordinates() {
    return startCoordinates.clone();
  }

  public double getStartOrdinate(final int ordinateIndex) {
    return startCoordinates[ordinateIndex];
  }
}
