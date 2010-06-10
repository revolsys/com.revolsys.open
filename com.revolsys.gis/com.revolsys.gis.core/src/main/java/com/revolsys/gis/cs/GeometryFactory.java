package com.revolsys.gis.cs;

import java.util.Arrays;
import java.util.List;

import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.PrecisionModelUtil;
import com.revolsys.gis.model.coordinates.SimpleCoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesListFactory;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

public class GeometryFactory extends
  com.vividsolutions.jts.geom.GeometryFactory {

  public static GeometryFactory getFactory(
    final Geometry geometry) {
    final com.vividsolutions.jts.geom.GeometryFactory factory = geometry.getFactory();
    if (factory instanceof GeometryFactory) {
      return (GeometryFactory)factory;
    } else {
      final int srid = factory.getSRID();
      final CoordinateSystem coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(srid);
      final PrecisionModel precisionModel = factory.getPrecisionModel();
      if (precisionModel.isFloating()) {
        return new GeometryFactory(coordinateSystem);
      } else {
        final CoordinatesPrecisionModel coordinatesPrecisionModel = new SimpleCoordinatesPrecisionModel(
          precisionModel.getScale());
        return new GeometryFactory(coordinateSystem, coordinatesPrecisionModel);
      }
    }
  }

  private final CoordinatesPrecisionModel coordinatesPrecisionModel;

  private final CoordinateSystem coordinateSystem;

  public GeometryFactory(
    final CoordinateSystem coordinateSystem) {
    this(coordinateSystem, new SimpleCoordinatesPrecisionModel());
  }

  public GeometryFactory(
    final CoordinateSystem coordinateSystem,
    final CoordinatesPrecisionModel coordinatesPrecisionModel) {
    super(PrecisionModelUtil.getPrecisionModel(coordinatesPrecisionModel),
      coordinateSystem.getId(), new DoubleCoordinatesListFactory());
    this.coordinateSystem = coordinateSystem;
    this.coordinatesPrecisionModel = coordinatesPrecisionModel;
  }

  public LineString createLineString(
    final Coordinates... points) {
    final List<Coordinates> p = Arrays.asList(points);
    return createLineString(p);
  }

  public LineString createLineString(
    final List<Coordinates> points) {
    CoordinatesList coordinatesList;
    final int numPoints = points.size();
    if (numPoints == 0) {
      coordinatesList = null;
    } else {
      final Coordinates point0 = points.get(0);
      final byte numAxis = point0.getNumAxis();

      coordinatesList = new DoubleCoordinatesList(numAxis, numPoints);
      for (int i = 0; i < numPoints; i++) {
        final Coordinates point = points.get(i);
        coordinatesList.setCoordinates(i, point);
      }
    }
    return createLineString(coordinatesList);
  }

  public Point createPoint(
    final Coordinates point) {
    final byte numAxis = point.getNumAxis();
    final double[] coordinates = point.getCoordinates();
    final DoubleCoordinatesList coordinatesList = new DoubleCoordinatesList(
      numAxis, coordinates);
    return createPoint(coordinatesList);
  }

  public CoordinatesPrecisionModel getCoordinatesPrecisionModel() {
    return coordinatesPrecisionModel;
  }

  public CoordinateSystem getCoordinateSystem() {
    return coordinateSystem;
  }

  @Override
  public String toString() {
    return coordinateSystem.getName() + ", precision="
      + getCoordinatesPrecisionModel();
  }

}
