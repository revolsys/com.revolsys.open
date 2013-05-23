package com.revolsys.gis.cs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.cs.projection.GeometryProjectionUtil;
import com.revolsys.gis.model.coordinates.CoordinateCoordinates;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.PrecisionModelUtil;
import com.revolsys.gis.model.coordinates.SimpleCoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesListFactory;
import com.revolsys.io.wkt.WktParser;
import com.revolsys.util.CollectionUtil;
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

  /** The cached geometry factories. */
  private static Map<String, GeometryFactory> factories = new HashMap<String, GeometryFactory>();

  private static final long serialVersionUID = 4328651897279304108L;

  public static void clear() {
    factories.clear();
  }

  /**
   * <p>Get a GeometryFactory with no coordinate system, 3D axis (x, y &amp; z) and a floating precision model.</p>
   * 
   * @return The geometry factory.
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

  /**
   * <p>Get a GeometryFactory with no coordinate system, 3D axis (x, y &amp; z) and a fixed x, y & floating z precision models.</p>
   * 
   * @param scaleXy The scale factor used to round the x, y coordinates. The precision is 1 / scaleXy.
   * A scale factor of 1000 will give a precision of 1 / 1000 = 1mm for projected coordinate systems using metres.
   * @return The geometry factory.
   */
  public static GeometryFactory getFactory(final double scaleXy) {
    return getFactory(0, 3, scaleXy, 0);
  }

  // Get the geometry factory from an existing geometry
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
   * <p>Get a GeometryFactory with the coordinate system, 3D axis (x, y &amp; z) and a floating precision models.</p>
   * 
   * @param srid The <a href="http://spatialreference.org/ref/epsg/">EPSG coordinate system id</a>. 
   * @return The geometry factory.
   */
  public static GeometryFactory getFactory(final int srid) {
    return getFactory(srid, 3, 0, 0);
  }

  /**
   * <p>Get a GeometryFactory with the coordinate system, 2D axis (x &amp; y) and a fixed x, y precision model.</p>
   * 
   * @param srid The <a href="http://spatialreference.org/ref/epsg/">EPSG coordinate system id</a>. 
   * @param scaleXy The scale factor used to round the x, y coordinates. The precision is 1 / scaleXy.
   * A scale factor of 1000 will give a precision of 1 / 1000 = 1mm for projected coordinate systems using metres.
   * @return The geometry factory.
   */
  public static GeometryFactory getFactory(final int srid, final double scaleXy) {
    return getFactory(srid, 2, scaleXy, 0);
  }

  /**
   * <p>Get a GeometryFactory with no coordinate system, 3D axis (x, y &amp; z) and a fixed x, y &amp; floating z precision models.</p>
   * 
   * @param srid The <a href="http://spatialreference.org/ref/epsg/">EPSG coordinate system id</a>. 
   * @param scaleXy The scale factor used to round the x, y coordinates. The precision is 1 / scaleXy.
   * A scale factor of 1000 will give a precision of 1 / 1000 = 1mm for projected coordinate systems using metres.
   * @param scaleZ The scale factor used to round the z coordinates. The precision is 1 / scaleZ.
   * A scale factor of 1000 will give a precision of 1 / 1000 = 1mm for projected coordinate systems using metres.
   * @return The geometry factory.
   */
  public static GeometryFactory getFactory(final int srid,
    final double scaleXy, final double scaleZ) {
    return getFactory(srid, 3, scaleXy, scaleZ);
  }

  /**
   * <p>Get a GeometryFactory with the coordinate system, number of axis and a floating precision model.</p>
   * 
   * @param srid The <a href="http://spatialreference.org/ref/epsg/">EPSG coordinate system id</a>. 
   * @param numAxis The number of coordinate axis. 2 for 2D x &amp; y coordinates. 3 for 3D x, y &amp; z coordinates.
   * @return The geometry factory.
   */
  public static GeometryFactory getFactory(final int srid, final int numAxis) {
    return getFactory(srid, numAxis, 0, 0);
  }

  /**
   * <p>Get a GeometryFactory with the coordinate system, number of axis and a fixed x, y &amp; fixed z precision models.</p>
   * 
   * @param srid The <a href="http://spatialreference.org/ref/epsg/">EPSG coordinate system id</a>. 
   * @param numAxis The number of coordinate axis. 2 for 2D x &amp; y coordinates. 3 for 3D x, y &amp; z coordinates.
   * @param scaleXy The scale factor used to round the x, y coordinates. The precision is 1 / scaleXy.
   * A scale factor of 1000 will give a precision of 1 / 1000 = 1mm for projected coordinate systems using metres.
   * @param scaleZ The scale factor used to round the z coordinates. The precision is 1 / scaleZ.
   * A scale factor of 1000 will give a precision of 1 / 1000 = 1mm for projected coordinate systems using metres.
   * @return The geometry factory.
   */
  public static GeometryFactory getFactory(final int crsId, final int numAxis,
    final double scaleXY, final double scaleZ) {
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
    final Collection<? extends Geometry> geometries) {
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

  private final CoordinatesPrecisionModel coordinatesPrecisionModel;

  private final CoordinateSystem coordinateSystem;

  private int numAxis = 2;

  protected GeometryFactory(final int crsId, final int numAxis,
    final double scaleXY, final double scaleZ) {
    super(PrecisionModelUtil.getPrecisionModel(scaleXY), crsId,
      new DoubleCoordinatesListFactory());
    this.coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(crsId);
    this.coordinatesPrecisionModel = new SimpleCoordinatesPrecisionModel(
      scaleXY, scaleZ);
    this.numAxis = numAxis;
  }

  @SuppressWarnings("unchecked")
  public <G extends Geometry> G copy(final G geometry) {
    return (G)createGeometry(geometry);
  }

  public GeometryCollection createCollection(final Geometry... geometries) {
    final List<Geometry> list = new ArrayList<Geometry>();
    for (final Geometry geometry : geometries) {
      for (int i = 0; i < geometry.getNumGeometries(); i++) {
        list.add(geometry.getGeometryN(i));
      }
    }
    return createGeometryCollection(geometries);
  }

  public Coordinates createCoordinates(final Coordinates point) {
    final Coordinates newPoint = new DoubleCoordinates(point, this.numAxis);
    makePrecise(newPoint);
    return newPoint;
  }

  public Coordinates createCoordinates(final double... coordinates) {
    final Coordinates newPoint = new DoubleCoordinates(this.numAxis,
      coordinates);
    makePrecise(newPoint);
    return newPoint;
  }

  public CoordinatesList createCoordinatesList(final Collection<?> points) {
    if (points == null || points.isEmpty()) {
      return null;
    } else {
      final int numPoints = points.size();
      final int numAxis = getNumAxis();
      final CoordinatesList coordinatesList = new DoubleCoordinatesList(
        numPoints, numAxis);
      int i = 0;
      for (final Object object : points) {
        Coordinates point;
        if (object instanceof Coordinates) {
          point = (Coordinates)object;
        } else if (object instanceof Point) {
          point = CoordinatesUtil.get((Point)object);
        } else if (object instanceof double[]) {
          point = new DoubleCoordinates((double[])object);
        } else if (object instanceof Coordinate) {
          point = new CoordinateCoordinates((Coordinate)object);
        } else if (object instanceof CoordinatesList) {
          final CoordinatesList coordinates = (CoordinatesList)object;
          point = coordinates.get(0);
        } else if (object instanceof CoordinateSequence) {
          final CoordinateSequence coordinates = (CoordinateSequence)object;
          point = new CoordinateCoordinates(coordinates.getCoordinate(0));
        } else {
          throw new IllegalArgumentException("Unexepected data type: " + object);
        }

        coordinatesList.setPoint(i, point);
        i++;
      }
      return coordinatesList;
    }
  }

  public CoordinatesList createCoordinatesList(final Coordinates... points) {
    return new DoubleCoordinatesList(numAxis, points);
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

  protected GeometryCollection createEmptyGeometryCollection() {
    return new GeometryCollection(null, this);
  }

  public Geometry createGeometry(final Collection<? extends Geometry> geometries) {
    if (geometries == null || geometries.size() == 0) {
      return createEmptyGeometryCollection();
    } else if (geometries.size() == 1) {
      return CollectionUtil.get(geometries, 0);
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
        return GeometryProjectionUtil.performCopy(geometry, this);
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

  @SuppressWarnings("unchecked")
  public <T extends Geometry> T createGeometry(final String wkt) {
    final WktParser parser = new WktParser(this);
    return (T)parser.parseGeometry(wkt);
  }

  @SuppressWarnings("unchecked")
  public <T extends Geometry> T createGeometry(final String wkt,
    final boolean useNumAxisFromGeometryFactory) {
    final WktParser parser = new WktParser(this);
    return (T)parser.parseGeometry(wkt, useNumAxisFromGeometryFactory);
  }

  public GeometryCollection createGeometryCollection(
    final List<? extends Geometry> geometries) {
    if (geometries == null || geometries.size() == 0) {
      return createEmptyGeometryCollection();
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

  public LinearRing createLinearRing(final Collection<?> points) {
    final CoordinatesList coordinatesList = createCoordinatesList(points);
    return createLinearRing(coordinatesList);
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

  public LinearRing createLinearRing(final Object... points) {
    return createLinearRing(Arrays.asList(points));
  }

  public LineString createLineString() {
    final DoubleCoordinatesList points = new DoubleCoordinatesList(0,
      getNumAxis());
    return createLineString(points);
  }

  public LineString createLineString(final Collection<?> points) {
    final CoordinatesList coordinatesList = createCoordinatesList(points);
    return createLineString(coordinatesList);
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

  public LineString createLineString(final Object... points) {
    return createLineString(Arrays.asList(points));
  }

  public MultiLineString createMultiLineString(final Collection<?> lines) {
    final LineString[] lineArray = toLineStringArray(lines);
    return createMultiLineString(lineArray);
  }

  @Override
  public MultiLineString createMultiLineString(final LineString... lines) {
    return super.createMultiLineString(lines);
  }

  public MultiPoint createMultiPoint(final Collection<?> points) {
    final CoordinatesList coordinatesList = createCoordinatesList(points);
    return createMultiPoint(coordinatesList);
  }

  public MultiPoint createMultiPoint(final CoordinatesList coordinatesList) {
    if (coordinatesList != null) {
      coordinatesList.makePrecise(coordinatesPrecisionModel);
    }
    Point[] points = new Point[coordinatesList.size()];
    for (int i = 0; i < points.length; i++) {
      Coordinates coordinates = coordinatesList.get(i);
      Point point = createPoint(coordinates);
      points[i] = point;
    }
    return super.createMultiPoint(points);
  }

  public MultiPoint createMultiPoint(final Object... points) {
    return createMultiPoint(Arrays.asList(points));
  }

  public MultiPolygon createMultiPolygon(final Collection<?> polygons) {
    final Polygon[] polygonArray = toPolygonArray(polygons);
    return createMultiPolygon(polygonArray);
  }

  public Point createPoint() {
    final DoubleCoordinatesList points = new DoubleCoordinatesList(0,
      getNumAxis());
    return createPoint(points);
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
    return createPoint(coords);
  }

  public Point createPoint(final Point point) {
    final CoordinatesList points = CoordinatesListUtil.get(point);
    final CoordinatesList newPoints = createCoordinatesList(points);
    return createPoint(newPoints);

  }

  public Polygon createPolygon() {
    final DoubleCoordinatesList points = new DoubleCoordinatesList(0,
      getNumAxis());
    return createPolygon(points);
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

  public Polygon createPolygon(final Object... rings) {
    return createPolygon(Arrays.asList(rings));
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

  @Override
  public Coordinates getPreciseCoordinates(final Coordinates point) {
    return coordinatesPrecisionModel.getPreciseCoordinates(point);
  }

  @Override
  public double getScaleXY() {
    final CoordinatesPrecisionModel precisionModel = getCoordinatesPrecisionModel();
    return precisionModel.getScaleXY();
  }

  @Override
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

  @Override
  public boolean isFloating() {
    return coordinatesPrecisionModel.isFloating();
  }

  @Override
  public void makePrecise(final Coordinates point) {
    coordinatesPrecisionModel.makePrecise(point);
  }

  public double makePrecise(final double value) {
    return getPrecisionModel().makePrecise(value);
  }

  @Override
  public double makeXyPrecise(final double value) {
    return coordinatesPrecisionModel.makeXyPrecise(value);
  }

  @Override
  public double makeZPrecise(final double value) {
    return coordinatesPrecisionModel.makeZPrecise(value);
  }

  /**
   * Project the geometry if it is in a different coordinate system
   * @param geometry
   * @return
   */
  public <G extends Geometry> G project(final G geometry) {
    return GeometryProjectionUtil.perform(geometry, this);
  }

  public LineString[] toLineStringArray(final Collection<?> lines) {
    final LineString[] lineStrings = new LineString[lines.size()];
    final Iterator<?> iterator = lines.iterator();
    for (int i = 0; i < lines.size(); i++) {
      final Object value = iterator.next();
      if (value instanceof LineString) {
        final LineString lineString = (LineString)value;
        lineStrings[i] = lineString;
      } else if (value instanceof CoordinatesList) {
        final CoordinatesList coordinates = (CoordinatesList)value;
        lineStrings[i] = createLineString(coordinates);
      } else if (value instanceof CoordinateSequence) {
        final CoordinateSequence coordinates = (CoordinateSequence)value;
        lineStrings[i] = createLineString(coordinates);
      } else if (value instanceof double[]) {
        final double[] points = (double[])value;
        lineStrings[i] = createLineString(points);
      }
    }
    return lineStrings;
  }

  @SuppressWarnings("unchecked")
  public Polygon[] toPolygonArray(final Collection<?> polygonList) {
    final Polygon[] polygons = new Polygon[polygonList.size()];
    int i = 0;
    for (final Object value : polygonList) {
      if (value instanceof Polygon) {
        final Polygon polygon = (Polygon)value;
        polygons[i] = polygon;
      } else if (value instanceof List) {
        final List<CoordinatesList> coordinateList = (List<CoordinatesList>)value;
        polygons[i] = createPolygon(coordinateList);
      }
      i++;
    }
    return polygons;
  }

  @Override
  public String toString() {
    if (coordinateSystem == null) {
      return "Unknown coordinate system";
    } else {
      final StringBuffer string = new StringBuffer(coordinateSystem.getName());
      final int srid = coordinateSystem.getId();
      string.append(", srid=");
      string.append(srid);
      string.append(", numAxis=");
      string.append(numAxis);
      final double scaleXY = coordinatesPrecisionModel.getScaleXY();
      string.append(", scaleXy=");
      if (scaleXY <= 0) {
        string.append("floating");
      } else {
        string.append(scaleXY);
      }
      if (hasZ()) {
        final double scaleZ = coordinatesPrecisionModel.getScaleZ();
        string.append(", scaleZ=");
        if (scaleZ <= 0) {
          string.append("floating");
        } else {
          string.append(scaleZ);
        }
      }
      return string.toString();
    }
  }

}
