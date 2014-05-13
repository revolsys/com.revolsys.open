/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.jts.geom;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.StringUtils;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.gis.cs.ProjectedCoordinateSystem;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.cs.esri.EsriCoordinateSystems;
import com.revolsys.gis.cs.projection.CoordinatesOperation;
import com.revolsys.gis.cs.projection.ProjectionFactory;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.io.map.InvokeMethodMapObjectFactory;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.io.wkt.WktParser;
import com.revolsys.jts.geom.impl.GeometryCollectionImpl;
import com.revolsys.jts.geom.impl.LineStringImpl;
import com.revolsys.jts.geom.impl.LinearRingImpl;
import com.revolsys.jts.geom.impl.MultiLineStringImpl;
import com.revolsys.jts.geom.impl.MultiPointImpl;
import com.revolsys.jts.geom.impl.MultiPolygonImpl;
import com.revolsys.jts.geom.impl.PointImpl;
import com.revolsys.jts.geom.impl.PolygonImpl;
import com.revolsys.jts.operation.linemerge.LineMerger;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.MathUtil;

/**
 * Supplies a set of utility methods for building Geometry objects from lists
 * of Coordinates.
 * <p>
 * Note that the factory constructor methods do <b>not</b> change the input coordinates in any way.
 * In particular, they are not rounded to the supplied <tt>PrecisionModel</tt>.
 * It is assumed that input Point meet the given precision.
 *
 *
 * @version 1.7
 */
public class GeometryFactory implements Serializable, MapSerializer {
  public static final MapObjectFactory FACTORY = new InvokeMethodMapObjectFactory(
    "geometryFactory", "Geometry Factory", GeometryFactory.class, "create");

  /** The cached geometry factories. */
  private static Map<String, GeometryFactory> factories = new HashMap<String, GeometryFactory>();

  private static final long serialVersionUID = 4328651897279304108L;

  public static void clear() {
    factories.clear();
  }

  public static GeometryFactory create(final Map<String, Object> properties) {
    final int srid = CollectionUtil.getInteger(properties, "srid", 0);
    final int axisCount = CollectionUtil.getInteger(properties, "axisCount", 2);
    final double scaleXY = CollectionUtil.getDouble(properties, "scaleXy", 0.0);
    final double scaleZ = CollectionUtil.getDouble(properties, "scaleZ", 0.0);
    return GeometryFactory.getFactory(srid, axisCount, scaleXY, scaleZ);
  }

  /**
   * <p>
   * Get a GeometryFactory with no coordinate system, 3D axis (x, y &amp; z) and
   * a floating precision model.
   * </p>
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
    final CoordinateSystem coordinateSystem, final int axisCount,
    final double... scales) {
    return new GeometryFactory(coordinateSystem, axisCount, scales);
  }

  /**
   * <p>
   * Get a GeometryFactory with no coordinate system, 3D axis (x, y &amp; z) and
   * a fixed x, y & floating z precision models.
   * </p>
   * 
   * @param scaleXY The scale factor used to round the x, y coordinates. The
   *          precision is 1 / scaleXy. A scale factor of 1000 will give a
   *          precision of 1 / 1000 = 1mm for projected coordinate systems using
   *          metres.
   * @return The geometry factory.
   */
  public static GeometryFactory getFactory(final double scaleXY) {
    return getFactory(0, 3, scaleXY, 0);
  }

  public static GeometryFactory getFactory(final Geometry geometry) {
    if (geometry == null) {
      return getFactory(0, 3, 0, 0);
    } else {
      return geometry.getGeometryFactory();
    }
  }

  /**
   * <p>
   * Get a GeometryFactory with the coordinate system, 3D axis (x, y &amp; z)
   * and a floating precision models.
   * </p>
   * 
   * @param srid The <a href="http://spatialreference.org/ref/epsg/">EPSG
   *          coordinate system id</a>.
   * @return The geometry factory.
   */
  public static GeometryFactory getFactory(final int srid) {
    return getFactory(srid, 3, 0, 0);
  }

  /**
   * <p>
   * Get a GeometryFactory with the coordinate system, 2D axis (x &amp; y) and a
   * fixed x, y precision model.
   * </p>
   * 
   * @param srid The <a href="http://spatialreference.org/ref/epsg/">EPSG
   *          coordinate system id</a>.
   * @param scaleXY The scale factor used to round the x, y coordinates. The
   *          precision is 1 / scaleXy. A scale factor of 1000 will give a
   *          precision of 1 / 1000 = 1mm for projected coordinate systems using
   *          metres.
   * @return The geometry factory.
   */
  public static GeometryFactory getFactory(final int srid,
    final double... scales) {
    return getFactory(srid, scales.length + 1, scales);
  }

  /**
   * <p>
   * Get a GeometryFactory with the coordinate system, number of axis and a
   * floating precision model.
   * </p>
   * 
   * @param srid The <a href="http://spatialreference.org/ref/epsg/">EPSG
   *          coordinate system id</a>.
   * @param axisCount The number of coordinate axis. 2 for 2D x &amp; y
   *          coordinates. 3 for 3D x, y &amp; z coordinates.
   * @return The geometry factory.
   */
  public static GeometryFactory getFactory(final int srid, final int axisCount) {
    return getFactory(srid, axisCount, 0, 0);
  }

