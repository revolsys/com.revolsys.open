package com.revolsys.gis.grid;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.ProjectedCoordinateSystem;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.cs.projection.CoordinatesOperation;
import com.revolsys.gis.cs.projection.ProjectionFactory;
import com.revolsys.gis.model.coordinates.CoordinateCoordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.PrecisionModel;

public class UtmPrecisionModel extends PrecisionModel {

  private PrecisionModel elevationPrecisionModel = new PrecisionModel(1);

  private PrecisionModel precisionModel = new PrecisionModel(1);

  private int srid;

  private final UtmRectangularMapGrid utmGrid = new UtmRectangularMapGrid();

  private PrecisionModel utmPrecisionModel = new PrecisionModel(1);

  private CoordinateSystem coordinateSystem;

  public UtmPrecisionModel() {
  }

  public UtmPrecisionModel(
    final int srid,
    final double scale,
    final double utmScale,
    final double elevationScale) {
    this.srid = srid;
    this.precisionModel = new PrecisionModel(scale);
    this.utmPrecisionModel = new PrecisionModel(utmScale);
    this.elevationPrecisionModel = new PrecisionModel(elevationScale);
  }

  public UtmPrecisionModel(
    final int srid,
    final PrecisionModel precisionModel,
    final PrecisionModel utmPrecisionModel,
    final PrecisionModel elevationPrecisionModel) {
    this.srid = srid;
    this.precisionModel = precisionModel;
    this.utmPrecisionModel = utmPrecisionModel;
    this.elevationPrecisionModel = elevationPrecisionModel;
  }

  public PrecisionModel getElevationPrecisionModel() {
    return elevationPrecisionModel;
  }

  public double getElevationScale() {
    return elevationPrecisionModel.getScale();
  }

  public PrecisionModel getPrecisionModel() {
    return precisionModel;
  }

  @Override
  public double getScale() {
    return precisionModel.getScale();
  }

  public int getSrid() {
    return srid;
  }

  public PrecisionModel getUtmPrecisionModel() {
    return utmPrecisionModel;
  }

  public double getUtmScale() {
    return utmPrecisionModel.getScale();
  }

  @Override
  public void makePrecise(
    final Coordinate coordinate) {
    double lon;
    double lat;
    final CoordinateCoordinates coordinateCoordinates = new CoordinateCoordinates(
      coordinate);
    final DoubleCoordinates geoCoordinates = new DoubleCoordinates(2);
    if (coordinateSystem instanceof ProjectedCoordinateSystem) {
      final ProjectedCoordinateSystem projectedCoordinateSystem = (ProjectedCoordinateSystem)coordinateSystem;
      final CoordinatesOperation geoOp = ProjectionFactory.getCoordinatesOperation(
        projectedCoordinateSystem,
        projectedCoordinateSystem.getGeographicCoordinateSystem());
      geoOp.perform(coordinateCoordinates, geoCoordinates);

      lon = geoCoordinates.getValue(0);
      lat = geoCoordinates.getValue(1);
    } else {
      lon = coordinate.x;
      lat = coordinate.y;
    }
    final int utmSrid = utmGrid.getNad83Srid(lon, lat);
    if (srid != utmSrid) {
      final ProjectedCoordinateSystem utmCoordinateSystem = (ProjectedCoordinateSystem)EpsgCoordinateSystems.getCoordinateSystem(utmSrid);
      final CoordinatesOperation toUtm = ProjectionFactory.getCoordinatesOperation(
        utmCoordinateSystem.getGeographicCoordinateSystem(),
        utmCoordinateSystem);
      final Coordinate utmCoordinate = new Coordinate();
      final CoordinateCoordinates utmCoordinates = new CoordinateCoordinates(
        utmCoordinate);
      toUtm.perform(geoCoordinates, utmCoordinates);
      utmPrecisionModel.makePrecise(utmCoordinate);
      final CoordinatesOperation toCs = ProjectionFactory.getCoordinatesOperation(
        utmCoordinateSystem, coordinateSystem);
      toCs.perform(utmCoordinates, coordinateCoordinates);
    }
    precisionModel.makePrecise(coordinate);
    if (elevationPrecisionModel != null) {
      coordinate.z = elevationPrecisionModel.makePrecise(coordinate.z);
    }
  }

  @Override
  public double makePrecise(
    double val) {
    return precisionModel.makePrecise(val);
  }

  public void setElevationPrecisionModel(
    final PrecisionModel elevationPrecisionModel) {
    this.elevationPrecisionModel = elevationPrecisionModel;
  }

  public void setElevationScale(
    final double elevationScale) {
    this.elevationPrecisionModel = new PrecisionModel(elevationScale);
  }

  public void setPrecisionModel(
    final PrecisionModel precisionModel) {
    this.precisionModel = precisionModel;
  }

  public void setScale(
    final double scale) {
    this.precisionModel = new PrecisionModel(scale);
  }

  public void setSrid(
    final int srid) {
    this.srid = srid;
    coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(srid);

  }

  public void setUtmPrecisionModel(
    final PrecisionModel utmPrecisionModel) {
    this.utmPrecisionModel = utmPrecisionModel;
  }

  public void setUtmScale(
    final double utmScale) {
    this.utmPrecisionModel = new PrecisionModel(utmScale);
  }
}
