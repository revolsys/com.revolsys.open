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
import com.revolsys.gis.cs.esri.EsriCoordinateSystems;
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
import com.vividsolutions.jts.operation.linemerge.LineMerger;

public class GeometryFactory extends
  com.vividsolutions.jts.geom.GeometryFactory implements
  CoordinatesPrecisionModel {

  /** The cached geometry factories. */
  private static Map<String, GeometryFactory> factories = new HashMap<String, GeometryFactory>();

  private static final long serialVersionUID = 4328651897279304108L;

  public static final GeometryFactory WORLD_MERCATOR = getFactory(3857);

  public static final GeometryFactory WGS84 = getFactory(4326);

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
    final int srid = getId(coordinateSystem);
    return getFactory(srid, 3, 0, 0);
  }

  public static GeometryFactory getFactory(
    final CoordinateSystem coordinateSystem, final int numAxis,
    final double scaleXY, final double scaleZ) {
    return new GeometryFactory(coordinateSystem, numAxis, scaleXY, scaleZ);
  }

  /**
   * <p>Get a GeometryFactory with no coordinate system, 3D axis (x, y &amp; z) and a fixed x, y & floating z precision models.</p>
   * 
   * @param scaleXY The scale factor used to round the x, y coordinates. The precision is 1 / scaleXy.
   * A scale factor of 1000 will give a precision of 1 / 1000 = 1mm for projected coordinate systems using metres.
   * @return The geometry factory.
   */
  public static GeometryFactory getFactory(final double scaleXY) {
    return getFactory(0, 3, scaleXY, 0);
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
        final int srid = geometry.getSRID();
        final PrecisionModel precisionModel = factory.getPrecisionModel();
        if (precisionModel.isFloating()) {
          return getFactory(srid, 3, 0, 0);
        } else {
          final double scaleXY = precisionModel.getScale();
          return getFactory(srid, 3, scaleXY, 0);
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
   * @param scaleXY The scale factor used to round the x, y coordinates. The precision is 1 / scaleXy.
   * A scale factor of 1000 will give a precision of 1 / 1000 = 1mm for projected coordinate systems using metres.
   * @return The geometry factory.
   */
  public static GeometryFactory getFactory(final int srid, final double scaleXY) {
    return getFactory(srid, 2, scaleXY, 0);
  }

  /**
   * <p>Get a GeometryFactory with no coordinate system, 3D axis (x, y &amp; z) and a fixed x, y &amp; floating z precision models.</p>
   * 
   * @param srid The <a href="http://spatialreference.org/ref/epsg/">EPSG coordinate system id</a>. 
   * @param scaleXY The scale factor used to round the x, y coordinates. The precision is 1 / scaleXy.
   * A scale factor of 1000 will give a precision of 1 / 1000 = 1mm for projected coordinate systems using metres.
   * @param scaleZ The scale factor used to round the z coordinates. The precision is 1 / scaleZ.
   * A scale factor of 1000 will give a precision of 1 / 1000 = 1mm for projected coordinate systems using metres.
   * @return The geometry factory.
   */
  public static GeometryFactory getFactory(final int srid,
    final double scaleXY, final double scaleZ) {
    return getFactory(srid, 3, scaleXY, scaleZ);
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
   * @param scaleXY The scale factor used to round the x, y coordinates. The precision is 1 / scaleXy.
   * A scale factor of 1000 will give a precision of 1 / 1000 = 1mm for projected coordinate systems using metres.
   * @param scaleZ The scale factor used to round the z coordinates. The precision is 1 / scaleZ.
   * A scale factor of 1000 will give a precision of 1 / 1000 = 1mm for projected coordinate systems using metres.
   * @return The geometry factory.
   */
  public static GeometryFactory getFactory(final int srid, final int numAxis,
    final double scaleXY, final double scaleZ) {
    synchronized (factories) {
      final String key = srid + "-" + numAxis + "-" + scaleXY + "-" + scaleZ;
      GeometryFactory factory = factories.get(key);
      if (factory == null) {
        factory = new GeometryFactory(srid, numAxis, scaleXY, scaleZ);
        factories.put(key, factory);
      }
      return factory;
    }
  }

  /**
   * <p>Get a GeometryFactory with the coordinate system, 3D axis (x, y &amp; z) and a floating precision models.</p>
   * 
   * @param srid The <a href="http://spatialreference.org/ref/epsg/">EPSG coordinate system id</a>. 
   * @return The geometry factory.
   */
  public static GeometryFactory getFactory(final String wkt) {
    final CoordinateSystem esriCoordinateSystem = EsriCoordinateSystems.getCoordinateSystem(wkt);
    if (esriCoordinateSystem == null) {
      return getFactory();
    } else {
      final CoordinateSystem epsgCoordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(esriCoordinateSystem);
      final int srid = epsgCoordinateSystem.getId();
      return getFactory(srid, 3, 0, 0);
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

  protected GeometryFactory(final CoordinateSystem coordinateSystem,
    final int numAxis, final double scaleXY, final double scaleZ) {
    super(PrecisionModelUtil.getPrecisionModel(scaleXY),
      coordinateSystem.getId(), new DoubleCoordinatesListFactory());
    this.coordinateSystem = coordinateSystem;
    this.coordinatesPrecisionModel = new SimpleCoordinatesPrecisionModel(
      scaleXY, scaleZ);
    this.numAxis = numAxis;
  }

  protected GeometryFactory(final int srid, final int numAxis,
    final double scaleXY, final double scaleZ) {
    super(PrecisionModelUtil.getPrecisionModel(scaleXY), srid,
      new DoubleCoordinatesListFactory());
    this.coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(srid);
    this.coordinatesPrecisionModel = new SimpleCoordinatesPrecisionModel(
      scaleXY, scaleZ);
    this.numAxis = numAxis;
  }

  public void addGeometries(final List<Geometry> geometryList,
    final Geometry geometry) {
    if (geometry != null && !geometry.isEmpty()) {
      for (int i = 0; i < geometry.getNumGeometries(); i++) {
        final Geometry part = geometry.getGeometryN(i);
        if (part != null && !part.isEmpty()) {
          geometryList.add(copy(part));
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  public <G extends Geometry> G copy(final G geometry) {
    return (G)createGeometry(geometry);
  }

  @SuppressWarnings("unchecked")
  public <V extends GeometryCollection> V createCollection(
    final Geometry... geometries) {
    return (V)createGeometryCollection(Arrays.asList(geometries));
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
      CoordinatesList coordinatesList = new DoubleCoordinatesList(numPoints,
        numAxis);
      int i = 0;
      for (final Object object : points) {
        Coordinates point;
        if (object == null) {
          point = null;
        } else if (object instanceof Coordinates) {
          point = (Coordinates)object;
        } else if (object instanceof Point) {
          final Point projectedPoint = copy((Point)object);
          point = CoordinatesUtil.get(projectedPoint);
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

        if (point != null) {
          coordinatesList.setPoint(i, point);
          i++;
        }
      }
      if (i < coordinatesList.size() - 1) {
        coordinatesList = coordinatesList.subList(0, i);
      }
      makePrecise(coordinatesList);
      return coordinatesList;
    }
  }

  public CoordinatesList createCoordinatesList(final Coordinates... points) {
    final DoubleCoordinatesList coordinatesList = new DoubleCoordinatesList(
      getNumAxis(), points);
    coordinatesList.makePrecise(coordinatesPrecisionModel);
    return coordinatesList;
  }

  public CoordinatesList createCoordinatesList(final CoordinateSequence points) {
    if (points == null) {
      return null;
    } else {
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
      makePrecise(newPoints);
      return newPoints;
    }
  }

  public CoordinatesList createCoordinatesList(final CoordinatesList points) {
    if (points == null) {
      return null;
    } else {
      final CoordinatesList newPoints = new DoubleCoordinatesList(getNumAxis(),
        points);
      makePrecise(newPoints);
      return newPoints;
    }
  }

  public CoordinatesList createCoordinatesList(final double... coordinates) {
    final CoordinatesList newPoints = new DoubleCoordinatesList(this.numAxis,
      coordinates);
    makePrecise(newPoints);
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

  /**
   * <p>Create a new geometry of the requested target geometry class.<p>
   * @param targetClass
   * @param geometry
   * @return
   */
  @SuppressWarnings({
    "unchecked"
  })
  public <V extends Geometry> V createGeometry(final Class<?> targetClass,
    Geometry geometry) {
    if (geometry != null && !geometry.isEmpty()) {
      geometry = copy(geometry);
      if (geometry instanceof GeometryCollection) {
        if (geometry.getNumGeometries() == 1) {
          geometry = geometry.getGeometryN(0);
        } else {
          geometry = geometry.union();
          // Union doesn't use this geometry factory
          geometry = copy(geometry);
        }
      }
      final Class<?> geometryClass = geometry.getClass();
      if (targetClass.isAssignableFrom(geometryClass)) {
        // TODO if geometry collection then clean up
        return (V)geometry;
      } else if (Point.class.isAssignableFrom(targetClass)) {
        if (geometry instanceof MultiPoint) {
          if (geometry.getNumGeometries() == 1) {
            return (V)geometry.getGeometryN(0);
          }
        }
      } else if (LineString.class.isAssignableFrom(targetClass)) {
        if (geometry instanceof MultiLineString) {
          if (geometry.getNumGeometries() == 1) {
            return (V)geometry.getGeometryN(0);
          } else {
            final LineMerger merger = new LineMerger();
            merger.add(geometry);
            final List<LineString> mergedLineStrings = (List<LineString>)merger.getMergedLineStrings();
            if (mergedLineStrings.size() == 1) {
              return (V)mergedLineStrings.get(0);
            }
          }
        }
      } else if (Polygon.class.isAssignableFrom(targetClass)) {
        if (geometry instanceof MultiPolygon) {
          if (geometry.getNumGeometries() == 1) {
            return (V)geometry.getGeometryN(0);
          }
        }
      } else if (MultiPoint.class.isAssignableFrom(targetClass)) {
        if (geometry instanceof Point) {
          return (V)createMultiPoint(geometry);
        }
      } else if (MultiLineString.class.isAssignableFrom(targetClass)) {
        if (geometry instanceof LineString) {
          return (V)createMultiLineString(geometry);
        }
      } else if (MultiPolygon.class.isAssignableFrom(targetClass)) {
        if (geometry instanceof Polygon) {
          return (V)createMultiPolygon(geometry);
        }
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public <V extends Geometry> V createGeometry(
    final Collection<? extends Geometry> geometries) {
    final Collection<? extends Geometry> geometryList = getGeometries(geometries);
    if (geometryList == null || geometries.size() == 0) {
      return (V)createEmptyGeometryCollection();
    } else if (geometries.size() == 1) {
      return (V)CollectionUtil.get(geometries, 0);
    } else {
      final Set<Class<?>> classes = getGeometryClassSet(geometries);
      if (classes.equals(Collections.singleton(Point.class))) {
        return (V)createMultiPoint(geometries);
      } else if (classes.equals(Collections.singleton(LineString.class))) {
        return (V)createMultiLineString(geometries);
      } else if (classes.equals(Collections.singleton(Polygon.class))) {
        return (V)createMultiPolygon(geometries);
      } else {
        final Geometry[] geometryArray = com.vividsolutions.jts.geom.GeometryFactory.toGeometryArray(geometries);
        return (V)createGeometryCollection(geometryArray);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public <V extends Geometry> V createGeometry(final Geometry... geometries) {
    return (V)createGeometry(Arrays.asList(geometries));
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
        if (geometry instanceof GeometryCollection) {
          final List<Geometry> geometries = new ArrayList<Geometry>();
          addGeometries(geometries, geometry);
          return createGeometryCollection(geometries);
        } else {
          return GeometryProjectionUtil.performCopy(geometry, this);
        }
      } else if (geometry instanceof GeometryCollection) {
        final List<Geometry> geometries = new ArrayList<Geometry>();
        addGeometries(geometries, geometry);
        return createGeometryCollection(geometries);
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
        return null;
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

  @SuppressWarnings("unchecked")
  public <V extends GeometryCollection> V createGeometryCollection(
    final Collection<? extends Geometry> geometries) {
    final List<Geometry> geometryList = getGeometries(geometries);
    if (geometryList == null || geometryList.size() == 0) {
      return (V)createEmptyGeometryCollection();
    } else {
      final Set<Class<?>> classes = getGeometryClassSet(geometryList);
      if (classes.equals(Collections.singleton(Point.class))) {
        return (V)createMultiPoint(geometryList);
      } else if (classes.equals(Collections.singleton(LineString.class))) {
        return (V)createMultiLineString(geometryList);
      } else if (classes.equals(Collections.singleton(Polygon.class))) {
        return (V)createMultiPolygon(geometryList);
      } else {
        final Geometry[] geometryArray = GeometryFactory.toGeometryArray(geometryList);
        return (V)super.createGeometryCollection(geometryArray);
      }
    }
  }

  @Deprecated
  @Override
  public GeometryCollection createGeometryCollection(final Geometry[] geometries) {
    return super.createGeometryCollection(geometries);
  }

  public LinearRing createLinearRing(final Collection<?> points) {
    final CoordinatesList coordinatesList = createCoordinatesList(points);
    return createLinearRing(coordinatesList);
  }

  public LinearRing createLinearRing(final CoordinatesList points) {
    final CoordinatesList coordinatesList = createCoordinatesList(points);
    return super.createLinearRing(coordinatesList);
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
    final CoordinatesList newPoints = createCoordinatesList(points);
    final LineString line = super.createLineString(newPoints);
    return line;
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
    final LineString[] lineArray = getLineStringArray(lines);
    return createMultiLineString(lineArray);
  }

  public MultiLineString createMultiLineString(final Object... lines) {
    return createMultiLineString(Arrays.asList(lines));
  }

  public MultiPoint createMultiPoint(final Collection<?> points) {
    final Point[] pointArray = getPointArray(points);
    return createMultiPoint(pointArray);
  }

  public MultiPoint createMultiPoint(final CoordinatesList coordinatesList) {
    if (coordinatesList != null) {
      makePrecise(coordinatesList);
    }
    final Point[] points = new Point[coordinatesList.size()];
    for (int i = 0; i < points.length; i++) {
      final Coordinates coordinates = coordinatesList.get(i);
      final Point point = createPoint(coordinates);
      points[i] = point;
    }
    return super.createMultiPoint(points);
  }

  public MultiPoint createMultiPoint(final Object... points) {
    return createMultiPoint(Arrays.asList(points));
  }

  public MultiPolygon createMultiPolygon(final Collection<?> polygons) {
    final Polygon[] polygonArray = getPolygonArray(polygons);
    return createMultiPolygon(polygonArray);
  }

  public MultiPolygon createMultiPolygon(final Object... polygons) {
    return createMultiPolygon(Arrays.asList(polygons));
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
      final CoordinatesList coordinatesList = createCoordinatesList(point);
      return super.createPoint(coordinatesList);
    }
  }

  public Point createPoint(final CoordinatesList points) {
    final CoordinatesList coordinatesList = createCoordinatesList(points);
    return super.createPoint(coordinatesList);
  }

  public Point createPoint(final double... coordinates) {
    final DoubleCoordinates coords = new DoubleCoordinates(numAxis, coordinates);
    makePrecise(coords);
    return createPoint(coords);
  }

  public Point createPoint(final Object object) {
    Coordinates coordinates;
    if (object instanceof Coordinates) {
      coordinates = (Coordinates)object;
    } else if (object instanceof Point) {
      return copy((Point)object);
    } else if (object instanceof double[]) {
      coordinates = new DoubleCoordinates((double[])object);
    } else if (object instanceof Coordinate) {
      coordinates = new CoordinateCoordinates((Coordinate)object);
    } else if (object instanceof CoordinatesList) {
      final CoordinatesList coordinatesList = (CoordinatesList)object;
      coordinates = coordinatesList.get(0);
    } else if (object instanceof CoordinateSequence) {
      final CoordinateSequence coordinatesList = (CoordinateSequence)object;
      coordinates = new CoordinateCoordinates(coordinatesList.getCoordinate(0));
    } else {
      coordinates = null;
    }
    return createPoint(coordinates);
  }

  public Point createPoint(final Point point) {
    final CoordinatesList points = CoordinatesListUtil.get(point);
    final CoordinatesList newPoints = createCoordinatesList(points);
    return createPoint(newPoints);

  }

  public List<Point> createPointList(final CoordinatesList sourcePoints) {
    final List<Point> points = new ArrayList<Point>(sourcePoints.size());
    for (final Coordinates coordinates : sourcePoints) {
      final Point point = createPoint(coordinates);
      points.add(point);
    }
    return points;
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

  public Coordinates getCoordinates(final Point point) {
    final Point convertedPoint = project(point);
    return CoordinatesUtil.get(convertedPoint);
  }

  public CoordinatesPrecisionModel getCoordinatesPrecisionModel() {
    return coordinatesPrecisionModel;
  }

  public CoordinateSystem getCoordinateSystem() {
    return coordinateSystem;
  }

  public GeometryFactory getGeographicGeometryFactory() {
    if (coordinateSystem instanceof GeographicCoordinateSystem) {
      return this;
    } else if (coordinateSystem instanceof ProjectedCoordinateSystem) {
      final ProjectedCoordinateSystem projectedCs = (ProjectedCoordinateSystem)coordinateSystem;
      final GeographicCoordinateSystem geographicCs = projectedCs.getGeographicCoordinateSystem();
      final int srid = geographicCs.getId();
      return getFactory(srid, getNumAxis(), 0, 0);
    } else {
      return getFactory(4326, getNumAxis(), 0, 0);
    }
  }

  public List<Geometry> getGeometries(
    final Collection<? extends Geometry> geometries) {
    final List<Geometry> geometryList = new ArrayList<Geometry>();
    for (final Geometry geometry : geometries) {
      addGeometries(geometryList, geometry);
    }
    return geometryList;
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
      final DoubleCoordinatesList points = new DoubleCoordinatesList(
        getNumAxis(), coordinates);
      return createLinearRing(points);
    } else {
      return null;
    }
  }

  public LineString[] getLineStringArray(final Collection<?> lines) {
    final List<LineString> lineStrings = new ArrayList<LineString>();
    for (final Object value : lines) {
      LineString lineString;
      if (value instanceof LineString) {
        lineString = (LineString)value;
      } else if (value instanceof CoordinatesList) {
        final CoordinatesList coordinates = (CoordinatesList)value;
        lineString = createLineString(coordinates);
      } else if (value instanceof CoordinateSequence) {
        final CoordinateSequence coordinates = (CoordinateSequence)value;
        lineString = createLineString(coordinates);
      } else if (value instanceof double[]) {
        final double[] points = (double[])value;
        lineString = createLineString(points);
      } else {
        lineString = null;
      }
      if (lineString != null) {
        lineStrings.add(lineString);
      }
    }
    return lineStrings.toArray(new LineString[lineStrings.size()]);
  }

  public int getNumAxis() {
    return numAxis;
  }

  public Point[] getPointArray(final Collection<?> pointsList) {
    final List<Point> points = new ArrayList<Point>();
    for (final Object object : pointsList) {
      final Point point = createPoint(object);
      if (point != null && !point.isEmpty()) {
        points.add(point);
      }
    }
    return points.toArray(new Point[points.size()]);
  }

  @SuppressWarnings("unchecked")
  public Polygon[] getPolygonArray(final Collection<?> polygonList) {
    final List<Polygon> polygons = new ArrayList<Polygon>();
    for (final Object value : polygonList) {
      Polygon polygon;
      if (value instanceof Polygon) {
        polygon = (Polygon)value;
      } else if (value instanceof List) {
        final List<CoordinatesList> coordinateList = (List<CoordinatesList>)value;
        polygon = createPolygon(coordinateList);
      } else if (value instanceof CoordinatesList) {
        final CoordinatesList coordinateList = (CoordinatesList)value;
        polygon = createPolygon(coordinateList);
      } else {
        polygon = null;
      }
      if (polygon != null) {
        polygons.add(polygon);
      }
    }
    return polygons.toArray(new Polygon[polygons.size()]);
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

  public void makePrecise(final CoordinatesList points) {
    points.makePrecise(coordinatesPrecisionModel);
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