  /**
   * <p>
   * Get a GeometryFactory with the coordinate system, number of axis and a
   * fixed x, y &amp; fixed z precision models.
   * </p>
   * 
   * @param srid The <a href="http://spatialreference.org/ref/epsg/">EPSG
   *          coordinate system id</a>.
   * @param axisCount The number of coordinate axis. 2 for 2D x &amp; y
   *          coordinates. 3 for 3D x, y &amp; z coordinates.
   * @param scaleXY The scale factor used to round the x, y coordinates. The
   *          precision is 1 / scaleXy. A scale factor of 1000 will give a
   *          precision of 1 / 1000 = 1mm for projected coordinate systems using
   *          metres.
   * @param scaleZ The scale factor used to round the z coordinates. The
   *          precision is 1 / scaleZ. A scale factor of 1000 will give a
   *          precision of 1 / 1000 = 1mm for projected coordinate systems using
   *          metres.
   * @return The geometry factory.
   */
  public static GeometryFactory getFactory(final int srid, final int axisCount,
    double... scales) {
    synchronized (factories) {
      scales = getScales(axisCount, scales);
      final String key = srid + "-" + axisCount + "-"
        + CollectionUtil.toString("-", CollectionUtil.toList(scales));
      GeometryFactory factory = factories.get(key);
      if (factory == null) {
        factory = new GeometryFactory(srid, axisCount, scales);
        factories.put(key, factory);
      }
      return factory;
    }
  }

  /**
   * <p>
   * Get a GeometryFactory with the coordinate system, 3D axis (x, y &amp; z)
   * and a floating precision models.
   * </p>
   * 
   * @param srid The <a href="http://spatialreference.org/ref/epsg/">EPSG
   *          coordinate system id</a>.
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

  private static Set<DataType> getGeometryDataTypes(
    final Collection<? extends Geometry> geometries) {
    final Set<DataType> dataTypes = new LinkedHashSet<DataType>();
    for (final Geometry geometry : geometries) {
      final DataType dataType = geometry.getDataType();
      dataTypes.add(dataType);
    }
    return dataTypes;
  }

  private static int getId(final CoordinateSystem coordinateSystem) {
    if (coordinateSystem == null) {
      return 0;
    } else {
      return coordinateSystem.getId();
    }
  }

  public static double[] getScales(final int axisCount, final double... scales) {
    final double[] newScales = new double[Math.max(2, axisCount)];
    for (int i = 0; i < newScales.length; i++) {
      int scaleIndex = i;
      if (i > 0) {
        scaleIndex--;
      }
      double scale = 0;
      if (scaleIndex < scales.length) {
        scale = scales[scaleIndex];
      }
      if (scale > 0) {
        newScales[i] = scale;
      }
    }
    return newScales;
  }

  public static GeometryFactory wgs84() {
    return getFactory(4326);
  }

  public static GeometryFactory worldMercator() {
    return getFactory(3857);
  }

  private final CoordinateSystem coordinateSystem;

  private int axisCount = 2;

  private final int srid;

  private final WktParser parser = new WktParser(this);

  private double[] scales;

  protected GeometryFactory(final CoordinateSystem coordinateSystem,
    final int axisCount, final double... scales) {
    this.srid = coordinateSystem.getId();
    this.coordinateSystem = coordinateSystem;
    init(axisCount, scales);
  }

  protected GeometryFactory(final int srid, final int axisCount,
    final double... scales) {
    this.srid = srid;
    this.coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(srid);
    init(axisCount, scales);
  }

  public void addGeometries(final List<Geometry> geometryList,
    final Geometry geometry) {
    if (geometry != null && !geometry.isEmpty()) {
      for (final Geometry part : geometry.geometries()) {
        if (part != null && !part.isEmpty()) {
          geometryList.add(part.copy(this));
        }
      }
    }
  }

  /**
   *  Build an appropriate <code>Geometry</code>, <code>MultiGeometry</code>, or
   *  <code>GeometryCollection</code> to contain the <code>Geometry</code>s in
   *  it.
   * For example:<br>
   *
   *  <ul>
   *    <li> If <code>geomList</code> contains a single <code>Polygon</code>,
   *    the <code>Polygon</code> is returned.
   *    <li> If <code>geomList</code> contains several <code>Polygon</code>s, a
   *    <code>MultiPolygon</code> is returned.
   *    <li> If <code>geomList</code> contains some <code>Polygon</code>s and
   *    some <code>LineString</code>s, a <code>GeometryCollection</code> is
   *    returned.
   *    <li> If <code>geomList</code> is empty, an empty <code>GeometryCollection</code>
   *    is returned
   *  </ul>
   *
   * Note that this method does not "flatten" Geometries in the input, and hence if
   * any MultiGeometries are contained in the input a GeometryCollection containing
   * them will be returned.
   *
   *@param  geometries  the <code>Geometry</code>s to combine
   *@return           a <code>Geometry</code> of the "smallest", "most
   *      type-specific" class that can contain the elements of <code>geomList</code>
   *      .
   */
  public Geometry buildGeometry(final Collection<? extends Geometry> geometries) {

    /**
     * Determine some facts about the geometries in the list
     */
    DataType collectionDataType = null;
    boolean isHeterogeneous = false;
    boolean hasGeometryCollection = false;
    for (final Geometry geometry : geometries) {
      DataType geometryDataType = geometry.getDataType();
      if (geometry instanceof LinearRing) {
        geometryDataType = DataTypes.LINE_STRING;
      }
      if (collectionDataType == null) {
        collectionDataType = geometryDataType;
      } else if (geometryDataType != collectionDataType) {

        isHeterogeneous = true;
      }
      if (geometry instanceof GeometryCollection) {
        hasGeometryCollection = true;
      }
    }

    /**
     * Now construct an appropriate geometry to return
     */
    if (collectionDataType == null) {
      return geometryCollection();
    } else if (isHeterogeneous || hasGeometryCollection) {
      return geometryCollection(geometries);
    } else if (geometries.size() == 1) {
      return geometries.iterator().next();
    } else if (DataTypes.POINT.equals(collectionDataType)) {
      return multiPoint(geometries);
    } else if (DataTypes.LINE_STRING.equals(collectionDataType)) {
      return multiLineString(geometries);
    } else if (DataTypes.POLYGON.equals(collectionDataType)) {
      return multiPolygon(geometries);
    } else {
      throw new IllegalArgumentException("Unknown geometry type "
        + collectionDataType);
    }
  }

