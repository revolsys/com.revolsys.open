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
package com.revolsys.geometry.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.revolsys.collection.CollectionUtil;
import com.revolsys.collection.map.IntHashMap;
import com.revolsys.collection.map.Maps;
import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.geometry.cs.esri.EsriCoordinateSystems;
import com.revolsys.geometry.cs.projection.CoordinatesOperation;
import com.revolsys.geometry.cs.projection.ProjectionFactory;
import com.revolsys.geometry.graph.linemerge.LineMerger;
import com.revolsys.geometry.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.geometry.model.impl.GeometryCollectionImpl;
import com.revolsys.geometry.model.impl.LineStringDouble;
import com.revolsys.geometry.model.impl.LineStringDoubleGf;
import com.revolsys.geometry.model.impl.LinearRingDoubleGf;
import com.revolsys.geometry.model.impl.MultiLineStringImpl;
import com.revolsys.geometry.model.impl.MultiPointImpl;
import com.revolsys.geometry.model.impl.MultiPolygonImpl;
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.geometry.model.impl.PointDoubleGf;
import com.revolsys.geometry.model.impl.PolygonImpl;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.LineSegmentDoubleGF;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.record.io.format.wkt.WktParser;
import com.revolsys.util.MathUtil;
import com.revolsys.util.Property;

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
public class GeometryFactory implements GeometryFactoryProxy, Serializable, MapSerializer {
  private static IntHashMap<IntHashMap<List<GeometryFactory>>> factoriesBySrid = new IntHashMap<>();

  private static final long serialVersionUID = 4328651897279304108L;

  public static final BoundingBox boundingBox(final Geometry geometry) {
    if (geometry == null) {
      return floating3().boundingBox();
    } else {
      return geometry.getBoundingBox();
    }
  }

  public static void clear() {
    factoriesBySrid.clear();
  }

  public static GeometryFactory create(final Map<String, Object> properties) {
    final int coordinateSystemId = Maps.getInteger(properties, "srid", 0);
    final int axisCount = Maps.getInteger(properties, "axisCount", 2);
    final double scaleXY = Maps.getDouble(properties, "scaleXy", 0.0);
    final double scaleZ = Maps.getDouble(properties, "scaleZ", 0.0);
    return GeometryFactory.fixed(coordinateSystemId, axisCount, scaleXY, scaleZ);
  }

  public static GeometryFactory fixed(final CoordinateSystem coordinateSystem, final int axisCount,
    final double... scales) {
    if (coordinateSystem == null) {
      return fixed(0, axisCount, scales);
    } else {
      final int coordinateSystemId = coordinateSystem.getId();
      if (coordinateSystemId == 0) {
        return new GeometryFactory(coordinateSystem, axisCount, scales);
      } else {
        return fixed(coordinateSystemId, axisCount, scales);
      }
    }

  }

  /**
   * <p>
   * Get a GeometryFactory with the coordinate system, 2D axis (x &amp; y) and a
   * fixed x, y precision model.
   * </p>
   *
   * @param coordinateSystemId The <a href="http://spatialreference.org/ref/epsg/">EPSG
   *          coordinate system id</a>.
   * @param scaleXY The scale factor used to round the x, y coordinates. The
   *          precision is 1 / scaleXy. A scale factor of 1000 will give a
   *          precision of 1 / 1000 = 1mm for projected coordinate systems using
   *          metres.
   * @return The geometry factory.
   */
  public static GeometryFactory fixed(final int coordinateSystemId, final double... scales) {
    return fixed(coordinateSystemId, scales.length + 1, scales);
  }

