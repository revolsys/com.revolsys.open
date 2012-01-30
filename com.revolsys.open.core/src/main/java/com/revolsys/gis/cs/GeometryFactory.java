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
import com.revolsys.gis.cs.projection.GeometryProjectionUtil;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.PrecisionModelUtil;
import com.revolsys.gis.model.coordinates.SimpleCoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesListFactory;
import com.revolsys.io.wkt.WktParser;
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

  private static Map<String, GeometryFactory> factories = new HashMap<String, GeometryFactory>();

  /**
   * get a 3d geometry factory with no coordinate system and a floating scale.
   */
  public static GeometryFactory getFactory() {
    return getFactory(0, 3, 0, 0);
  }

  /**
   * get a 3d geometry factory with a floating scale.
   */
  public static GeometryFactory getFactory(
    final CoordinateSystem coordinateSystem) {
    final int crsId = getId(coordinateSystem);
    return getFactory(crsId, 3, 0, 0);
  }

  public static GeometryFactory getFactory(final double scaleXy) {
    return getFactory(0, 3, scaleXy, 0);
  }

  public static GeometryFactory getFactory(final Geometry geometry) {
    if (geometry == null) {
      return getFactory(0, 3, 0, 0);
    } else {
      final com.vividsolutions.jts.geom.GeometryFactory factory = geometry.getFactory();
      if (factory instanceof GeometryFactory) {
        return (GeometryFactory)factory;
      } else {
        final int crsId = geometry.getSRID();
        final PrecisionModel precisionModel = factory.getPrecisionModel();
        if (precisionModel.isFloating()) {
          return getFactory(crsId, 3, 0, 0);
        } else {
          final double scaleXy = precisionModel.getScale();
          return getFactory(crsId, 3, scaleXy, 0);
        }
      }
    }
  }

  /**
   * get a 3d geometry factory with a floating scale.
   */
  public static GeometryFactory getFactory(final int crsId) {
    return getFactory(crsId, 3, 0, 0);
  }

  /**
   * Get a 2D geometry factory with the specified scale
   * 
   * @param crsId
   * @param scale
   * @return
   */
  public static GeometryFactory getFactory(final int crsId, final double scale) {
    return getFactory(crsId, 2, scale, 0);
  }

  public static GeometryFactory getFactory(
    final int crsId,
    final double scaleXy,
    final double scaleZ) {
    return getFactory(crsId, 3, scaleXy, scaleZ);
  }

  /**
   * Get a 2D geometry factory with the specified scale
   * 
   * @param crsId
   * @param scale
   * @return
   */
  public static GeometryFactory getFactory(final int crsId, final int numAxis) {
    return getFactory(crsId, numAxis, 0, 0);
  }

  public static GeometryFactory getFactory(
    final int crsId,
    final int numAxis,
    final double scaleXY,
    final double scaleZ) {
    synchronized (factories) {
      final String key = crsId + "-" + numAxis + "-" + scaleXY + "-" + scaleZ;
      GeometryFactory factory = factories.get(key);
      if (factory == null) {
        factory = new GeometryFactory(crsId, numAxis, scaleXY, scaleZ);
        factories.put(key, factory);
      }
      return factory;
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

  private static int getId(final CoordinateSystem coordinateSystem) {
    if (coordinateSystem == null) {
      return 0;
    } else {
      return coordinateSystem.getId();
    }
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
      } else if (value instanceof double[]) {
        final double[] points = (double[])value;
        lineStrings[i] = factory.createLineString(points);
      }
    }
    return lineStrings;
  }

  public static MultiPolygon toMultiPolygon(
    final GeometryFactory geometryFactory,
    final List<?> polygons) {
    final Polygon[] polygonArray = toPolygonArray(geometryFactory, polygons);
    return geometryFactory.createMultiPolygon(polygonArray);
  }

  public static MultiPolygon toMultiPolygon(final List<Polygon> polygons) {
    final GeometryFactory geometryFactory;
    if (polygons.isEmpty()) {
      geometryFactory = GeometryFactory.getFactory();
    } else {
      geometryFactory = getFactory(polygons.get(0));
    }
    return toMultiPolygon(geometryFactory, polygons);
  }

  public static MultiPolygon toMultiPolygon(final Polygon... polygons) {
    return toMultiPolygon(Arrays.asList(polygons));
  }

  public static Point[] toPointArray(
    final GeometryFactory factory,
    final Collection<?> points) {
    final Point[] pointArray = new Point[points.size()];
    int i = 0;
    for (final Object value : points) {
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
      } else if (value instanceof double[]) {
        final double[] coordinates = (double[])value;
        pointArray[i] = factory.createPoint(coordinates);
      }
      i++;
    }
    return pointArray;
  }

  @SuppressWarnings("unchecked")
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

  private final CoordinatesPrecisionModel coordinatesPrecisionModel;

  private final CoordinateSystem coordinateSystem;

  private int numAxis = 2;

  private final WktParser wktParser = new WktParser(this);

  public GeometryFactory(final CoordinateSystem coordinateSystem,
    final CoordinatesPrecisionModel coordinatesPrecisionModel) {
    super(PrecisionModelUtil.getPrecisionModel(coordinatesPrecisionModel),
      getId(coordinateSystem), new DoubleCoordinatesListFactory());
    this.coordinateSystem = coordinateSystem;
    this.coordinatesPrecisionModel = coordinatesPrecisionModel;
  }

  public GeometryFactory(final int crsId, final int numAxis,
    final double scaleXY, final double scaleZ) {
    this(EpsgCoordinateSystems.getCoordinateSystem(crsId),
      new SimpleCoordinatesPrecisionModel(scaleXY, scaleZ));
    this.numAxis = numAxis;
  }

  public CoordinatesList createCoordinatesList(final CoordinateSequence points) {
    final int size = points.size();
    final CoordinatesList newPoints = new DoubleCoordinatesList(size,
      this.numAxis);
    final int numAxis2 = points.getDimension();
    final int numAxis = Math.min(this.numAxis, numAxis2);
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < numAxis; j++) {
        final double coordinate = points.getOrdinate(i, j);
        newPoints.setValue(i, j, coordinate);
      }
    }
    return newPoints;
  }

  public CoordinatesList createCoordinatesList(final CoordinatesList points) {
    final int size = points.size();
    final CoordinatesList newPoints = new DoubleCoordinatesList(size,
      this.numAxis);
    final byte numAxis2 = points.getNumAxis();
    final int numAxis = Math.min(this.numAxis, numAxis2);
    points.copy(0, newPoints, 0, numAxis, size);
    return newPoints;
  }

  public CoordinatesList createCoordinatesList(final double... coordinates) {
    final CoordinatesList newPoints = new DoubleCoordinatesList(this.numAxis,
      coordinates);
    return newPoints;
  }

  public CoordinatesList createCoordinatesList(final int size) {
    final CoordinatesList points = new DoubleCoordinatesList(size, this.numAxis);
    return points;
  }

  public Geometry createEmptyGeometry() {
    return createPoint((Coordinate)null);
  }

  @Override
  public Geometry createGeometry(final Geometry geometry) {
    if (geometry == null) {
      return null;
    } else {
      final int srid = getSRID();
      final int geometrySrid = geometry.getSRID();
      if (srid == 0 && geometrySrid != 0) {
        final GeometryFactory geometryFactory = GeometryFactory.getFactory(
          geometrySrid, numAxis, getScaleXY(), getScaleZ());
        return geometryFactory.createGeometry(geometry);
      } else if (srid != 0 && geometrySrid != srid) {
        return GeometryProjectionUtil.perform(geometry, this);
      } else if (geometry instanceof MultiPoint) {
        final List<Point> geometries = new ArrayList<Point>();
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
          final Point subGeometry = (Point)geometry.getGeometryN(i);
          final Point newSubGeometry = createPoint(subGeometry);
          geometries.add(newSubGeometry);
        }
        return createMultiPoint(geometries);
      } else if (geometry instanceof MultiLineString) {
        final List<LineString> geometries = new ArrayList<LineString>();
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
          final LineString subGeometry = (LineString)geometry.getGeometryN(i);
          final LineString newSubGeometry = createLineString(subGeometry);
          geometries.add(newSubGeometry);
        }
        return createMultiLineString(geometries);
      } else if (geometry instanceof MultiPolygon) {
        final List<Polygon> geometries = new ArrayList<Polygon>();
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
          final Polygon subGeometry = (Polygon)geometry.getGeometryN(i);
          final Polygon newSubGeometry = createPolygon(subGeometry);
          geometries.add(newSubGeometry);
        }
        return createMultiPolygon(geometries);
      } else if (geometry instanceof GeometryCollection) {
        final List<Geometry> geometries = new ArrayList<Geometry>();
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
          final Geometry subGeometry = geometry.getGeometryN(i);
          final Geometry newSubGeometry = createGeometry(subGeometry);
          geometries.add(newSubGeometry);
        }
        return createGeometryCollection(geometries);
      } else if (geometry instanceof Point) {
        final Point point = (Point)geometry;
        return createPoint(point);
      } else if (geometry instanceof Point) {
        final Point point = (Point)geometry;
        return createPoint(point);
      } else if (geometry instanceof LinearRing) {
        final LinearRing linearRing = (LinearRing)geometry;
        return createLinearRing(linearRing);
      } else if (geometry instanceof LineString) {
        final LineString lineString = (LineString)geometry;
        return createLineString(lineString);
      } else if (geometry instanceof Polygon) {
        final Polygon polygon = (Polygon)geometry;
        return createPolygon(polygon);
      } else {
        throw new RuntimeException("Unknown geometry type " + geometry);
      }
    }
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

  @SuppressWarnings("unchecked")
  public <T extends Geometry> T createGeometry(final String wkt) {
    return (T)wktParser.parseGeometry(wkt);
  }

  public GeometryCollection createGeometryCollection(
    final List<Geometry> geometries) {
    final Geometry[] array = new Geometry[geometries.size()];
    geometries.toArray(array);
    return createGeometryCollection(array);
  }

  public LinearRing createLinearRing(final CoordinatesList points) {
    points.makePrecise(coordinatesPrecisionModel);
    return super.createLinearRing(points);
  }

  public LinearRing createLinearRing(final double... coordinates) {
    final CoordinatesList points = createCoordinatesList(coordinates);
    return createLinearRing(points);
  }

  public LinearRing createLinearRing(final LinearRing linearRing) {
    final CoordinatesList points = CoordinatesListUtil.get(linearRing);
    final CoordinatesList newPoints = createCoordinatesList(points);
    return createLinearRing(newPoints);
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

  public LineString createLineString(final double... coordinates) {
    final CoordinatesList points = createCoordinatesList(coordinates);
    return createLineString(points);
  }

  public LineString createLineString(final LineString lineString) {
    final CoordinatesList points = CoordinatesListUtil.get(lineString);
    final CoordinatesList newPoints = createCoordinatesList(points);
    return createLineString(newPoints);
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

  @Override
  public MultiLineString createMultiLineString(final LineString... lines) {
    return super.createMultiLineString(lines);
  }

  public MultiLineString createMultiLineString(final List<?> lines) {
    final LineString[] lineArray = toLineStringArray(this, lines);
    return createMultiLineString(lineArray);
  }

  public MultiPoint createMultiPoint(final Collection<?> points) {
    final Point[] pointArray = toPointArray(this, points);
    return createMultiPoint(pointArray);
  }

  public MultiPoint createMultiPoint(final CoordinatesList points) {
    if (points != null) {
      points.makePrecise(coordinatesPrecisionModel);
    }
    return super.createMultiPoint(points);
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

  public Point createPoint(final double... coordinates) {
    final DoubleCoordinates coords = new DoubleCoordinates(numAxis, coordinates);
    return createPoint(coordinates);
  }

  public Point createPoint(final Point point) {
    final CoordinatesList points = CoordinatesListUtil.get(point);
    final CoordinatesList newPoints = createCoordinatesList(points);
    return createPoint(newPoints);

  }

  public Polygon createPolygon(final CoordinatesList... rings) {
    final List<CoordinatesList> ringList = Arrays.asList(rings);
    return createPolygon(ringList);
  }

  public Polygon createPolygon(final List<?> rings) {
    if (rings.size() == 0) {
      final DoubleCoordinatesList nullPoints = new DoubleCoordinatesList(0,
        numAxis);
      final LinearRing ring = createLinearRing(nullPoints);
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

  public Polygon createPolygon(final Polygon polygon) {
    final List<LinearRing> rings = new ArrayList<LinearRing>();
    final LinearRing exteriorRing = (LinearRing)polygon.getExteriorRing();
    final LinearRing newExteriorRing = createLinearRing(exteriorRing);
    rings.add(newExteriorRing);
    for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
      final LinearRing interiorRing = (LinearRing)polygon.getInteriorRingN(i);
      final LinearRing newInteriorRing = createLinearRing(interiorRing);
      rings.add(newInteriorRing);

    }
    return createPolygon(rings);
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
    } else if (ring instanceof LineString) {
      final LineString line = (LineString)ring;
      final CoordinatesList points = CoordinatesListUtil.get(line);
      return createLinearRing(points);
    } else if (ring instanceof double[]) {
      final double[] coordinates = (double[])ring;
      return createLinearRing(coordinates);
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

  public boolean hasM() {
    return numAxis > 3;
  }

  public boolean hasZ() {
    return numAxis > 2;
  }

  public boolean isFloating() {
    return coordinatesPrecisionModel.isFloating();
  }

  public void makePrecise(final Coordinates point) {
    coordinatesPrecisionModel.makePrecise(point);
  }

  public double makeXyPrecise(final double value) {
    return coordinatesPrecisionModel.makeXyPrecise(value);
  }

  public double makeZPrecise(final double value) {
    return coordinatesPrecisionModel.makeZPrecise(value);
  }

  @Override
  public String toString() {
    if (coordinateSystem == null) {
      return "Unknown coordinate system";
    } else {
      if (coordinatesPrecisionModel.isFloating()) {
        return coordinateSystem.getName() + " (" + coordinateSystem.getId()
          + ")";
      } else {
        return coordinateSystem.getName() + " (" + coordinateSystem.getId()
          + ") " + coordinatesPrecisionModel;
      }
    }
  }

  public GeometryCollection createCollection(Geometry... geometries) {
    List<Geometry> list = new ArrayList<Geometry>();
    for (Geometry geometry : geometries) {
      for (int i = 0; i < geometry.getNumGeometries(); i++) {
        list.add(geometry.getGeometryN(i));
      }
    }
    return createGeometryCollection(geometries);
  }
}