  public GeometryFactory convertAxisCount(final int axisCount) {
    if (axisCount == getAxisCount()) {
      return this;
    } else {
      final int srid = getSrid();
      final double[] scales = new double[this.scales.length - 1];
      System.arraycopy(this.scales, 1, scales, 0, scales.length);
      return GeometryFactory.getFactory(srid, axisCount, scales);
    }
  }

  public GeometryFactory convertScales(final double... scales) {
    final int srid = getSrid();
    final int axisCount = getAxisCount();
    return GeometryFactory.getFactory(srid, axisCount, scales);
  }

  public double[] copyPrecise(final double[] values) {
    final double[] valuesPrecise = new double[values.length];
    makePrecise(values, valuesPrecise);
    return valuesPrecise;
  }

  public Point createCoordinates(final double... coordinates) {
    for (int i = 0; i < coordinates.length; i++) {
      coordinates[i] = makePrecise(i, coordinates[i]);
    }
    final Point newPoint = new DoubleCoordinates(this.axisCount, coordinates);
    return newPoint;
  }

  public Point createCoordinates(final Point point) {
    return getPreciseCoordinates(point);
  }

  public CoordinatesList createCoordinatesList(final Collection<?> points) {
    if (points == null || points.isEmpty()) {
      return null;
    } else {
      final int numPoints = points.size();
      final int axisCount = getAxisCount();
      final double[] coordinates = new double[numPoints * axisCount];
      int i = 0;
      for (final Object object : points) {
        Point point;
        if (object == null) {
          point = null;
        } else if (object instanceof Point) {
          point = (Point)object;
        } else if (object instanceof Point) {
          final Point projectedPoint = ((Point)object).convert(this);
          point = projectedPoint;
        } else if (object instanceof double[]) {
          point = new DoubleCoordinates((double[])object);
        } else if (object instanceof CoordinatesList) {
          final CoordinatesList pointList = (CoordinatesList)object;
          point = pointList.get(0);
        } else {
          throw new IllegalArgumentException("Unexepected data type: " + object);
        }

        if (point != null && point.getAxisCount() > 1) {
          CoordinatesListUtil.setCoordinates(this, coordinates, axisCount, i,
            point);
          i++;
        }
      }
      return new DoubleCoordinatesList(axisCount, i, coordinates);
    }
  }

  public Geometry geometry() {
    return point();
  }

