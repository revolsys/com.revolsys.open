package com.revolsys.gis.grid;

import javax.annotation.PostConstruct;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.cs.projection.CoordinatesOperation;
import com.revolsys.gis.cs.projection.ProjectionFactory;
import com.revolsys.gis.model.coordinates.CoordinateCoordinates;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.SimpleCoordinatesPrecisionModel;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.PrecisionModel;

public class UtmPrecisionModel extends PrecisionModel implements
  CoordinatesPrecisionModel {

  private int srid;

  private final UtmRectangularMapGrid utmGrid = new UtmRectangularMapGrid();

  private SimpleCoordinatesPrecisionModel utmPrecisionModel = new SimpleCoordinatesPrecisionModel(
    1);

  private CoordinateSystem coordinateSystem;

  private SimpleCoordinatesPrecisionModel precisionModel = new SimpleCoordinatesPrecisionModel(
    1000, 1);

  private CoordinateSystem utmCoordinateSystem;

  private int utmSrid;

  private CoordinatesOperation toUtm;

  private CoordinatesOperation toCs;

  public UtmPrecisionModel() {
  }

  public int getSrid() {
    return srid;
  }

  @PostConstruct
  public void initialize() {
    toUtm = ProjectionFactory.getCoordinatesOperation(coordinateSystem,
      utmCoordinateSystem);
    toCs = ProjectionFactory.getCoordinatesOperation(utmCoordinateSystem,
      coordinateSystem);
  }

  @Override
  public void makePrecise(
    final Coordinate coordinate) {
    final CoordinateCoordinates coordinates = new CoordinateCoordinates(
      coordinate);
    makePrecise(coordinates);
  }

  @Override
  public double makePrecise(
    double val) {
    return SimpleCoordinatesPrecisionModel.makePrecise(val,
      precisionModel.getScaleXY());
  }

  public void setSrid(
    final int srid) {
    this.srid = srid;
    coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(srid);
  }

  public void setUtmSrid(
    final int utmSrid) {
    this.utmSrid = utmSrid;
    utmCoordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(utmSrid);
  }

  public Coordinates getPreciseCoordinates(
    final Coordinates coordinates) {
    Coordinates newCoordinates = new DoubleCoordinates(coordinates);
    makePrecise(newCoordinates);
    return newCoordinates;
  }

  public void makePrecise(
    Coordinates coordinates) {
    double lon;
    double lat;
    if (coordinateSystem.equals(utmCoordinateSystem)) {
      utmPrecisionModel.makePrecise(coordinates);
    } else {
      final Coordinates utmCoordinates = new DoubleCoordinates(
        coordinates.getNumAxis());
      toUtm.perform(coordinates, utmCoordinates);
      utmPrecisionModel.makePrecise(utmCoordinates);

      toCs.perform(utmCoordinates, coordinates);
      precisionModel.makePrecise(coordinates);
    }
  }

  @Override
  public String toString() {
    return "UTM-Fixed(0.001,1)";
  }
}
