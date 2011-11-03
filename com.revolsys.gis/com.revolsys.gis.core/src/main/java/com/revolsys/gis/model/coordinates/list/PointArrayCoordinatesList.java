package com.revolsys.gis.model.coordinates.list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.revolsys.gis.model.coordinates.Coordinates;

public class PointArrayCoordinatesList extends AbstractCoordinatesList {
  private static final long serialVersionUID = 5567278244212676984L;

  private byte numAxis;

  private List<Coordinates> points = new ArrayList<Coordinates>();

  public PointArrayCoordinatesList() {
  }

  public PointArrayCoordinatesList(byte numAxis) {
    this.numAxis = numAxis;
  }

  public PointArrayCoordinatesList(byte numAxis,Coordinates... points) {
    this.numAxis =numAxis;
    for (Coordinates point : points) {
      add(point);
    }
  }

  public void clear() {
    points.clear();
  }

  public void add(Coordinates coordinates) {
    points.add(coordinates);
  }

  public byte getNumAxis() {
    return numAxis;
  }

  public double getValue(int index, int axisIndex) {
    final Coordinates point = points.get(index);
    return point.getValue(axisIndex);
  }

  public void setValue(int index, int axisIndex, double value) {
    final Coordinates point = points.get(index);
    point.setValue(axisIndex, value);
  }

  public int size() {
    return points.size();
  }

  @Override
  public PointArrayCoordinatesList clone() {
    PointArrayCoordinatesList clone = (PointArrayCoordinatesList)super.clone();
    clone.points = new ArrayList<Coordinates>(points);
    return null;
  }

}