  /**
   * <p>
   * Create a new geometry of the requested target geometry class.
   * <p>
   * 
   * @param targetClass
   * @param geometry
   * @return
   */
  @SuppressWarnings({
    "unchecked"
  })
  public <V extends Geometry> V geometry(final Class<?> targetClass,
    Geometry geometry) {
    if (geometry != null && !geometry.isEmpty()) {
      geometry = geometry.copy(this);
      if (geometry instanceof GeometryCollection) {
        if (geometry.getGeometryCount() == 1) {
          geometry = geometry.getGeometry(0);
        } else {
          geometry = geometry.union();
          // Union doesn't use this geometry factory
          geometry = geometry.copy(this);
        }
      }
      final Class<?> geometryClass = geometry.getClass();
      if (targetClass.isAssignableFrom(geometryClass)) {
        // TODO if geometry collection then clean up
        return (V)geometry;
      } else if (Point.class.isAssignableFrom(targetClass)) {
        if (geometry instanceof MultiPoint) {
          if (geometry.getGeometryCount() == 1) {
            return (V)geometry.getGeometry(0);
          }
        }
      } else if (LineString.class.isAssignableFrom(targetClass)) {
        if (geometry instanceof MultiLineString) {
          if (geometry.getGeometryCount() == 1) {
            return (V)geometry.getGeometry(0);
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
          if (geometry.getGeometryCount() == 1) {
            return (V)geometry.getGeometry(0);
          }
        }
      } else if (MultiPoint.class.isAssignableFrom(targetClass)) {
        if (geometry instanceof Point) {
          return (V)multiPoint(geometry);
        }
      } else if (MultiLineString.class.isAssignableFrom(targetClass)) {
        if (geometry instanceof LineString) {
          return (V)multiLineString(geometry);
        }
      } else if (MultiPolygon.class.isAssignableFrom(targetClass)) {
        if (geometry instanceof Polygon) {
          return (V)multiPolygon(geometry);
        }
      }
    }
    return null;
  }

  /**
   * Create a new geometry my flattening the input geometries, ignoring and null or empty
   * geometries. If there are no geometries an empty {@link GeometryCollection} will be returned.
   * If there is one geometry that single geometry will be returned. Otherwise the result
   * will be a subclass of {@link GeometryCollection}.
   * 
   * @author Paul Austin <paul.austin@revolsys.com>
   * @param geometries
   * @return
   */
  @SuppressWarnings("unchecked")
  public <V extends Geometry> V geometry(
    final Collection<? extends Geometry> geometries) {
    final Collection<? extends Geometry> geometryList = getGeometries(geometries);
    if (geometryList == null || geometries.size() == 0) {
      return (V)geometryCollection();
    } else if (geometries.size() == 1) {
      return (V)CollectionUtil.get(geometries, 0);
    } else {
      final Set<DataType> dataTypes = getGeometryDataTypes(geometryList);
      if (dataTypes.size() == 1) {
        final DataType dataType = CollectionUtil.get(dataTypes, 0);
        if (dataType.equals(DataTypes.POINT)) {
          return (V)multiPoint(geometryList);
        } else if (dataType.equals(DataTypes.LINE_STRING)) {
          return (V)multiLineString(geometryList);
        } else if (dataType.equals(DataTypes.POLYGON)) {
          return (V)multiPolygon(geometryList);
        }
      }
      return (V)geometryCollection(geometries);
    }
  }

  @SuppressWarnings("unchecked")
  public <V extends Geometry> V geometry(final Geometry... geometries) {
    return (V)geometry(Arrays.asList(geometries));
  }

  /**
   * Creates a deep copy of the input {@link Geometry}.
   * <p>
   * This is a convenient way to change the <tt>CoordinatesList</tt>
   * used to represent a geometry, or to change the 
   * factory used for a geometry.
   * <p>
   * {@link Geometry#clone()} can also be used to make a deep copy,
   * but it does not allow changing the CoordinatesList type.
   * 
   * @return a deep copy of the input geometry, using the CoordinatesList type of this factory
   * 
   * @see Geometry#clone() 
   */
  public Geometry geometry(final Geometry geometry) {
    if (geometry == null) {
      return null;
    } else {
      final int srid = getSrid();
      final int geometrySrid = geometry.getSrid();
      if (srid == 0 && geometrySrid != 0) {
        final GeometryFactory geometryFactory = GeometryFactory.getFactory(
          geometrySrid, axisCount, getScaleXY(), getScaleZ());
        return geometryFactory.geometry(geometry);
      } else if (srid != 0 && geometrySrid != 0 && geometrySrid != srid) {
        if (geometry instanceof MultiPoint) {
          final List<Geometry> geometries = new ArrayList<Geometry>();
          addGeometries(geometries, geometry);
          return multiPoint(geometries);
        } else if (geometry instanceof MultiLineString) {
          final List<Geometry> geometries = new ArrayList<Geometry>();
          addGeometries(geometries, geometry);
          return multiLineString(geometries);
        } else if (geometry instanceof MultiPolygon) {
          final List<Geometry> geometries = new ArrayList<Geometry>();
          addGeometries(geometries, geometry);
          return multiPolygon(geometries);
        } else if (geometry instanceof GeometryCollection) {
          final List<Geometry> geometries = new ArrayList<Geometry>();
          addGeometries(geometries, geometry);
          return geometryCollection(geometries);
        } else {
          return geometry.copy(this);
        }
      } else if (geometry instanceof MultiPoint) {
        final List<Geometry> geometries = new ArrayList<Geometry>();
        addGeometries(geometries, geometry);
        return multiPoint(geometries);
      } else if (geometry instanceof MultiLineString) {
        final List<Geometry> geometries = new ArrayList<Geometry>();
        addGeometries(geometries, geometry);
        return multiLineString(geometries);
      } else if (geometry instanceof MultiPolygon) {
        final List<Geometry> geometries = new ArrayList<Geometry>();
        addGeometries(geometries, geometry);
        return multiPolygon(geometries);
      } else if (geometry instanceof GeometryCollection) {
        final List<Geometry> geometries = new ArrayList<Geometry>();
        addGeometries(geometries, geometry);
        return geometryCollection(geometries);
      } else if (geometry instanceof Point) {
        final Point point = (Point)geometry;
        return point.copy(this);
      } else if (geometry instanceof LinearRing) {
        final LinearRing linearRing = (LinearRing)geometry;
        return linearRing.copy(this);
      } else if (geometry instanceof LineString) {
        final LineString lineString = (LineString)geometry;
        return lineString.copy(this);
      } else if (geometry instanceof Polygon) {
        final Polygon polygon = (Polygon)geometry;
        return polygon(polygon);
      } else {
        return null;
      }
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends Geometry> T geometry(final String wkt) {
    if (StringUtils.hasText(wkt)) {
      return (T)parser.parseGeometry(wkt);
    } else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends Geometry> T geometry(final String wkt,
    final boolean useAxisCountFromGeometryFactory) {
    return (T)parser.parseGeometry(wkt, useAxisCountFromGeometryFactory);
  }

  public GeometryCollection geometryCollection() {
    return new GeometryCollectionImpl(this);
  }

  @SuppressWarnings("unchecked")
  public <V extends GeometryCollection> V geometryCollection(
    final Collection<? extends Geometry> geometries) {
    final Set<DataType> dataTypes = new HashSet<>();
    final List<Geometry> geometryList = new ArrayList<>();
    if (geometries != null) {
      for (final Geometry geometry : geometries) {
        if (geometry != null) {
          dataTypes.add(geometry.getDataType());
          final Geometry copy = geometry.copy(this);
          geometryList.add(copy);
        }
      }
    }
    if (geometryList == null || geometryList.size() == 0) {
      return (V)geometryCollection();
    } else if (dataTypes.equals(Collections.singleton(DataTypes.POINT))) {
      return (V)multiPoint(geometryList);
    } else if (dataTypes.equals(Collections.singleton(DataTypes.LINE_STRING))) {
      return (V)multiLineString(geometryList);
    } else if (dataTypes.equals(Collections.singleton(DataTypes.POLYGON))) {
      return (V)multiPolygon(geometryList);
    } else {
      final Geometry[] geometryArray = new Geometry[geometries.size()];
      geometries.toArray(geometryArray);
      return (V)new GeometryCollectionImpl(this, geometryArray);
    }
  }

  @SuppressWarnings("unchecked")
  public <V extends GeometryCollection> V geometryCollection(
    final Geometry... geometries) {
    return (V)geometryCollection(Arrays.asList(geometries));
  }

  public int getAxisCount() {
    return axisCount;
  }

  public Point getCoordinates(final Point point) {
    final Point convertedPoint = project(point);
    return convertedPoint;
  }

  /**
   * <p>Get the {@link CoordinatesOperation} to convert between this factory's and the other factory's
   * {@link CoordinateSystem}.</p>
   * 
   * @author Paul Austin <paul.austin@revolsys.com>
   * @param geometryFactory The geometry factory to convert to.
   * @return The coordinates operation or null if no conversion is available.
   */
  public CoordinatesOperation getCoordinatesOperation(
    final GeometryFactory geometryFactory) {
    final CoordinateSystem coordinateSystem = getCoordinateSystem();
    final CoordinateSystem otherCoordinateSystem = geometryFactory.getCoordinateSystem();
    return ProjectionFactory.getCoordinatesOperation(coordinateSystem,
      otherCoordinateSystem);
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
      return getFactory(srid, getAxisCount(), 0, 0);
    } else {
      return getFactory(4326, getAxisCount(), 0, 0);
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
      return linearRing(points);
    } else if (ring instanceof LineString) {
      final LineString line = (LineString)ring;
      final CoordinatesList points = CoordinatesListUtil.get(line);
      return linearRing(points);
    } else if (ring instanceof double[]) {
      final double[] coordinates = (double[])ring;
      final DoubleCoordinatesList points = new DoubleCoordinatesList(
        getAxisCount(), coordinates);
      return linearRing(points);
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
        lineString = lineString(coordinates);
      } else if (value instanceof double[]) {
        final double[] points = (double[])value;
        lineString = lineString(getAxisCount(), points);
      } else {
        lineString = null;
      }
      if (lineString != null) {
        lineStrings.add(lineString);
      }
    }
    return lineStrings.toArray(new LineString[lineStrings.size()]);
  }

  /**
   * Returns the maximum number of significant digits provided by this
   * precision model.
   * Intended for use by routines which need to print out 
   * decimal representations of precise values .
   * <p>
   * This method would be more correctly called
   * <tt>getMinimumDecimalPlaces</tt>, 
   * since it actually computes the number of decimal places
   * that is required to correctly display the full
   * precision of an ordinate value.
   * <p>
   * Since it is difficult to compute the required number of
   * decimal places for scale factors which are not powers of 10,
   * the algorithm uses a very rough approximation in this case.
   * This has the side effect that for scale factors which are
   * powers of 10 the value returned is 1 greater than the true value.
   * 
   *
   * @return the maximum number of decimal places provided by this precision model
   */
  public int getMaximumSignificantDigits() {
    int maxSigDigits = 16;
    if (isFloating()) {
      maxSigDigits = 16;
    } else {
      maxSigDigits = 1 + (int)Math.ceil(Math.log(getScale(0)) / Math.log(10));
    }
    return maxSigDigits;
  }

  public Point[] getPointArray(final Collection<?> pointsList) {
    final List<Point> points = new ArrayList<Point>();
    for (final Object object : pointsList) {
      final Point point = point(object);
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
        polygon = polygon(coordinateList);
      } else if (value instanceof CoordinatesList) {
        final CoordinatesList coordinateList = (CoordinatesList)value;
        polygon = polygon(coordinateList);
      } else {
        polygon = null;
      }
      if (polygon != null) {
        polygons.add(polygon);
      }
    }
    return polygons.toArray(new Polygon[polygons.size()]);
  }

  public Point[] getPrecise(final Point... points) {
    final Point[] precisesPoints = new Point[points.length];
    for (int i = 0; i < points.length; i++) {
      final Point point = points[i];
      precisesPoints[i] = getPreciseCoordinates(point);
    }
    return precisesPoints;
  }

  public Point getPreciseCoordinates(final Point point) {
    final double[] coordinates = point.getCoordinates();
    makePrecise(coordinates.length, coordinates);
    return new DoubleCoordinates(coordinates);
  }

  public double getResolution(final int axisIndex) {
    final double scale = getScale(axisIndex);
    if (scale <= 0) {
      return 0;
    } else {
      return 1 / scale;
    }
  }

  public double getResolutionXy() {
    final double scaleXy = getScaleXY();
    if (scaleXy <= 0) {
      return 0;
    } else {
      return 1 / scaleXy;
    }
  }

  public double getResolutionZ() {
    final double scaleZ = getScaleZ();
    if (scaleZ <= 0) {
      return 0;
    } else {
      return 1 / scaleZ;
    }
  }

  public double getScale(final int axisIndex) {
    if (axisIndex < 0 || axisIndex >= scales.length) {
      return 0;
    } else {
      return scales[0];
    }
  }

  public double getScaleXY() {
    return getScale(0);
  }

  public double getScaleZ() {
    return getScale(2);
  }

  /**
   * Gets the srid value defined for this factory.
   * 
   * @return the factory srid value
   */
  public int getSrid() {
    return srid;
  }

  public boolean hasM() {
    return axisCount > 3;
  }

  public boolean hasZ() {
    return axisCount > 2;
  }

  protected void init(final int axisCount, final double... scales) {
    this.axisCount = Math.max(axisCount, 2);
    this.scales = getScales(axisCount, scales);
  }

  public boolean isFloating() {
    return getScale(0) == 0;
  }

  public boolean isGeographics() {
    return coordinateSystem instanceof GeographicCoordinateSystem;
  }

  public boolean isProjected() {
    return coordinateSystem instanceof ProjectedCoordinateSystem;
  }

  public LinearRing linearRing() {
    return new LinearRingImpl(this);
  }

  public LinearRing linearRing(final Collection<?> points) {
    if (points == null || points.isEmpty()) {
      return linearRing();
    } else {
      final CoordinatesList coordinatesList = createCoordinatesList(points);
      return linearRing(coordinatesList);
    }
  }

  /**
   * Creates a {@link LinearRing} using the given {@link CoordinatesList}. 
   * A null or empty array creates an empty LinearRing. 
   * The points must form a closed and simple linestring. 
   * 
   * @param coordinates a CoordinatesList (possibly empty), or null
   * @return the created LinearRing
   * @throws IllegalArgumentException if the ring is not closed, or has too few points
   */
  public LinearRing linearRing(final CoordinatesList points) {
    return new LinearRingImpl(this, points);
  }

  public LinearRing linearRing(final int axisCount, final double... coordinates) {
    return new LinearRingImpl(this, axisCount, coordinates);
  }

  public LinearRing linearRing(final LineString lineString) {
    return linearRing(lineString.getCoordinatesList());
  }

  /**
   * Creates a {@link LinearRing} using the given {@link Coordinates}s.
   * A null or empty array creates an empty LinearRing. 
   * The points must form a closed and simple linestring. 
   * @param coordinates an array without null elements, or an empty array, or null
   * @return the created LinearRing
   * @throws IllegalArgumentException if the ring is not closed, or has too few points
   */
  public LinearRing linearRing(final Point... coordinates) {
    if (coordinates == null) {
      return linearRing();
    } else {
      return linearRing(new DoubleCoordinatesList(coordinates));
    }
  }

  public LineString lineString() {
    return new LineStringImpl(this);
  }

  public LineString lineString(final Collection<?> points) {
    if (points.isEmpty()) {
      return lineString();
    } else {
      final CoordinatesList coordinatesList = createCoordinatesList(points);
      return lineString(coordinatesList);
    }
  }

  /**
   * Creates a LineString using the given CoordinatesList.
   * A null or empty CoordinatesList creates an empty LineString. 
   * 
   * @param coordinates a CoordinatesList (possibly empty), or null
   */
  public LineString lineString(final CoordinatesList points) {
    return new LineStringImpl(this, points);
  }

  public LineString lineString(final int axisCount, final double... coordinates) {
    return new LineStringImpl(this, axisCount, coordinates);
  }

  public LineString lineString(final int axisCount, final int vertexCount,
    final double... coordinates) {
    return new LineStringImpl(this, axisCount, vertexCount, coordinates);
  }

  public LineString lineString(final LineString lineString) {
    if (lineString == null || lineString.isEmpty()) {
      return lineString();
    } else {
      return new LineStringImpl(this, lineString.getCoordinatesList());
    }
  }

  public LineString lineString(final Point... points) {
    if (points == null) {
      return lineString();
    } else {
      final List<Point> pointList = Arrays.asList(points);
      return lineString(pointList);
    }
  }

  public void makePrecise(final double... values) {
    makePrecise(values, values);
  }

  public void makePrecise(final double[] values, final double[] valuesPrecise) {
    for (int i = 0; i < valuesPrecise.length; i++) {
      final int axisIndex = i % axisCount;
      valuesPrecise[i] = makePrecise(axisIndex, values[i]);
    }
  }

  public double makePrecise(final int axisIndex, final double value) {
    final double scale = getScale(axisIndex);
    return MathUtil.makePrecise(scale, value);
  }

  public void makePrecise(final int axisCount, final double... coordinates) {
    for (int i = 0; i < coordinates.length; i++) {
      final double value = coordinates[i];
      final int axisIndex = i % axisCount;
      final double scale = getScale(axisIndex);
      coordinates[i] = MathUtil.makePrecise(scale, value);
    }
  }

  public double makeXyPrecise(final double value) {
    return makePrecise(0, value);
  }

  public double makeZPrecise(final double value) {
    return makePrecise(2, value);
  }

  public MultiLineString multiLineString(final Collection<?> lines) {
    final LineString[] lineArray = getLineStringArray(lines);
    return multiLineString(lineArray);
  }

  /**
   * Creates a MultiLineString using the given LineStrings; a null or empty
   * array will create an empty MultiLineString.
   * 
   * @param lineStrings LineStrings, each of which may be empty but not null
   * @return the created MultiLineString
   */
  public MultiLineString multiLineString(final LineString[] lineStrings) {
    return new MultiLineStringImpl(lineStrings, this);
  }

  public MultiLineString multiLineString(final Object... lines) {
    return multiLineString(Arrays.asList(lines));
  }

  public MultiPoint multiPoint() {
    return new MultiPointImpl(this);
  }

  public MultiPoint multiPoint(final Collection<?> points) {
    final Point[] pointArray = getPointArray(points);
    return multiPoint(pointArray);
  }

  /**
   * Creates a {@link MultiPoint} using the 
   * points in the given {@link CoordinatesList}.
   * A <code>null</code> or empty CoordinatesList creates an empty MultiPoint.
   *
   * @param coordinates a CoordinatesList (possibly empty), or <code>null</code>
   * @return a MultiPoint geometry
   */
  public MultiPoint multiPoint(final CoordinatesList coordinatesList) {
    if (coordinatesList == null) {
      return multiPoint();
    } else {
      final Point[] points = new Point[coordinatesList.size()];
      for (int i = 0; i < points.length; i++) {
        final Point coordinates = coordinatesList.get(i);
        final Point point = point(coordinates);
        points[i] = point;
      }
      return multiPoint(points);
    }
  }

  public MultiPoint multiPoint(final Object... points) {
    return multiPoint(Arrays.asList(points));
  }

  /**
   * Creates a {@link MultiPoint} using the given {@link Point}s.
   * A null or empty array will create an empty MultiPoint.
   *
   * @param coordinates an array (without null elements), or an empty array, or <code>null</code>
   * @return a MultiPoint object
   */
  public MultiPoint multiPoint(final Point... points) {
    if (points == null || points.length == 0) {
      return multiPoint();
    } else {
      return new MultiPointImpl(this, points);
    }
  }

  public MultiPolygon multiPolygon(final Collection<?> polygons) {
    final Polygon[] polygonArray = getPolygonArray(polygons);
    return multiPolygon(polygonArray);
  }

  public MultiPolygon multiPolygon(final Object... polygons) {
    return multiPolygon(Arrays.asList(polygons));
  }

  /**
   * Creates a MultiPolygon using the given Polygons; a null or empty array
   * will create an empty Polygon. The polygons must conform to the
   * assertions specified in the <A
   * HREF="http://www.opengis.org/techno/specs.htm">OpenGIS Simple Features
   * Specification for SQL</A>.
   *
   * @param polygons
   *            Polygons, each of which may be empty but not null
   * @return the created MultiPolygon
   */
  public MultiPolygon multiPolygon(final Polygon[] polygons) {
    return new MultiPolygonImpl(polygons, this);
  }

  /**
   * <p>Create an empty {@link Point}.</p>
   *
   * @return The point.
   */
  public Point point() {
    return new PointImpl(this);
  }

  /**
   * Creates a Point using the given CoordinatesList; a null or empty
   * CoordinatesList will create an empty Point.
   * 
   * @param points a CoordinatesList (possibly empty), or null
   * @return the created Point
   */
  public Point point(final CoordinatesList points) {
    if (points == null) {
      return point();
    } else {
      final int size = points.size();
      if (size == 0) {
        return point();
      } else if (size == 1) {
        final int axisCount = Math.min(points.getAxisCount(), getAxisCount());
        final double[] coordinates = new double[axisCount];
        for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
          final double coordinate = points.getValue(0, axisIndex);
          coordinates[axisIndex] = coordinate;
        }
        return point(coordinates);
      } else {
        throw new IllegalArgumentException("Point can only have 1 vertex not "
          + size);
      }
    }
  }

