package com.revolsys.gis.cs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.PrecisionModelUtil;
import com.revolsys.gis.model.coordinates.SimpleCoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesListFactory;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
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
  private static final long serialVersionUID = 4328651897279304108L;

  private static Map<Integer, GeometryFactory> factories = new HashMap<Integer, GeometryFactory>();

  public static GeometryFactory getFactory(final Geometry geometry) {
    if (geometry == null) {
      return null;
    } else {
      final com.vividsolutions.jts.geom.GeometryFactory factory = geometry.getFactory();
      if (factory instanceof GeometryFactory) {
        return (GeometryFactory)factory;
      } else {
        final int srid = geometry.getSRID();
        final CoordinateSystem coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(srid);
        if (coordinateSystem == null) {
          return null;
        } else {
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
  }

  public static GeometryFactory getFactory(final int srid) {
    GeometryFactory factory = factories.get(srid);
    if (factory == null) {
      factory = getFactory(EpsgCoordinateSystems.getCoordinateSystem(srid));
    }
    return factory;
  }

  public static GeometryFactory getFactory(
    final CoordinateSystem coordinateSystem) {
    int srid = getId(coordinateSystem);
    synchronized (factories) {
      GeometryFactory factory = factories.get(srid);
      if (factory == null) {
        factory = new GeometryFactory(coordinateSystem);
        factories.put(srid, factory);
      }
      return factory;
    }
  }

  public static GeometryFactory getFactory(final int srid, final int scale) {
    GeometryFactory factory = new GeometryFactory(
      EpsgCoordinateSystems.getCoordinateSystem(srid),
      new SimpleCoordinatesPrecisionModel(scale));
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

  public static LineString[] toLineStringArray(final GeometryFactory factory,
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

  public static MultiPolygon toMultiPolygon(
    final GeometryFactory geometryFactory, final List<?> polygons) {
    final Polygon[] polygonArray = toPolygonArray(geometryFactory, polygons);
    return geometryFactory.createMultiPolygon(polygonArray);
  }

  public static MultiPolygon toMultiPolygon(final List<Polygon> polygons) {
    final GeometryFactory geometryFactory;
    if (polygons.isEmpty()) {
      geometryFactory = new GeometryFactory();
    } else {
      geometryFactory = getFactory(polygons.get(0));
    }
    return toMultiPolygon(geometryFactory, polygons);
  }

  public static MultiPolygon toMultiPolygon(final Polygon... polygons) {
    return toMultiPolygon(Arrays.asList(polygons));
  }

  public static Point[] toPointArray(final GeometryFactory factory,
    final Collection<?> points) {
    final Point[] pointArray = new Point[points.size()];
    int i = 0;
    for (Object value : points) {
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
      i++;
    }
    return pointArray;
  }

  @SuppressWarnings("unchecked")
  public static Polygon[] toPolygonArray(final GeometryFactory factory,
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

  public GeometryFactory(final CoordinateSystem coordinateSystem) {
    this(coordinateSystem, new SimpleCoordinatesPrecisionModel());
  }

  public GeometryFactory(final CoordinateSystem coordinateSystem,
    final CoordinatesPrecisionModel coordinatesPrecisionModel) {
    super(PrecisionModelUtil.getPrecisionModel(coordinatesPrecisionModel),
      getId(coordinateSystem), new DoubleCoordinatesListFactory());
    this.coordinateSystem = coordinateSystem;
    this.coordinatesPrecisionModel = coordinatesPrecisionModel;
  }

  private static int getId(final CoordinateSystem coordinateSystem) {
    if (coordinateSystem == null) {
      return 0;
    } else {
      return coordinateSystem.getId();
    }
  }

  public GeometryFactory(final CoordinateSystem coordinateSystem,
    final int numAxis) {
    this(coordinateSystem, new SimpleCoordinatesPrecisionModel(), numAxis);
  }

  public GeometryFactory(final CoordinateSystem coordinateSystem,
    final CoordinatesPrecisionModel precisionModel, final int numAxis) {
    this(coordinateSystem, precisionModel);
    this.numAxis = numAxis;
  }

  public GeometryFactory(final GeometryFactory geometryFactory,
    final int numAxis) {
    this(geometryFactory.getCoordinateSystem(),
      geometryFactory.getCoordinatesPrecisionModel());
    this.numAxis = numAxis;
  }

  public GeometryFactory(CoordinatesPrecisionModel precisionModel, int numAxis) {
    super(PrecisionModelUtil.getPrecisionModel(precisionModel), 0,
      DoubleCoordinatesListFactory.INSTANCE);
    this.coordinateSystem = null;
    this.coordinatesPrecisionModel = precisionModel;
    this.numAxis = numAxis;
  }

  public GeometryFactory(int crsId) {
    this(EpsgCoordinateSystems.getCoordinateSystem(crsId));
  }

  public GeometryFactory(int crsId, final int numAxis) {
    this(EpsgCoordinateSystems.getCoordinateSystem(crsId), numAxis);
  }

  public GeometryFactory(int crsId, double scaleXY) {
    this(EpsgCoordinateSystems.getCoordinateSystem(crsId),
      new SimpleCoordinatesPrecisionModel(scaleXY), 2);
  }

  public GeometryFactory(int crsId, double scaleXY, double scaleZ) {
    this(EpsgCoordinateSystems.getCoordinateSystem(crsId),
      new SimpleCoordinatesPrecisionModel(scaleXY, scaleZ), 3);
  }

  public GeometryFactory(CoordinateSystem coordinateSystem, double scaleXY,
    double scaleZ) {
    this(coordinateSystem, new SimpleCoordinatesPrecisionModel(scaleXY, scaleZ));
  }

  public Geometry createGeometry(Geometry geometry) {
    if (geometry == null) {
      return null;
    } else if (geometry instanceof MultiPoint) {
      List<Point> geometries = new ArrayList<Point>();
      for (int i = 0; i < geometry.getNumGeometries(); i++) {
        Point subGeometry = (Point)geometry.getGeometryN(i);
        Point newSubGeometry = createPoint(subGeometry);
        geometries.add(newSubGeometry);
      }
      return createMultiPoint(geometries);
    } else if (geometry instanceof MultiLineString) {
      List<LineString> geometries = new ArrayList<LineString>();
      for (int i = 0; i < geometry.getNumGeometries(); i++) {
        LineString subGeometry = (LineString)geometry.getGeometryN(i);
        LineString newSubGeometry = createLineString(subGeometry);
        geometries.add(newSubGeometry);
      }
      return createMultiLineString(geometries);
    } else if (geometry instanceof MultiPolygon) {
      List<Polygon> geometries = new ArrayList<Polygon>();
      for (int i = 0; i < geometry.getNumGeometries(); i++) {
        Polygon subGeometry = (Polygon)geometry.getGeometryN(i);
        Polygon newSubGeometry = createPolygon(subGeometry);
        geometries.add(newSubGeometry);
      }
      return createMultiPolygon(geometries);
    } else if (geometry instanceof GeometryCollection) {
      List<Geometry> geometries = new ArrayList<Geometry>();
      for (int i = 0; i < geometry.getNumGeometries(); i++) {
        Geometry subGeometry = geometry.getGeometryN(i);
        Geometry newSubGeometry = createGeometry(subGeometry);
        geometries.add(newSubGeometry);
      }
      return createGeometryCollection(geometries);
    } else if (geometry instanceof Point) {
      Point point = (Point)geometry;
      return createPoint(point);
    } else if (geometry instanceof Point) {
      Point point = (Point)geometry;
      return createPoint(point);
    } else if (geometry instanceof LinearRing) {
      LinearRing linearRing = (LinearRing)geometry;
      return createLinearRing(linearRing);
    } else if (geometry instanceof LineString) {
      LineString lineString = (LineString)geometry;
      return createLineString(lineString);
    } else if (geometry instanceof Polygon) {
      Polygon polygon = (Polygon)geometry;
      return createPolygon(polygon);
    } else {
      throw new RuntimeException("Unknown geometry type " + geometry);
    }
  }

  public GeometryCollection createGeometryCollection(List<Geometry> geometries) {
    Geometry[] array = new Geometry[geometries.size()];
    geometries.toArray(array);
    return createGeometryCollection(array);
  }

  public Polygon createPolygon(Polygon polygon) {
    List<LinearRing> rings = new ArrayList<LinearRing>();
    LinearRing exteriorRing = (LinearRing)polygon.getExteriorRing();
    LinearRing newExteriorRing = createLinearRing(exteriorRing);
    rings.add(newExteriorRing);
    for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
      LinearRing interiorRing = (LinearRing)polygon.getInteriorRingN(i);
      LinearRing newInteriorRing = createLinearRing(interiorRing);
      rings.add(newInteriorRing);

    }
    return createPolygon(rings);
  }

  public LineString createLineString(LineString lineString) {
    CoordinatesList points = CoordinatesListUtil.get(lineString);
    CoordinatesList newPoints = createCoordinatesList(points);
    return createLineString(newPoints);
  }

  private CoordinatesList createCoordinatesList(CoordinatesList points) {
    final int size = points.size();
    final byte numAxis2 = points.getNumAxis();
    final int numAxis = Math.min(this.numAxis, numAxis2);
    CoordinatesList newPoints = new DoubleCoordinatesList(size, this.numAxis);
    points.copy(0, newPoints, 0, numAxis, size);
    return newPoints;
  }

  public LinearRing createLinearRing(LinearRing linearRing) {
    CoordinatesList points = CoordinatesListUtil.get(linearRing);
    CoordinatesList newPoints = createCoordinatesList(points);
    return createLinearRing(newPoints);
  }

  public Point createPoint(Point point) {
    CoordinatesList points = CoordinatesListUtil.get(point);
    CoordinatesList newPoints = createCoordinatesList(points);
    return createPoint(newPoints);

  }

  public Geometry createGeometry(final List<? extends Geometry> geometries) {
    if (geometries == null || geometries.size() == 0) {
      return createGeometryCollection((Geometry[])null);
    } else if (geometries.size() == 1) {
      return geometries.get(0);
    } else {
      final Set<Class<?>> classes = getGeometryClassSet(geometries);
      if (classes.equals(Collections.singleton(Point.class))) {
        return createMultiPoint(geometries);
      } else if (classes.equals(Collections.singleton(LineString.class))) {
        return createMultiLineString(geometries);
      } else if (classes.equals(Collections.singleton(Polygon.class))) {
        return createMultiPolygon(geometries);
      } else {
        final Geometry[] geometryArray = com.vividsolutions.jts.geom.GeometryFactory.toGeometryArray(geometries);
        return createGeometryCollection(geometryArray);
      }
    }
  }

  public LinearRing createLinearRing(final CoordinatesList points) {
    points.makePrecise(coordinatesPrecisionModel);
    return super.createLinearRing(points);
  }

  public LineString createLineString(final Coordinates... points) {
    final List<Coordinates> p = Arrays.asList(points);
    return createLineString(p);
  }

  public LineString createLineString(final CoordinatesList points) {
    if (points != null) {
      points.makePrecise(coordinatesPrecisionModel);
    }
    final LineString line = super.createLineString(points);
    return line;
  }

  public LineString createLineString(final List<Coordinates> points) {
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

  public MultiLineString createMultiLineString(final List<?> lines) {
    final LineString[] lineArray = toLineStringArray(this, lines);
    return createMultiLineString(lineArray);
  }

  public MultiPoint createMultiPoint(final Collection<?> points) {
    final Point[] pointArray = toPointArray(this, points);
    return createMultiPoint(pointArray);
  }

  public MultiPolygon createMultiPolygon(final List<?> polygons) {
    return toMultiPolygon(this, polygons);
  }

  public Point createPoint(final Coordinates point) {
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

  public Point createPoint(final CoordinatesList points) {
    if (points != null) {
      points.makePrecise(coordinatesPrecisionModel);
    }
    return super.createPoint(points);
  }

  public MultiPoint createMultiPoint(final CoordinatesList points) {
    if (points != null) {
      points.makePrecise(coordinatesPrecisionModel);
    }
    return super.createMultiPoint(points);
  }

  public Polygon createPolygon(final CoordinatesList... rings) {
    final List<CoordinatesList> ringList = Arrays.asList(rings);
    return createPolygon(ringList);
  }

  public Polygon createPolygon(final List<?> rings) {
    if (rings.size() == 0) {
      final DoubleCoordinatesList nullPoints = new DoubleCoordinatesList(0,
        numAxis);
      LinearRing ring = createLinearRing(nullPoints);
      return createPolygon(ring, null);
    } else {
      final LinearRing exteriorRing = getLinearRing(rings, 0);
      final LinearRing[] interiorRings = new LinearRing[rings.size() - 1];
      for (int i = 1; i < rings.size(); i++) {
        interiorRings[i - 1] = getLinearRing(rings, i);
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

  private LinearRing getLinearRing(final List<?> rings, final int index) {
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

  public int getNumAxis() {
    return numAxis;
  }

  public Coordinates getPreciseCoordinates(final Coordinates point) {
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

  public boolean hasM() {
    return numAxis > 3;
  }

  public void makePrecise(final Coordinates point) {
    coordinatesPrecisionModel.makePrecise(point);
  }

  @Override
  public String toString() {
    if (coordinatesPrecisionModel.isFloating()) {
      return coordinateSystem.getName() + " (" + coordinateSystem.getId() + ")";
    } else {
      return coordinateSystem.getName() + " (" + coordinateSystem.getId()
        + ") " + coordinatesPrecisionModel;
    }
  }

  public boolean isFloating() {
    return coordinatesPrecisionModel.isFloating();
  }

  public Point createPoint(double x, double y) {
    DoubleCoordinates coordinates = new DoubleCoordinates(x, y);
    return createPoint(coordinates);
  }

  public Geometry createEmptyGeometry() {
    return createPoint((Coordinate)null);
  }
}
