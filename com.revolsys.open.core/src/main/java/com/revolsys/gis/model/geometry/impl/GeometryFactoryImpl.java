package com.revolsys.gis.model.geometry.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.cs.projection.CoordinatesOperation;
import com.revolsys.gis.cs.projection.ProjectionFactory;
import com.revolsys.gis.model.coordinates.CoordinatesListCoordinates;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.SimpleCoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.gis.model.geometry.Geometry;
import com.revolsys.gis.model.geometry.GeometryCollection;
import com.revolsys.gis.model.geometry.LineString;
import com.revolsys.gis.model.geometry.LinearRing;
import com.revolsys.gis.model.geometry.MultiLineString;
import com.revolsys.gis.model.geometry.MultiLinearRing;
import com.revolsys.gis.model.geometry.MultiPoint;
import com.revolsys.gis.model.geometry.MultiPolygon;
import com.revolsys.gis.model.geometry.Point;
import com.revolsys.gis.model.geometry.Polygon;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;

public class GeometryFactoryImpl extends SimpleCoordinatesPrecisionModel
  implements com.revolsys.gis.model.geometry.GeometryFactoryI {
  private static Map<String, GeometryFactoryImpl> factories = new HashMap<String, GeometryFactoryImpl>();

  private static final long serialVersionUID = 4328651897279304108L;

  public static void clear() {
    factories.clear();
  }

  /**
   * get a 3d geometry factory with no coordinate system and a floating scale.
   */
  public static GeometryFactoryImpl getFactory() {
    return getFactory(0, 3, 0, 0);
  }

  /**
   * get a 3d geometry factory with a floating scale.
   */
  public static GeometryFactoryImpl getFactory(
    final CoordinateSystem coordinateSystem) {
    final int crsId = getId(coordinateSystem);
    return getFactory(crsId, 3, 0, 0);
  }

  public static GeometryFactoryImpl getFactory(final double scaleXy) {
    return getFactory(0, 3, scaleXy, 0);
  }

  /**
   * get a 3d geometry factory with a floating scale.
   */
  public static GeometryFactoryImpl getFactory(final int crsId) {
    return getFactory(crsId, 3, 0, 0);
  }

  public static GeometryFactoryImpl getFactory(final int crsId,
    final byte numAxis, final double scaleXY, final double scaleZ) {
    synchronized (factories) {
      final String key = crsId + "-" + numAxis + "-" + scaleXY + "-" + scaleZ;
      GeometryFactoryImpl factory = factories.get(key);
      if (factory == null) {
        factory = new GeometryFactoryImpl(crsId, numAxis, scaleXY, scaleZ);
        factories.put(key, factory);
      }
      return factory;
    }
  }

  /**
   * Get a 2D geometry factory with the specified scale
   * 
   * @param crsId
   * @param scale
   * @return
   */
  public static GeometryFactoryImpl getFactory(final int crsId,
    final double scale) {
    return getFactory(crsId, 2, scale, 0);
  }

  public static GeometryFactoryImpl getFactory(final int crsId,
    final double scaleXy, final double scaleZ) {
    return getFactory(crsId, 3, scaleXy, scaleZ);
  }

  /**
   * Get a 2D geometry factory with the specified scale
   * 
   * @param crsId
   * @param scale
   * @return
   */
  public static GeometryFactoryImpl getFactory(final int crsId,
    final int numAxis) {
    return getFactory(crsId, numAxis, 0, 0);
  }

  public static GeometryFactoryImpl getFactory(final int crsId,
    final int numAxis, final double scaleXY, final double scaleZ) {
    return getFactory(crsId, (byte)numAxis, scaleXY, scaleZ);
  }

  private static int getId(final CoordinateSystem coordinateSystem) {
    if (coordinateSystem == null) {
      return 0;
    } else {
      return coordinateSystem.getId();
    }
  }

  private final CoordinateSystem coordinateSystem;

  private byte numAxis = 2;

  private GeometryFactoryImpl(final int crsId, final byte numAxis,
    final double scaleXY, final double scaleZ) {
    super(scaleXY, scaleZ);
    this.coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(crsId);
    this.numAxis = numAxis;
  }

  protected CoordinatesList createCoordinatesList(
    final com.revolsys.gis.model.geometry.GeometryFactoryI factory,
    final CoordinatesList points) {
    final byte numAxis = getNumAxis();
    CoordinatesList newPoints;
    if (factory == this) {
      newPoints = new DoubleCoordinatesList(numAxis, points);
    } else {
      final CoordinateSystem sourceCoordinateSystem = factory.getCoordinateSystem();
      final CoordinatesOperation operation = ProjectionFactory.getCoordinatesOperation(
        sourceCoordinateSystem, getCoordinateSystem());
      final int size = points.size();
      if (operation == null) {
        newPoints = new DoubleCoordinatesList(numAxis, points);
      } else {
        newPoints = new DoubleCoordinatesList(size, numAxis);
        final CoordinatesListCoordinates sourceCoordinates = new CoordinatesListCoordinates(
          points);
        final CoordinatesListCoordinates targetCoordinates = new CoordinatesListCoordinates(
          newPoints);
        for (int i = 0; i < size; i++) {
          sourceCoordinates.setIndex(i);
          targetCoordinates.setIndex(i);
          operation.perform(sourceCoordinates, targetCoordinates);
        }
      }
      newPoints.makePrecise(this);
    }
    return newPoints;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <G extends Geometry> G createGeometry(
    final Collection<? extends Geometry> geometries) {
    if (geometries.isEmpty()) {
      return (G)createGeometryCollection();
    } else {
      final Class<? extends Geometry> geomClass = getGeometryInterface(geometries);
      if (geomClass == null) {
        return (G)createGeometryCollection(geometries);
      } else {
        final Geometry geometry = geometries.iterator().next();
        final boolean isCollection = geometries.size() > 1;
        if (isCollection) {
          if (geomClass == Polygon.class) {
            return (G)createMultiPolygon(geometries);
          } else if (geomClass == LinearRing.class) {
            return (G)createMultiLinearRing(geometries);
          } else if (geomClass == LineString.class) {
            return (G)createMultiLineString(geometries);
          } else if (geomClass == Point.class) {
            return (G)createMultiPoint(geometries);
          } else {
            throw new IllegalArgumentException("Unhandled class: "
              + geometry.getClass());
          }
        }
        return (G)geometry;
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <G extends Geometry> G createGeometry(final Geometry geometry) {
    if (geometry == null) {
      return null;
    } else if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      return (G)createPoint(point);
    } else if (geometry instanceof LinearRing) {
      final LinearRing line = (LinearRing)geometry;
      return (G)createLinearRing(line);
    } else if (geometry instanceof LineString) {
      final LineString line = (LineString)geometry;
      return (G)createLineString(line);
    } else if (geometry instanceof Polygon) {
      final Polygon polygon = (Polygon)geometry;
      return (G)createPolygon(polygon);
    } else if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      return (G)createPoint(point);
    } else if (geometry instanceof MultiPoint) {
      final MultiPoint point = (MultiPoint)geometry;
      return (G)createMultiPoint(point);
    } else if (geometry instanceof MultiLinearRing) {
      final MultiLinearRing line = (MultiLinearRing)geometry;
      return (G)createMultiLinearRing(line);
    } else if (geometry instanceof MultiLineString) {
      final MultiLineString line = (MultiLineString)geometry;
      return (G)createMultiLineString(line);
    } else if (geometry instanceof MultiPolygon) {
      final MultiPolygon polygon = (MultiPolygon)geometry;
      return (G)createMultiPolygon(polygon);
    } else {
      throw new IllegalArgumentException("Geometry class not supported "
        + geometry.getClass());
    }
  }

  @Override
  public GeometryCollection createGeometryCollection() {
    final List<Geometry> geometries = Collections.emptyList();
    return new GeometryCollectionImpl(this, geometries);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <G extends GeometryCollection> G createGeometryCollection(
    final Collection<? extends Geometry> geometries) {
    if (geometries.isEmpty()) {
      return (G)createGeometryCollection();
    } else {
      final Class<? extends Geometry> geomClass = getGeometryInterface(geometries);
      if (geomClass == null) {
        return createGeometryCollection(geometries);
      } else {
        if (geomClass == Polygon.class) {
          return (G)createMultiPolygon(geometries);
        } else if (geomClass == LinearRing.class) {
          return (G)createMultiLinearRing(geometries);
        } else if (geomClass == LineString.class) {
          return (G)createMultiLineString(geometries);
        } else if (geomClass == Point.class) {
          return (G)createMultiPoint(geometries);
        } else {
          throw new IllegalArgumentException("Unhandled class: " + geomClass);
        }
      }
    }
  }

  @Override
  public LinearRing createLinearRing(final CoordinatesList points) {
    return new LinearRingImpl(this, points);
  }

  @Override
  public LinearRing createLinearRing(final LineString lineString) {
    CoordinatesList points;
    final com.revolsys.gis.model.geometry.GeometryFactoryI factory = lineString.getGeometryFactory();
    if (factory == this) {
      points = lineString;
    } else {
      final CoordinateSystem sourceCoordinateSystem = factory.getCoordinateSystem();
      final CoordinatesOperation operation = ProjectionFactory.getCoordinatesOperation(
        sourceCoordinateSystem, getCoordinateSystem());
      final int size = lineString.size();
      points = new DoubleCoordinatesList(size, getNumAxis());
      if (operation != null) {
        final CoordinatesListCoordinates sourceCoordinates = new CoordinatesListCoordinates(
          lineString);
        final CoordinatesListCoordinates targetCoordinates = new CoordinatesListCoordinates(
          points);
        for (int i = 0; i < size; i++) {
          sourceCoordinates.setIndex(i);
          targetCoordinates.setIndex(i);
          operation.perform(sourceCoordinates, targetCoordinates);
        }
      }
      points.makePrecise(this);
    }
    return createLinearRing(points);
  }

  @Override
  public LinearRing createLinearRing(final Object points) {
    if (points instanceof LineString) {
      return createLinearRing((LineString)points);
    } else if (points instanceof CoordinatesList) {
      return createLinearRing((CoordinatesList)points);
    } else if (points instanceof double[]) {
      final double[] coordinates = (double[])points;
      return createLinearRing(coordinates);
    } else {
      throw new IllegalArgumentException("Class not supported "
        + points.getClass());
    }
  }

  @Override
  public Geometry createLineString() {
    return createLineString(new DoubleCoordinatesList(2));
  }

  @Override
  public LineString createLineString(final CoordinatesList points) {
    return new LineStringImpl(this, points);
  }

  @Override
  public LineString createLineString(final LineString lineString) {
    final GeometryFactoryImpl factory = lineString.getGeometryFactory();
    final CoordinatesList points = createCoordinatesList(factory, lineString);
    return createLineString(points);
  }

  @Override
  public LineString createLineString(final Object points) {
    if (points instanceof LineString) {
      return createLineString((LineString)points);
    } else if (points instanceof CoordinatesList) {
      return createLineString((CoordinatesList)points);
    } else if (points instanceof double[]) {
      final double[] coordinates = (double[])points;
      return createLineString(coordinates);
    } else {
      throw new IllegalArgumentException("Class not supported "
        + points.getClass());
    }
  }

  private MultiLinearRingImpl createMultiLinearRing(
    final Collection<? extends Geometry> geometries) {
    return new MultiLinearRingImpl(this, geometries);
  }

  public MultiLinearRing createMultiLinearRing(final List<LinearRing> rings) {
    return new MultiLinearRingImpl(this, rings);
  }

  public Geometry createMultiLinearRing(final MultiLinearRing line) {
    final List<LinearRing> geometries = line.getGeometries();
    return createMultiLinearRing(geometries);
  }

  private MultiLineStringImpl createMultiLineString(
    final Collection<? extends Geometry> geometries) {
    return new MultiLineStringImpl(this, geometries);
  }

  @Override
  public MultiLineString createMultiLineString(final List<?> lines) {
    final List<LineString> lineStrings = new ArrayList<LineString>();
    for (final Object object : lines) {
      final LineString line = createLineString(object);
      if (line != null) {
        lineStrings.add(line);
      }
    }
    return new MultiLineStringImpl(this, lineStrings);
  }

  public Geometry createMultiLineString(final MultiLineString line) {
    final Collection<LineString> geometries = line.getGeometries();
    return createMultiLineString(geometries);
  }

  private MultiPointImpl createMultiPoint(
    final Collection<? extends Geometry> geometries) {
    return new MultiPointImpl(this, geometries);
  }

  public Geometry createMultiPoint(final MultiPoint point) {
    // TODO List<Point> geometries = point.getGeometries();
    // return createMultiPoint(geometries);
    return null;
  }

  private MultiPolygonImpl createMultiPolygon(
    final Collection<? extends Geometry> geometries) {
    return new MultiPolygonImpl(this, geometries);
  }

  public Geometry createMultiPolygon(final MultiPolygon polygon) {
    // TODO List<Polygon> geometries = polygon.getGeometries();
    // return createMultiPolygon(geometries);
    return null;
  }

  @Override
  public Point createPoint(final Coordinates coordinates) {
    return new PointImpl(this, coordinates);
  }

  @Override
  public Point createPoint(final double... coordinates) {
    return new PointImpl(this, coordinates);
  }

  @Override
  public Point createPoint(final Point point) {
    Coordinates coordinates;
    final com.revolsys.gis.model.geometry.GeometryFactoryI factory = point.getGeometryFactory();
    if (factory == this) {
      coordinates = point;
    } else {
      final CoordinateSystem sourceCoordinateSystem = factory.getCoordinateSystem();
      final CoordinatesOperation operation = ProjectionFactory.getCoordinatesOperation(
        sourceCoordinateSystem, getCoordinateSystem());
      coordinates = new DoubleCoordinates(point);
      if (operation != null) {
        operation.perform(point, coordinates);
      }
      makePrecise(coordinates);
    }
    return createPoint(coordinates);
  }

  @Override
  public Polygon createPolygon(final CoordinatesList... rings) {
    final List<CoordinatesList> ringList = Arrays.asList(rings);
    return createPolygon(ringList);
  }

  @Override
  public Polygon createPolygon(final List<?> rings) {
    final List<LinearRing> ringList = new ArrayList<LinearRing>();
    if (!rings.isEmpty()) {
      for (final Object points : rings) {
        final LinearRing ring = createLinearRing(points);
        ringList.add(ring);
      }
    }
    return new PolygonImpl(this, ringList);
  }

  @Override
  public Polygon createPolygon(final Polygon polygon) {
    final List<LinearRing> newRings = new ArrayList<LinearRing>();
    for (final LinearRing ring : polygon) {
      final LinearRing newRing = createLinearRing(ring);
      newRings.add(newRing);
    }
    return createPolygon(newRings);
  }

  public CoordinatesPrecisionModel getCoordinatesPrecisionModel() {
    return this;
  }

  @Override
  public CoordinateSystem getCoordinateSystem() {
    return coordinateSystem;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <G extends Geometry> G getGeometry(final Geometry geometry) {
    if (geometry == null) {
      return null;
    } else {
      final com.revolsys.gis.model.geometry.GeometryFactoryI geometryFactory = geometry.getGeometryFactory();
      if (geometryFactory == this) {
        return (G)geometry;
      } else {
        return (G)createGeometry(geometry);
      }
    }
  }

  protected Class<? extends Geometry> getGeometryInterface(
    final Collection<? extends Geometry> geometries) {
    Class<? extends Geometry> geomClass = null;
    for (final Geometry geometry : geometries) {
      Class<? extends Geometry> partClass;
      if (geometry instanceof GeometryCollection) {
        return null;
      } else if (geometry instanceof Point) {
        partClass = Point.class;
      } else if (geometry instanceof LinearRing) {
        partClass = LinearRing.class;
      } else if (geometry instanceof LineString) {
        partClass = LineString.class;
      } else if (geometry instanceof Polygon) {
        partClass = Polygon.class;
      } else {
        throw new IllegalArgumentException("Unsupported geometry class "
          + geometry.getClass());
      }
      if (geomClass == null) {
        geomClass = partClass;
      } else if (partClass != geomClass) {
        return null;
      }
    }
    return geomClass;
  }

  public byte getNumAxis() {
    return numAxis;
  }

  @Override
  public int getSrid() {
    return coordinateSystem.getId();
  }

  public boolean hasM() {
    return numAxis > 3;
  }

  public boolean hasZ() {
    return numAxis > 2;
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
      final double scaleXY = getScaleXY();
      string.append(", scaleXy=");
      if (scaleXY <= 0) {
        string.append("floating");
      } else {
        string.append(scaleXY);
      }
      if (hasZ()) {
        final double scaleZ = getScaleZ();
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