  /**
   * <p>Create a new {@link Point} from the specified point coordinates.
   * If the point is null or has length < 2 an empty point will be returned.
   * The result point will have the same  {@link #getAxisCount()} from this factory.
   * Additional coordinates in the point will be ignored. If the point length is &lt;
   * {@link #getAxisCount()} then {@link Double#NaN} will be used for that axis.</p>
   * 
   * @param point The coordinates to create the point from.
   * @return The point.
   */
  public Point point(final double... coordinates) {
    if (coordinates == null || coordinates.length < 2) {
      return point();
    } else {
      return new PointImpl(this, coordinates);
    }
  }

  /**
   * <p>Create a new {@link Point} from the object using the following rules.<p>
   * <ul>
   *   <li><code>null</code> using {@link #point()}</li>
   *   <li>Instances of {@link Point} using {@link Point#copy(GeometryFactory)}</li>
   *   <li>Instances of {@link Coordinates} using {@link #point(Point)}</li>
   *   <li>Instances of {@link CoordinatesList} using {@link #point(CoordinatesList)}</li>
   *   <li>Instances of {@link double[]} using {@link #point(double[])}</li>
   *   <li>Instances of any other class throws {@link IllegalArgumentException}.<li>
   * </ul>
   * 
   * @param point The coordinates to create the point from.
   * @return The point.
   * @throws IllegalArgumentException If the object is not an instance of a supported class.
   */
  public Point point(final Object object) {
    if (object == null) {
      return point();
    } else if (object instanceof Point) {
      final Point point = (Point)object;
      return point.copy(this);
    } else if (object instanceof double[]) {
      return point((double[])object);
    } else if (object instanceof Point) {
      return point((Point)object);
    } else if (object instanceof CoordinatesList) {
      return point((CoordinatesList)object);
    } else {
      throw new IllegalArgumentException("Cannot create a point from "
        + object.getClass());
    }
  }

