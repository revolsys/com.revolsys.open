package com.revolsys.gis.model.coordinates.comparator;

import java.util.Comparator;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.cs.projection.CoordinatesOperation;
import com.revolsys.gis.cs.projection.ProjectionFactory;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;

/**
 * Compare the coordinates, lowest Y first if equal then X comparison.
 * Calculation performed in geographics.
 * 
 * @author paustin
 */
public class LowestLeftComparator implements Comparator<Coordinates> {
  private CoordinatesOperation inverseOperation;

  public LowestLeftComparator(final int srid) {
    CoordinateSystem coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(srid);
    inverseOperation = ProjectionFactory.getInverseCoordinatesOperation(coordinateSystem);
  }

  public int compare(final Coordinates coordinates1,
    final Coordinates coordinates2) {
    final Coordinates point1;
    final Coordinates point2;
    if (inverseOperation == null) {
      point1 = coordinates1;
      point2 = coordinates2;
    } else {
      point1 = new DoubleCoordinates(2);
      inverseOperation.perform(coordinates1, point1);
      point2 = new DoubleCoordinates(2);
      inverseOperation.perform(coordinates2, point2);
    }

    Double x1 = point1.getX();
    Double y1 = point1.getY();
    Double x2 = point2.getX();
    Double y2 = point2.getY();

    int yCompare = y1.compareTo(y2);
    if (yCompare == 0) {
      return x1.compareTo(x2);
    } else {
      return yCompare;
    }
  }
}
