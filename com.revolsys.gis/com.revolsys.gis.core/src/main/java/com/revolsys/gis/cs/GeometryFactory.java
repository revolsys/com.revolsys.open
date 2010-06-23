package com.revolsys.gis.cs;

import java.util.Arrays;
import java.util.List;

import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.data.model.DataObjectUtil;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.PrecisionModelUtil;
import com.revolsys.gis.model.coordinates.SimpleCoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesListFactory;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

public class GeometryFactory extends
  com.vividsolutions.jts.geom.GeometryFactory implements
  CoordinatesPrecisionModel {

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

  public GeometryFactory() {
    super(
      PrecisionModelUtil.getPrecisionModel(SimpleCoordinatesPrecisionModel.FLOATING),
      0, DoubleCoordinatesListFactory.INSTANCE);
    this.coordinateSystem = null;
    this.coordinatesPrecisionModel = SimpleCoordinatesPrecisionModel.FLOATING;
  }

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

  public LinearRing createLinearRing(
    final CoordinatesList points) {
    points.makePrecise(coordinatesPrecisionModel);
    return super.createLinearRing(points);
  }

  public LineString createLineString(
    final Coordinates... points) {
    final List<Coordinates> p = Arrays.asList(points);
    return createLineString(p);
  }

  public LineString createLineString(
    final CoordinatesList points) {
    points.makePrecise(coordinatesPrecisionModel);
    final LineString line = super.createLineString(points);
    return line;
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

      coordinatesList = new DoubleCoordinatesList(numPoints, numAxis);
      for (int i = 0; i < numPoints; i++) {
        final Coordinates point = points.get(i);
        coordinatesList.setPoint(i, point);
      }
    }
    final LineString line = createLineString(coordinatesList);
    return line;
  }

  public Geometry createMultiPoint(
    final List<Point> points) {
    final Point[] pointArray = com.vividsolutions.jts.geom.GeometryFactory.toPointArray(points);
    return super.createMultiPoint(pointArray);
  }

  public Point createPoint(
    final Coordinates point) {
    final byte numAxis = point.getNumAxis();
    final double[] coordinates = point.getCoordinates();
    final DoubleCoordinatesList coordinatesList = new DoubleCoordinatesList(
      numAxis, coordinates);
    coordinatesList.makePrecise(coordinatesPrecisionModel);
    return super.createPoint(coordinatesList);
  }

  public Point createPoint(
    final CoordinatesList points) {
    points.makePrecise(coordinatesPrecisionModel);
    return super.createPoint(points);
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

  public Coordinates getPreciseCoordinates(
    Coordinates point) {
    return coordinatesPrecisionModel.getPreciseCoordinates(point);
  }

  public void makePrecise(
    Coordinates point) {
    coordinatesPrecisionModel.makePrecise(point);
  }

}