  /**
   * <p>Create a new {@link Point} from the specified point ({@link Coordinates}).
   * If the point is null or has {@link Coordinates#getAxisCount()} &lt; 2 an empty
   * point will be returned. The result point will have the same  {@link #getAxisCount()} from this
   * factory. Additional axis in the point will be ignored. If the point has a smaller
   * {@link Point#getAxisCount()} then {@link Double#NaN} will be used for that axis.</p>
   * 
   * @param point The coordinates to create the point from.
   * @return The point.
   */
  public Point point(final Point point) {
    if (point == null) {
      return point();
    } else {
      return point(point.getCoordinates());
    }
  }

  public PolygonImpl polygon() {
    return new PolygonImpl(this);
  }

  public Polygon polygon(final CoordinatesList... rings) {
    final List<CoordinatesList> ringList = Arrays.asList(rings);
    return polygon(ringList);
  }

  /**
   * Constructs a <code>Polygon</code> with the given exterior boundary.
   *
   * @param shell
   *            the outer boundary of the new <code>Polygon</code>, or
   *            <code>null</code> or an empty <code>LinearRing</code> if
   *            the empty geometry is to be created.
   * @throws IllegalArgumentException if the boundary ring is invalid
   */
  public Polygon polygon(final LinearRing shell) {
    return new PolygonImpl(this, shell);
  }

