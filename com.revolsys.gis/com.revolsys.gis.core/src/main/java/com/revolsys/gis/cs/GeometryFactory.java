package com.revolsys.gis.cs;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.PrecisionModelUtil;
import com.revolsys.gis.model.coordinates.SimpleCoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesListFactory;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
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

  private static Set<Class<?>> getGeometryClassSet(
    final List<? extends Geometry> geometries) {
    final Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
    for (final Geometry geometry : geometries) {
      classes.add(geometry.getClass());
    }
    return classes;
  }

  public static MultiPolygon toMultiPolygon(
    final List<Polygon> polygons) {
    final GeometryFactory geometryFactory;
    if (polygons.isEmpty()) {
      geometryFactory = new GeometryFactory();
    } else {
      geometryFactory = getFactory(polygons.get(0));
    }
    return toMultiPolygon(geometryFactory, polygons);
  }

  public static MultiPolygon toMultiPolygon(
    final GeometryFactory geometryFactory,
    final List<Polygon> polygons) {
    final Polygon[] polygonArray = toPolygonArray(polygons);
    return geometryFactory.createMultiPolygon(polygonArray);
  }

  private final CoordinatesPrecisionModel coordinatesPrecisionModel;

  private final CoordinateSystem coordinateSystem;

  private int numAxis = 2;

  public GeometryFactory() {
    super(
      PrecisionModelUtil.getPrecisionModel(SimpleCoordinatesPrecisionModel.FLOATING),
      0, DoubleCoordinatesListFactory.INSTANCE);
    this.coordinateSystem = null;
    this.coordinatesPrecisionModel = SimpleCoordinatesPrecisionModel.FLOATING;
  }

  public GeometryFactory(
    final CoordinatesPrecisionModel coordinatesPrecisionModel) {
    super(PrecisionModelUtil.getPrecisionModel(coordinatesPrecisionModel), 0,
      new DoubleCoordinatesListFactory());
    this.coordinatesPrecisionModel = coordinatesPrecisionModel;
    this.coordinateSystem = null;
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

  public GeometryFactory(
    final CoordinateSystem coordinateSystem,
    final CoordinatesPrecisionModel precisionModel,
    final int numAxis) {
    this(coordinateSystem, precisionModel);
    this.numAxis = numAxis;
  }

  public GeometryFactory(
    final GeometryFactory geometryFactory,
    final int numAxis) {
    this(geometryFactory.getCoordinateSystem(),
      geometryFactory.getCoordinatesPrecisionModel());
    this.numAxis = numAxis;
  }

  @SuppressWarnings("unchecked")
  public Geometry createGeometry(
    final List<? extends Geometry> geometries) {
    if (geometries == null || geometries.size() == 0) {
      return createGeometryCollection(null);
    } else if (geometries.size() == 1) {
      return geometries.get(0);
    } else {
      final Set<Class<?>> classes = getGeometryClassSet(geometries);
      if (classes.equals(Collections.singleton(Point.class))) {
        return createMultiPoint((List<Point>)geometries);
      } else if (classes.equals(Collections.singleton(LineString.class))) {
        return createMultiLineString((List<LineString>)geometries);
      } else if (classes.equals(Collections.singleton(Polygon.class))) {
        return createMultiPolygon((List<Polygon>)geometries);
      } else {
        final Geometry[] geometryArray = com.vividsolutions.jts.geom.GeometryFactory.toGeometryArray(geometries);
        return createGeometryCollection(geometryArray);
      }
    }
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
    if (points == null || points.isEmpty()) {
      return createLineString((CoordinateSequence)null);
    } else {
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
      return createLineString(coordinatesList);
    }
  }

  public MultiLineString createMultiLineString(
    final List<?> lines) {
    final LineString[] lineArray = toLineStringArray(this, lines);
    return createMultiLineString(lineArray);
  }

  public static Point[] toPointArray(
    GeometryFactory factory,
    List<?> points) {
    Point[] pointArray = new Point[points.size()];
    for (int i = 0; i < points.size(); i++) {
      Object value = points.get(i);
      if (value instanceof Point) {
        Point point = (Point)value;
        pointArray[i] = point;
      } else if (value instanceof Coordinates) {
        Coordinates coordinates = (Coordinates)value;
        pointArray[i] = factory.createPoint(coordinates);
      } else if (value instanceof Coordinate) {
        Coordinate coordinate = (Coordinate)value;
        pointArray[i] = factory.createPoint(coordinate);
      } else if (value instanceof CoordinatesList) {
        CoordinatesList coordinates = (CoordinatesList)value;
        pointArray[i] = factory.createPoint(coordinates);
      } else if (value instanceof CoordinateSequence) {
        CoordinateSequence coordinates = (CoordinateSequence)value;
        pointArray[i] = factory.createPoint(coordinates);
      }
    }
    return pointArray;
  }

  public static LineString[] toLineStringArray(
    GeometryFactory factory,
    List<?> lines) {
    LineString[] lineStrings = new LineString[lines.size()];
    for (int i = 0; i < lines.size(); i++) {
      Object value = lines.get(i);
      if (value instanceof LineString) {
        LineString lineString = (LineString)value;
        lineStrings[i] = lineString;
      } else if (value instanceof CoordinatesList) {
        CoordinatesList coordinates = (CoordinatesList)value;
        lineStrings[i] = factory.createLineString(coordinates);
      } else if (value instanceof CoordinateSequence) {
        CoordinateSequence coordinates = (CoordinateSequence)value;
        lineStrings[i] = factory.createLineString(coordinates);
      }
    }
    return lineStrings;
  }

  public MultiPoint createMultiPoint(
    final List<?> points) {
    final Point[] pointArray = toPointArray(this, points);
    return createMultiPoint(pointArray);
  }

  public MultiPolygon createMultiPolygon(
    final List<Polygon> polygons) {
    return toMultiPolygon(this, polygons);
  }

  public Point createPoint(
    final Coordinates point) {
    if (point == null) {
      return createPoint((Coordinate)null);
    } else {
      final byte numAxis = point.getNumAxis();
      final double[] coordinates = point.getCoordinates();
      final DoubleCoordinatesList coordinatesList = new DoubleCoordinatesList(
        numAxis, coordinates);
      coordinatesList.makePrecise(coordinatesPrecisionModel);
      return super.createPoint(coordinatesList);
    }
  }

  public Point createPoint(
    final CoordinatesList points) {
    points.makePrecise(coordinatesPrecisionModel);
    return super.createPoint(points);
  }

  public Polygon createPolygon(
    final CoordinatesList... rings) {
    return createPolygon(Arrays.asList(rings));
  }

  public Polygon createPolygonFromLinearRings(
    final List<LinearRing> rings) {
    final LinearRing exteriorRing = rings.get(0);
    final LinearRing[] interiorRings = new LinearRing[rings.size() - 1];
    for (int i = 1; i < rings.size(); i++) {
      interiorRings[i - 1] = rings.get(i);
    }
    return createPolygon(exteriorRing, interiorRings);
  }

  public Polygon createPolygon(
    final List<CoordinatesList> rings) {
    if (rings == null || rings.isEmpty()) {
      return createPolygon(null, null);
    } else {
      final LinearRing exteriorRing = createLinearRing(rings.get(0));
      final LinearRing[] interiorRings = new LinearRing[rings.size() - 1];
      for (int i = 1; i < rings.size(); i++) {
        interiorRings[i - 1] = createLinearRing(rings.get(i));
      }
      return createPolygon(exteriorRing, interiorRings);
    }
  }

  public CoordinatesPrecisionModel getCoordinatesPrecisionModel() {
    return coordinatesPrecisionModel;
  }

  public CoordinateSystem getCoordinateSystem() {
    return coordinateSystem;
  }

  protected int getNumAxis() {
    return numAxis;
  }

  public Coordinates getPreciseCoordinates(
    final Coordinates point) {
    return coordinatesPrecisionModel.getPreciseCoordinates(point);
  }

  public double getScaleXY() {
    final CoordinatesPrecisionModel precisionModel = getCoordinatesPrecisionModel();
    return precisionModel.getScaleXY();
  }

  public double getScaleZ() {
    final CoordinatesPrecisionModel precisionModel = getCoordinatesPrecisionModel();
    return precisionModel.getScaleZ();
  }

  public boolean hasZ() {
    return numAxis > 2;
  }

  public void makePrecise(
    final Coordinates point) {
    coordinatesPrecisionModel.makePrecise(point);
  }

  @Override
  public String toString() {
    return coordinateSystem.getName() + ", precision="
      + getCoordinatesPrecisionModel();
  }
}
