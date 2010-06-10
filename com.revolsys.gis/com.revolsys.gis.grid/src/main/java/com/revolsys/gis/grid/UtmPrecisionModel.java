package com.revolsys.gis.grid;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.ProjectedCoordinateSystem;
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

  public UtmPrecisionModel() {
  }

  public int getSrid() {
    return srid;
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
    final DoubleCoordinates geoCoordinates = new DoubleCoordinates(2);
    if (coordinateSystem instanceof ProjectedCoordinateSystem) {
      final ProjectedCoordinateSystem projectedCoordinateSystem = (ProjectedCoordinateSystem)coordinateSystem;
      final CoordinatesOperation geoOp = ProjectionFactory.getCoordinatesOperation(
        projectedCoordinateSystem,
        projectedCoordinateSystem.getGeographicCoordinateSystem());
      geoOp.perform(coordinates, geoCoordinates);

      lon = geoCoordinates.getX();
      lat = geoCoordinates.getY();
    } else {
      lon = coordinates.getX();
      lat = coordinates.getY();
    }
    final int utmSrid = utmGrid.getNad83Srid(lon, lat);
    if (srid != utmSrid) {
      final ProjectedCoordinateSystem utmCoordinateSystem = (ProjectedCoordinateSystem)EpsgCoordinateSystems.getCoordinateSystem(utmSrid);
      final CoordinatesOperation toUtm = ProjectionFactory.getCoordinatesOperation(
        utmCoordinateSystem.getGeographicCoordinateSystem(),
        utmCoordinateSystem);
       final Coordinates utmCoordinates = new DoubleCoordinates(
        coordinates.getNumAxis());
      toUtm.perform(geoCoordinates, utmCoordinates);
      utmPrecisionModel.makePrecise(utmCoordinates);
      final CoordinatesOperation toCs = ProjectionFactory.getCoordinatesOperation(
        utmCoordinateSystem, coordinateSystem);
      toCs.perform(utmCoordinates, coordinates);
    }

    precisionModel.makePrecise(coordinates);
  }
}