  public Polygon polygon(final List<?> rings) {
    if (rings.size() == 0) {
      return polygon();
    } else {
      final LinearRing[] linearRings = new LinearRing[rings.size()];
      for (int i = 0; i < rings.size(); i++) {
        linearRings[i] = getLinearRing(rings, i);
      }
      return new PolygonImpl(this, linearRings);
    }
  }

  public Polygon polygon(final Object... rings) {
    return polygon(Arrays.asList(rings));
  }

  public Polygon polygon(final Polygon polygon) {
    return polygon.copy(this);
  }

  /**
   * Project the geometry if it is in a different coordinate system
   * 
   * @param geometry
   * @return
   */
  public <G extends Geometry> G project(final G geometry) {
    return geometry.convert(this);
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("type", "geometryFactory");
    map.put("srid", getSrid());
    map.put("axisCount", getAxisCount());

    final double scaleXY = getScaleXY();
    if (scaleXY > 0) {
      map.put("scaleXy", scaleXY);
    }
    if (axisCount > 2) {
      final double scaleZ = getScaleZ();
      if (scaleZ > 0) {
        map.put("scaleZ", scaleZ);
      }
    }
    return map;
  }

  @Override
  public String toString() {
    final StringBuffer string = new StringBuffer();
    final int srid = getSrid();
    if (coordinateSystem != null) {
      string.append(coordinateSystem.getName());
      string.append(", ");
    }
    string.append("srid=");
    string.append(srid);
    string.append(", axisCount=");
    string.append(axisCount);
    string.append(", scales=");
    string.append(Arrays.toString(scales));
    return string.toString();
  }
}
