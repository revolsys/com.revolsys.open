package com.revolsys.gis.cs;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
  private static Map<Integer, GeometryFactory> factories = new HashMap<Integer, GeometryFactory>();

  public static GeometryFactory getFactory(
    final Geometry geometry) {
    if (geometry == null) {
      return null;
    } else {
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
          return new GeometryFactory(coordinateSystem,
            coordinatesPrecisionModel);
        }
      }
    }
  }

  public static GeometryFactory getFactory(
    final int srid) {
    GeometryFactory factory = factories.get(srid);
    if (factory == null) {
      factory = new GeometryFactory(
        EpsgCoordinateSystems.getCoordinateSystem(srid));
      factories.put(srid, factory);
    }
    return factory;
  }

  private static Set<Class<?>> getGeometryClassSet(
    final List<? extends Geometry> geometries) {
    final Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
    for (final Geometry geometry : geometries) {
      classes.add(geometry.getClass());
    }
    return classes;
  }

  public static LineString[] toLineStringArray(
    final GeometryFactory factory,
    final List<?> lines) {
    final LineString[] lineStrings = new LineString[lines.size()];
    for (int i = 0; i < lines.size(); i++) {
      final Object value = lines.get(i);
      if (value instanceof LineString) {
        final LineString lineString = (LineString)value;
        lineStrings[i] = lineString;
      } else if (value instanceof CoordinatesList) {
        final CoordinatesList coordinates = (CoordinatesList)value;
        lineStrings[i] = factory.createLineString(coordinates);
      } else if (value instanceof CoordinateSequence) {
        final CoordinateSequence coordinates = (CoordinateSequence)value;
        lineStrings[i] = factory.createLineString(coordinates);
      }
    }
    return lineStrings;
  }

  public static Polygon[] toPolygonArray(
    final GeometryFactory factory,
    final List<?> polygonList) {
    final Polygon[] polygons = new Polygon[polygonList.size()];
    for (int i = 0; i < polygonList.size(); i++) {
      final Object value = polygonList.get(i);
      if (value instanceof Polygon) {
        final Polygon polygon = (Polygon)value;
        polygons[i] = polygon;
      } else if (value instanceof List) {
        final List<CoordinatesList> coordinateList = (List<CoordinatesList>)value;
        polygons[i] = factory.createPolygon(coordinateList);
      }
    }
    return polygons;
  }

  public static MultiPolygon toMultiPolygon(
    final GeometryFactory geometryFactory,
    final List<?> polygons) {
    final Polygon[] polygonArray = toPolygonArray(geometryFactory, polygons);
    return geometryFactory.createMultiPolygon(polygonArray);
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
    final Polygon... polygons) {
    return toMultiPolygon(Arrays.asList(polygons));
  }

  public static Point[] toPointArray(
    final GeometryFactory factory,
    final List<?> points) {
    final Point[] pointArray = new Point[points.size()];
    for (int i = 0; i < points.size(); i++) {
      final Object value = points.get(i);
      if (value instanceof Point) {
        final Point point = (Point)value;
        pointArray[i] = point;
      } else if (value instanceof Coordinates) {
        final Coordinates coordinates = (Coordinates)value;
        pointArray[i] = factory.createPoint(coordinates);
      } else if (value instanceof Coordinate) {
        final Coordinate coordinate = (Coordinate)value;
        pointArray[i] = factory.createPoint(coordinate);
      } else if (value instanceof CoordinatesList) {
        final CoordinatesList coordinates = (CoordinatesList)value;
        pointArray[i] = factory.createPoint(coordinates);
      } else if (value instanceof CoordinateSequence) {
        final CoordinateSequence coordinates = (CoordinateSequence)value;
        pointArray[i] = factory.createPoint(coordinates);
      }
    }
    return pointArray;
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
        return createMultiPoint(geometries);
      } else if (classes.equals(Collections.singleton(LineString.class))) {
        return createMultiLineString(geometries);
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
    if (points != null) {
      points.makePrecise(coordinatesPrecisionModel);
    }
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

  public MultiPoint createMultiPoint(
    final List<?> points) {
    final Point[] pointArray = toPointArray(this, points);
    return createMultiPoint(pointArray);
  }

  public MultiPolygon createMultiPolygon(
    final List<?> polygons) {
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
    if (points != null) {
      points.makePrecise(coordinatesPrecisionModel);
    }
    return super.createPoint(points);
  }

  public Polygon createPolygon(
    final CoordinatesList... rings) {
    final List<CoordinatesList> ringList = Arrays.asList(rings);
    return createPolygon(ringList);
  }

  public Polygon createPolygon(
    final List<?> rings) {
    final LinearRing exteriorRing = getLinearRing(rings, 0);
    final LinearRing[] interiorRings = new LinearRing[rings.size() - 1];
    for (int i = 1; i < rings.size(); i++) {
      interiorRings[i - 1] = getLinearRing(rings, i);
    }
    return createPolygon(exteriorRing, interiorRings);
  }

  public CoordinatesPrecisionModel getCoordinatesPrecisionModel() {
    return coordinatesPrecisionModel;
  }

  public CoordinateSystem getCoordinateSystem() {
    return coordinateSystem;
  }

  private LinearRing getLinearRing(
    final List<?> rings,
    final int index) {
    final Object ring = rings.get(index);
    if (ring instanceof LinearRing) {
      return (LinearRing)ring;

    } else if (ring instanceof CoordinatesList) {
      final CoordinatesList points = (CoordinatesList)ring;
      return createLinearRing(points);
    } else if (ring instanceof CoordinateSequence) {
      final CoordinateSequence points = (CoordinateSequence)ring;
      return createLinearRing(points);
    } else {
      return null;
    }
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