  /**
   * <p>
   * Get a GeometryFactory with the coordinate system, number of axis and a
   * fixed x, y &amp; fixed z precision models.
   * </p>
   *
   * @param coordinateSystemId The <a href="http://spatialreference.org/ref/epsg/">EPSG
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
  public static GeometryFactory fixed(final int coordinateSystemId, final int axisCount,
    double... scales) {
    synchronized (factoriesBySrid) {
      scales = getScales(axisCount, scales);
      GeometryFactory factory = null;
      IntHashMap<List<GeometryFactory>> factoriesByAxisCount = factoriesBySrid
        .get(coordinateSystemId);
      if (factoriesByAxisCount == null) {
        factoriesByAxisCount = new IntHashMap<>();
        factoriesBySrid.put(coordinateSystemId, factoriesByAxisCount);
      }
      List<GeometryFactory> factories = factoriesByAxisCount.get(axisCount);
      if (factories == null) {
        factories = new ArrayList<>();
        factoriesByAxisCount.put(axisCount, factories);
      } else {
        final int size = factories.size();
        for (int i = 0; i < size; i++) {
          final GeometryFactory matchFactory = factories.get(i);
          if (matchFactory.scalesEqual(scales)) {
            return matchFactory;
          }
        }
      }
      if (factory == null) {
        factory = new GeometryFactory(coordinateSystemId, axisCount, scales);
        factories.add(factory);
      }
      return factory;
    }
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
  public static GeometryFactory fixedNoSrid(final double... scales) {
    return fixed(0, scales);
  }

  /**
   * get a  geometry factory with a floating scale.
   */
  public static GeometryFactory floating(final CoordinateSystem coordinateSystem,
    final int axisCount) {
    if (coordinateSystem == null) {
      return floating(0, axisCount);
    } else {
      final int coordinateSystemId = coordinateSystem.getId();
      if (coordinateSystemId == 0) {
        return new GeometryFactory(coordinateSystem, axisCount);
      } else {
        return floating(coordinateSystemId, axisCount);
      }
    }
  }

  /**
   * <p>
   * Get a GeometryFactory with the coordinate system, number of axis and a
   * floating precision model.
   * </p>
   *
   * @param coordinateSystemId The <a href="http://spatialreference.org/ref/epsg/">EPSG
   *          coordinate system id</a>.
   * @param axisCount The number of coordinate axis. 2 for 2D x &amp; y
   *          coordinates. 3 for 3D x, y &amp; z coordinates.
   * @return The geometry factory.
   */
  public static GeometryFactory floating(final int coordinateSystemId, final int axisCount) {
    return fixed(coordinateSystemId, axisCount);
  }

  /**
   * <p>
   * Get a GeometryFactory with no coordinate system, 3D axis (x, y &amp; z) and
   * a floating precision model.
   * </p>
   *
   * @return The geometry factory.
   */
  public static GeometryFactory floating3() {
    return fixed(0, 0.0, 0.0);
  }

  /**
   * get a 3d geometry factory with a floating scale.
   */
  public static GeometryFactory floating3(final CoordinateSystem coordinateSystem) {
    if (coordinateSystem == null) {
      return floating3();
    } else {
      final int coordinateSystemId = coordinateSystem.getId();
      if (coordinateSystemId == 0) {
        return new GeometryFactory(coordinateSystem, 3);
      } else {
        return floating3(coordinateSystemId);
      }
    }
  }

  /**
   * <p>
   * Get a GeometryFactory with the coordinate system, 3D axis (x, y &amp; z)
   * and a floating precision models.
   * </p>
   *
   * @param coordinateSystemId The <a href="http://spatialreference.org/ref/epsg/">EPSG
   *          coordinate system id</a>.
   * @return The geometry factory.
   */
  public static GeometryFactory floating3(final int coordinateSystemId) {
    return fixed(coordinateSystemId, 0.0, 0.0);
  }

  public static GeometryFactory get(final Object factory) {
    if (factory instanceof GeometryFactory) {
      return (GeometryFactory)factory;
    } else if (factory instanceof Map) {
      @SuppressWarnings("unchecked")
      final Map<String, Object> properties = (Map<String, Object>)factory;
      return create(properties);
    } else {
      return null;
    }
  }

  public static String getAxisName(final int axisIndex) {
    switch (axisIndex) {
      case 0:
        return "X";
      case 1:
        return "Y";
      case 2:
        return "Z";
      case 3:
        return "M";
      default:
        return String.valueOf(axisIndex);
    }
  }

  /**
   * <p>
   * Get a GeometryFactory with the coordinate system, 3D axis (x, y &amp; z)
   * and a floating precision models.
   * </p>
   *
   * @param coordinateSystemId The <a href="http://spatialreference.org/ref/epsg/">EPSG
   *          coordinate system id</a>.
   * @return The geometry factory.
   */
  public static GeometryFactory getFactory(final String wkt) {
    final CoordinateSystem esriCoordinateSystem = EsriCoordinateSystems.getCoordinateSystem(wkt);
    if (esriCoordinateSystem == null) {
      return floating3();
    } else {
      final CoordinateSystem epsgCoordinateSystem = EpsgCoordinateSystems
        .getCoordinateSystem(esriCoordinateSystem);
      final int coordinateSystemId = epsgCoordinateSystem.getId();
      return fixed(coordinateSystemId, 0.0, 0.0);
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
    return floating3(4326);
  }

  public static GeometryFactory worldMercator() {
    return floating3(3857);
  }

  private int axisCount = 2;

  private final CoordinateSystem coordinateSystem;

  private final int coordinateSystemId;

  private final WktParser parser = new WktParser(this);

  private double[] scales;

  protected GeometryFactory(final CoordinateSystem coordinateSystem, final int axisCount,
    final double... scales) {
    this.coordinateSystemId = coordinateSystem.getId();
    this.coordinateSystem = coordinateSystem;
    init(axisCount, scales);
  }

  protected GeometryFactory(final int coordinateSystemId, final int axisCount,
    final double... scales) {
    this.coordinateSystemId = coordinateSystemId;
    this.coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(coordinateSystemId);
    init(axisCount, scales);
  }

  public void addGeometries(final List<Geometry> geometryList, final Geometry geometry) {
    if (geometry != null && !geometry.isEmpty()) {
      for (final Geometry part : geometry.geometries()) {
        if (part != null && !part.isEmpty()) {
          geometryList.add(part.copy(this));
        }
      }
    }
  }

  public BoundingBox boundingBox() {
    return new BoundingBoxDoubleGf(this);
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
      if (geometry != null) {
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
      throw new IllegalArgumentException("Unknown geometry type " + collectionDataType);
    }
  }

  public GeometryFactory convertAxisCount(final int axisCount) {
    if (axisCount == getAxisCount()) {
      return this;
    } else {
      final int coordinateSystemId = getCoordinateSystemId();
      final double[] scales = new double[this.scales.length - 1];
      System.arraycopy(this.scales, 1, scales, 0, scales.length);
      return GeometryFactory.fixed(coordinateSystemId, axisCount, scales);
    }
  }

  public GeometryFactory convertScales(final double... scales) {
    final int coordinateSystemId = getCoordinateSystemId();
    final int axisCount = getAxisCount();
    return GeometryFactory.fixed(coordinateSystemId, axisCount, scales);
  }

  public GeometryFactory convertSrid(final int coordinateSystemId) {
    if (coordinateSystemId == getCoordinateSystemId()) {
      return this;
    } else {
      final int axisCount = getAxisCount();
      return GeometryFactory.fixed(coordinateSystemId, axisCount, this.scales);
    }
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
    final Point newPoint = new PointDouble(this.axisCount, coordinates);
    return newPoint;
  }

  public Point createCoordinates(final Point point) {
    return getPreciseCoordinates(point);
  }

  public LineString createCoordinatesList(final Collection<?> points) {
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
          final Point projectedPoint = ((Point)object).convert(this);
          point = projectedPoint;
        } else if (object instanceof double[]) {
          point = new PointDouble((double[])object);
        } else if (object instanceof LineString) {
          final LineString LineString = (LineString)object;
          point = LineString.getPoint(0);
        } else {
          throw new IllegalArgumentException("Unexepected data type: " + object);
        }

        if (point != null && point.getAxisCount() > 1) {
          CoordinatesListUtil.setCoordinates(this, coordinates, axisCount, i, point);
          i++;
        }
      }
      return new LineStringDouble(axisCount, i, coordinates);
    }
  }

  public Geometry geometry() {
    return point();
  }

  /**
   * <p>
   * Construct a new new geometry of the requested target geometry class.
   * <p>
   *
   * @param targetClass
   * @param geometry
   * @return
   */
  @SuppressWarnings({
    "unchecked"
  })
  public <V extends Geometry> V geometry(final Class<?> targetClass, Geometry geometry) {
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
            final List<LineString> mergedLineStrings = LineMerger.merge(geometry);
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
   * Construct a new new geometry my flattening the input geometries, ignoring and null or empty
   * geometries. If there are no geometries an empty {@link GeometryCollection} will be returned.
   * If there is one geometry that single geometry will be returned. Otherwise the result
   * will be a subclass of {@link GeometryCollection}.
   *
   * @author Paul Austin <paul.austin@revolsys.com>
   * @param geometries
   * @return
   */
  @SuppressWarnings("unchecked")
  public <V extends Geometry> V geometry(final Collection<? extends Geometry> geometries) {
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
   * This is a convenient way to change the <tt>LineString</tt>
   * used to represent a geometry, or to change the
   * factory used for a geometry.
   * <p>
   * {@link Geometry#clone()} can also be used to make a deep copy,
   * but it does not allow changing the LineString type.
   *
   * @return a deep copy of the input geometry, using the LineString type of this factory
   *
   * @see Geometry#clone()
   */
  public Geometry geometry(final Geometry geometry) {
    if (geometry == null) {
      return null;
    } else {
      final int coordinateSystemId = getCoordinateSystemId();
      final int geometrySrid = geometry.getCoordinateSystemId();
      if (coordinateSystemId == 0 && geometrySrid != 0) {
        final GeometryFactory geometryFactory = GeometryFactory.fixed(geometrySrid, this.axisCount,
          getScaleXY(), getScaleZ());
        return geometryFactory.geometry(geometry);
      } else
        if (coordinateSystemId != 0 && geometrySrid != 0 && geometrySrid != coordinateSystemId) {
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
    if (Property.hasValue(wkt)) {
      return (T)this.parser.parseGeometry(wkt);
    } else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends Geometry> T geometry(final String wkt,
    final boolean useAxisCountFromGeometryFactory) {
    return (T)this.parser.parseGeometry(wkt, useAxisCountFromGeometryFactory);
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
  public <V extends GeometryCollection> V geometryCollection(final Geometry... geometries) {
    return (V)geometryCollection(Arrays.asList(geometries));
  }

  public int getAxisCount() {
    return this.axisCount;
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
  public CoordinatesOperation getCoordinatesOperation(final GeometryFactory geometryFactory) {
    final CoordinateSystem coordinateSystem = getCoordinateSystem();
    final CoordinateSystem otherCoordinateSystem = geometryFactory.getCoordinateSystem();
    return ProjectionFactory.getCoordinatesOperation(coordinateSystem, otherCoordinateSystem);
  }

  @Override
  public CoordinateSystem getCoordinateSystem() {
    return this.coordinateSystem;
  }

  @Override
  public int getCoordinateSystemId() {
    return this.coordinateSystemId;
  }

  public GeometryFactory getGeographicGeometryFactory() {
    if (this.coordinateSystem instanceof GeographicCoordinateSystem) {
      return this;
    } else if (this.coordinateSystem instanceof ProjectedCoordinateSystem) {
      final ProjectedCoordinateSystem projectedCs = (ProjectedCoordinateSystem)this.coordinateSystem;
      final GeographicCoordinateSystem geographicCs = projectedCs.getGeographicCoordinateSystem();
      final int coordinateSystemId = geographicCs.getId();
      return fixed(coordinateSystemId, getAxisCount(), 0.0, 0.0);
    } else {
      return fixed(4326, getAxisCount(), 0.0, 0.0);
    }
  }

  public List<Geometry> getGeometries(final Collection<? extends Geometry> geometries) {
    final List<Geometry> geometryList = new ArrayList<Geometry>();
    for (final Geometry geometry : geometries) {
      addGeometries(geometryList, geometry);
    }
    return geometryList;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this;
  }

  private LinearRing getLinearRing(final List<?> rings, final int index) {
    final Object ring = rings.get(index);
    if (ring instanceof LinearRing) {
      return (LinearRing)ring;
    } else if (ring instanceof LineString) {
      final LineString points = (LineString)ring;
      return linearRing(points);
    } else if (ring instanceof LineString) {
      final LineString line = (LineString)ring;
      final LineString points = line;
      return linearRing(points);
    } else if (ring instanceof double[]) {
      final double[] coordinates = (double[])ring;
      return linearRing(getAxisCount(), coordinates);
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
      } else if (value instanceof LineString) {
        final LineString coordinates = (LineString)value;
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
        final List<LineString> coordinateList = (List<LineString>)value;
        polygon = polygon(coordinateList);
      } else if (value instanceof LineString) {
        final LineString coordinateList = (LineString)value;
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
    return new PointDouble(coordinates);
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
    if (axisIndex < 0 || axisIndex >= this.scales.length) {
      return 0;
    } else {
      return this.scales[0];
    }
  }

  public double getScaleXY() {
    return getScale(0);
  }

  public double getScaleZ() {
    return getScale(2);
  }

  @Override
  public int hashCode() {
    return this.coordinateSystemId;
  }

  public boolean hasM() {
    return this.axisCount > 3;
  }

  public boolean hasZ() {
    return this.axisCount > 2;
  }

  protected void init(final int axisCount, final double... scales) {
    this.axisCount = Math.max(axisCount, 2);
    this.scales = getScales(axisCount, scales);
  }

  public boolean isFloating() {
    return getScale(0) == 0;
  }

  public boolean isGeographics() {
    return this.coordinateSystem instanceof GeographicCoordinateSystem;
  }

  public boolean isHasCoordinateSystem() {
    return this.coordinateSystem != null;
  }

  public boolean isProjected() {
    return this.coordinateSystem instanceof ProjectedCoordinateSystem;
  }

  public boolean isSameCoordinateSystem(final GeometryFactory geometryFactory) {
    if (geometryFactory == null) {
      return false;
    } else {
      final int coordinateSystemId = getCoordinateSystemId();
      final int coordinateSystemId2 = geometryFactory.getCoordinateSystemId();
      if (coordinateSystemId == coordinateSystemId2) {
        return true;
      } else {
        final CoordinateSystem coordinateSystem = getCoordinateSystem();
        final CoordinateSystem coordinateSystem2 = geometryFactory.getCoordinateSystem();
        if (coordinateSystem == null) {
          if (coordinateSystemId <= 0) {
            return true;
          } else if (coordinateSystem2 == null && coordinateSystemId2 <= 0) {
            return true;
          } else {
            return false;
          }
        } else if (coordinateSystem2 == null) {
          if (coordinateSystemId2 <= 0) {
            return true;
          } else if (coordinateSystemId <= 0) {
            return true;
          } else {
            return false;
          }
        } else {
          return coordinateSystem.equals(coordinateSystem2);
        }
      }
    }
  }

  public LinearRing linearRing() {
    return new LinearRingDoubleGf(this);
  }

  public LinearRing linearRing(final Collection<?> points) {
    if (points == null || points.isEmpty()) {
      return linearRing();
    } else {
      final LineString coordinatesList = createCoordinatesList(points);
      return linearRing(coordinatesList);
    }
  }

  public LinearRing linearRing(final int axisCount, final double... coordinates) {
    return new LinearRingDoubleGf(this, axisCount, coordinates);
  }

  public LinearRing linearRing(final int axisCount, final int vertexCount,
    final double... coordinates) {
    return new LinearRingDoubleGf(this, axisCount, vertexCount, coordinates);
  }

  /**
   * Creates a {@link LinearRing} using the given {@link LineString}.
   * A null or empty array creates an empty LinearRing.
   * The points must form a closed and simple linestring.
   *
   * @param coordinates a LineString (possibly empty), or null
   * @return the created LinearRing
   * @throws IllegalArgumentException if the ring is not closed, or has too few points
   */
  public LinearRing linearRing(final LineString points) {
    return new LinearRingDoubleGf(this, points);
  }

  /**
   * Creates a {@link LinearRing} using the given {@link Coordinates}s.
   * A null or empty array creates an empty LinearRing.
   * The points must form a closed and simple linestring.
   * @param coordinates an array without null elements, or an empty array, or null
   * @return the created LinearRing
   * @throws IllegalArgumentException if the ring is not closed, or has too few points
   */
  public LinearRing linearRing(final Point... points) {
    if (points == null || points.length == 0) {
      return linearRing();
    } else {
      return linearRing(Arrays.asList(points));
    }
  }

  public LineSegment lineSegment(final Point p0, final Point p1) {
    return new LineSegmentDoubleGF(this, p0, p1);
  }

  public LineString lineString() {
    return new LineStringDoubleGf(this);
  }

  public LineString lineString(final Collection<?> points) {
    if (points.isEmpty()) {
      return lineString();
    } else {
      final LineString coordinatesList = createCoordinatesList(points);
      return lineString(coordinatesList);
    }
  }

  public LineString lineString(final int axisCount, final double... coordinates) {
    return new LineStringDoubleGf(this, axisCount, coordinates);
  }

  public LineString lineString(final int axisCount, final int vertexCount,
    final double... coordinates) {
    return new LineStringDoubleGf(this, axisCount, vertexCount, coordinates);
  }

  public LineString lineString(final int axisCount, final Number[] coordinates) {
    return new LineStringDoubleGf(this, axisCount, coordinates);
  }

  public LineString lineString(final LineString lineString) {
    if (lineString == null || lineString.isEmpty()) {
      return lineString();
    } else {
      return new LineStringDoubleGf(this, lineString);
    }
  }

  public LineString lineString(final Point... points) {
    if (points == null) {
      return lineString();
    } else {
      final List<Point> LineString = new ArrayList<>();
      for (final Point point : points) {
        if (point != null && !point.isEmpty()) {
          LineString.add(point);
        }
      }
      return lineString(LineString);
    }
  }

  public void makePrecise(final double... values) {
    makePrecise(values, values);
  }

  public void makePrecise(final double[] values, final double[] valuesPrecise) {
    for (int i = 0; i < valuesPrecise.length; i++) {
      final int axisIndex = i % this.axisCount;
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

  public MultiLineStringImpl multiLineString() {
    return new MultiLineStringImpl(this);
  }

  public MultiLineString multiLineString(final Collection<?> lines) {
    final LineString[] lineArray = getLineStringArray(lines);
    return multiLineString(lineArray);
  }

  public MultiLineString multiLineString(final Geometry geometry) {
    if (geometry instanceof LineString) {
      final LineString line = (LineString)geometry.convert(this);
      return new MultiLineStringImpl(this, line);
    } else if (geometry instanceof MultiLineString) {
      final MultiLineString multiLineString = (MultiLineString)geometry;
      return multiLineString.convert(this);
    } else if (geometry instanceof GeometryCollection) {
      final GeometryCollection collection = (GeometryCollection)geometry;
      final List<LineString> lines = new ArrayList<>();
      for (final Geometry part : collection.geometries()) {
        if (part instanceof LineString) {
          lines.add((LineString)part);
        } else {
          throw new IllegalArgumentException("Cannot convert class " + part.getClass() + " to "
            + MultiLineString.class + "\n" + geometry);
        }
      }
      return multiLineString(lines);
    } else {
      throw new IllegalArgumentException("Cannot convert class " + geometry.getClass() + " to "
        + MultiLineString.class + "\n" + geometry);
    }
  }

  public MultiLineString multiLineString(final int axisCount, final double[]... linesCoordinates) {
    if (linesCoordinates == null) {
      return multiLineString();
    } else {
      final int lineCount = linesCoordinates.length;
      final LineString[] lines = new LineString[lineCount];
      for (int i = 0; i < lineCount; i++) {
        final double[] coordinates = linesCoordinates[i];
        lines[i] = lineString(axisCount, coordinates);
      }
      return new MultiLineStringImpl(this, lines);
    }
  }

  /**
   * Creates a MultiLineString using the given LineStrings; a null or empty
   * array will Construct a newn empty MultiLineString.
   *
   * @param lineStrings LineStrings, each of which may be empty but not null
   * @return the created MultiLineString
   */
  public MultiLineString multiLineString(final LineString... lines) {
    return new MultiLineStringImpl(this, lines);
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

  public MultiPoint multiPoint(final Geometry geometry) {
    if (geometry instanceof Point) {
      final Point point = (Point)geometry.convert(this);
      return new MultiPointImpl(this, point);
    } else if (geometry instanceof MultiPoint) {
      final MultiPoint multiPoint = (MultiPoint)geometry;
      return multiPoint.convert(this);
    } else if (geometry instanceof GeometryCollection) {
      final GeometryCollection collection = (GeometryCollection)geometry;
      final List<Point> points = new ArrayList<>();
      for (final Geometry part : collection.geometries()) {
        if (part instanceof Point) {
          points.add((Point)part);
        } else {
          throw new IllegalArgumentException("Cannot convert class " + part.getClass() + " to "
            + MultiPoint.class + "\n" + geometry);
        }
      }
      return multiPoint(points);
    } else {
      throw new IllegalArgumentException("Cannot convert class " + geometry.getClass() + " to "
        + MultiPoint.class + "\n" + geometry);
    }
  }

  public MultiPoint multiPoint(final int axisCount, final double... coordinates) {
    if (coordinates == null || coordinates.length == 0 || axisCount < 2) {
      return multiPoint();
    } else if (coordinates.length % axisCount != 0) {
      throw new IllegalArgumentException(
        "Coordinates length=" + coordinates.length + " must be a multiple of " + axisCount);
    } else {
      final Point[] points = new Point[coordinates.length / axisCount];
      for (int i = 0; i < points.length; i++) {
        final double[] newCoordinates = new double[axisCount];
        System.arraycopy(coordinates, i * axisCount, newCoordinates, 0, axisCount);
        final Point point = point(newCoordinates);
        points[i] = point;
      }
      return new MultiPointImpl(this, points);
    }
  }

  /**
   * Creates a {@link MultiPoint} using the
   * points in the given {@link LineString}.
   * A <code>null</code> or empty LineString creates an empty MultiPoint.
   *
   * @param coordinates a LineString (possibly empty), or <code>null</code>
   * @return a MultiPoint geometry
   */
  public MultiPoint multiPoint(final LineString coordinatesList) {
    if (coordinatesList == null) {
      return multiPoint();
    } else {
      final Point[] points = new Point[coordinatesList.getVertexCount()];
      for (int i = 0; i < points.length; i++) {
        final Point coordinates = coordinatesList.getPoint(i);
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
   * A null or empty array will Construct a newn empty MultiPoint.
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

  public MultiPolygon multiPolygon(final Geometry geometry) {
    if (geometry instanceof Polygon) {
      final Polygon polygon = (Polygon)geometry.convert(this);
      return new MultiPolygonImpl(this, polygon);
    } else if (geometry instanceof MultiPolygon) {
      final MultiPolygon multiPolygon = (MultiPolygon)geometry;
      return multiPolygon.convert(this);
    } else if (geometry instanceof GeometryCollection) {
      final GeometryCollection collection = (GeometryCollection)geometry;
      final List<Polygon> polygons = new ArrayList<>();
      for (final Geometry part : collection.geometries()) {
        if (part instanceof Polygon) {
          polygons.add((Polygon)part);
        } else {
          throw new IllegalArgumentException("Cannot convert class " + part.getClass() + " to "
            + MultiPolygon.class + "\n" + geometry);
        }
      }
      return multiPolygon(polygons);
    } else {
      throw new IllegalArgumentException("Cannot convert class " + geometry.getClass() + " to "
        + MultiPolygon.class + "\n" + geometry);
    }
  }

  public MultiPolygon multiPolygon(final Object... polygons) {
    return multiPolygon(Arrays.asList(polygons));
  }

  /**
   * Creates a MultiPolygon using the given Polygons; a null or empty array
   * will Construct a newn empty Polygon. The polygons must conform to the
   * assertions specified in the <A
   * HREF="http://www.opengis.org/techno/specs.htm">OpenGIS Simple Features
   * Specification for SQL</A>.
   *
   * @param polygons
   *            Polygons, each of which may be empty but not null
   * @return the created MultiPolygon
   */
  public MultiPolygon multiPolygon(final Polygon[] polygons) {
    return new MultiPolygonImpl(this, polygons);
  }

  /**
   * <p>Construct a newn empty {@link Point}.</p>
   *
   * @return The point.
   */
  public Point point() {
    return new PointDoubleGf(this);
  }

  /**
   * <p>Construct a new new {@link Point} from the specified point coordinates.
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
      return new PointDoubleGf(this, coordinates);
    }
  }

  /**
   * Creates a Point using the given LineString; a null or empty
   * LineString will Construct a newn empty Point.
   *
   * @param points a LineString (possibly empty), or null
   * @return the created Point
   */
  public Point point(final LineString points) {
    if (points == null) {
      return point();
    } else {
      final int size = points.getVertexCount();
      if (size == 0) {
        return point();
      } else if (size == 1) {
        final int axisCount = Math.min(points.getAxisCount(), getAxisCount());
        final double[] coordinates = new double[axisCount];
        for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
          final double coordinate = points.getCoordinate(0, axisIndex);
          coordinates[axisIndex] = coordinate;
        }
        return point(coordinates);
      } else {
        throw new IllegalArgumentException("Point can only have 1 vertex not " + size);
      }
    }
  }

  /**
   * <p>Construct a new new {@link Point} from the object using the following rules.<p>
   * <ul>
   *   <li><code>null</code> using {@link #point()}</li>
   *   <li>Instances of {@link Point} using {@link Point#copy(GeometryFactory)}</li>
   *   <li>Instances of {@link Coordinates} using {@link #point(Point)}</li>
   *   <li>Instances of {@link LineString} using {@link #point(LineString)}</li>
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
    } else if (object instanceof LineString) {
      return point((LineString)object);
    } else {
      throw new IllegalArgumentException("Cannot Construct a new point from " + object.getClass());
    }
  }

  /**
   * <p>Construct a new new {@link Point} from the specified point ({@link Coordinates}).
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

  public Polygon polygon(final LineString... rings) {
    final List<LineString> ringList = Arrays.asList(rings);
    return polygon(ringList);
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

  private boolean scalesEqual(final double[] scales) {
    return Arrays.equals(this.scales, scales);
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("type", "geometryFactory");
    map.put("srid", getCoordinateSystemId());
    map.put("axisCount", getAxisCount());

    final double scaleXY = getScaleXY();
    if (scaleXY > 0) {
      map.put("scaleXy", scaleXY);
    }
    if (this.axisCount > 2) {
      final double scaleZ = getScaleZ();
      if (scaleZ > 0) {
        map.put("scaleZ", scaleZ);
      }
    }
    return map;
  }

  @Override
  public String toString() {
    final StringBuilder string = new StringBuilder();
    final int coordinateSystemId = getCoordinateSystemId();
    if (this.coordinateSystem != null) {
      string.append(this.coordinateSystem.getName());
      string.append(", ");
    }
    string.append("coordinateSystemId=");
    string.append(coordinateSystemId);
    string.append(", axisCount=");
    string.append(this.axisCount);
    string.append(", scales=");
    string.append(Arrays.toString(this.scales));
    return string.toString();
  }
}
