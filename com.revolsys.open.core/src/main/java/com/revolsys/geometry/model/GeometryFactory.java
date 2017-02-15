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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.revolsys.collection.CollectionUtil;
import com.revolsys.collection.map.IntHashMap;
import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
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
import com.revolsys.geometry.model.impl.AbstractPoint;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXYGeometryFactory;
import com.revolsys.geometry.model.impl.BoundingBoxGeometryFactory;
import com.revolsys.geometry.model.impl.GeometryCollectionImpl;
import com.revolsys.geometry.model.impl.LineStringDoubleBuilder;
import com.revolsys.geometry.model.impl.LineStringDoubleGf;
import com.revolsys.geometry.model.impl.LinearRingDoubleGf;
import com.revolsys.geometry.model.impl.MultiLineStringImpl;
import com.revolsys.geometry.model.impl.MultiPointImpl;
import com.revolsys.geometry.model.impl.MultiPolygonImpl;
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.geometry.model.impl.PointDoubleGf;
import com.revolsys.geometry.model.impl.PointDoubleXYGeometryFactory;
import com.revolsys.geometry.model.impl.PolygonImpl;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.LineSegmentDoubleGF;
import com.revolsys.geometry.util.BoundingBoxUtil;
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
  private class EmptyPoint extends AbstractPoint {
    private static final long serialVersionUID = 1L;

    @Override
    public Point clone() {
      return this;
    }

    @Override
    public void copyCoordinates(final double[] coordinates) {
      Arrays.fill(coordinates, java.lang.Double.NaN);
    }

    @Override
    public int getAxisCount() {
      return GeometryFactory.this.axisCount;
    }

    @Override
    public double getCoordinate(final int axisIndex) {
      return java.lang.Double.NaN;
    }

    @Override
    public GeometryFactory getGeometryFactory() {
      return GeometryFactory.this;
    }

    @Override
    public double getM() {
      return java.lang.Double.NaN;
    }

    @Override
    public double getX() {
      return java.lang.Double.NaN;
    }

    @Override
    public double getY() {
      return java.lang.Double.NaN;
    }

    @Override
    public double getZ() {
      return java.lang.Double.NaN;
    }

    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public String toString() {
      return toEwkt();
    }
  }

  private static final IntHashMap<IntHashMap<List<GeometryFactory>>> factoriesBySrid = new IntHashMap<>();

  private static final double[] SCALES_FLOATING_2 = new double[2];

  public static final double[] SCALES_FLOATING_3 = new double[3];

  public static final GeometryFactory DEFAULT_2D = fixed(0, SCALES_FLOATING_2);

  /**
   * The default GeometryFactory with no coordinate system, 3D axis (x, y &amp; z) and a floating precision model.
   */
  public static final GeometryFactory DEFAULT_3D = fixed(0, SCALES_FLOATING_3);

  private static final long serialVersionUID = 4328651897279304108L;

  public static final BoundingBox boundingBox(final Geometry geometry) {
    if (geometry == null) {
      return DEFAULT_3D.newBoundingBoxEmpty();
    } else {
      return geometry.getBoundingBox();
    }
  }

  public static void clear() {
    factoriesBySrid.clear();
  }

  public static GeometryFactory fixed(final CoordinateSystem coordinateSystem, final int axisCount,
    final double... scales) {
    if (coordinateSystem == null) {
      return fixed(0, axisCount, scales);
    } else {
      final int coordinateSystemId = coordinateSystem.getCoordinateSystemId();
      if (coordinateSystemId == 0) {
        return new GeometryFactory(coordinateSystem, axisCount, scales);
      } else {
        return fixed(coordinateSystem, coordinateSystemId, axisCount, scales);
      }
    }

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
  private static GeometryFactory fixed(final CoordinateSystem coordinateSystem,
    final int coordinateSystemId, final int axisCount, final double... scales) {
    synchronized (factoriesBySrid) {
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
          if (matchFactory.equalsScales(scales)) {
            return matchFactory;
          }
        }
      }
      if (factory == null) {
        factory = new GeometryFactory(coordinateSystem, coordinateSystemId, axisCount, scales);
        factories.add(factory);
      }
      return factory;
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
    return fixed(coordinateSystemId, scales.length, scales);
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
    final double... scales) {
    synchronized (factoriesBySrid) {
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
          if (matchFactory.equalsScales(scales)) {
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
      final int coordinateSystemId = coordinateSystem.getCoordinateSystemId();
      if (coordinateSystemId <= 0) {
        final double[] scales = newScalesFloating(axisCount);
        return new GeometryFactory(coordinateSystem, axisCount, scales);
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
    final double[] scales = newScalesFloating(axisCount);
    return fixed(coordinateSystemId, axisCount, scales);
  }

  /**
   * get a 3d geometry factory with a floating scale.
   */
  public static GeometryFactory floating3(final CoordinateSystem coordinateSystem) {
    if (coordinateSystem == null) {
      return DEFAULT_3D;
    } else {
      final int coordinateSystemId = coordinateSystem.getCoordinateSystemId();
      if (coordinateSystemId == 0) {
        return new GeometryFactory(coordinateSystem, 3, SCALES_FLOATING_3);
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
    return fixed(coordinateSystemId, SCALES_FLOATING_3);
  }

  public static GeometryFactory get(final Object factory) {
    if (factory instanceof GeometryFactory) {
      return (GeometryFactory)factory;
    } else if (factory instanceof Map) {
      @SuppressWarnings("unchecked")
      final Map<String, Object> properties = (Map<String, Object>)factory;
      return newGeometryFactory(properties);
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
      return DEFAULT_3D;
    } else {
      final CoordinateSystem epsgCoordinateSystem = EpsgCoordinateSystems
        .getCoordinateSystem(esriCoordinateSystem);
      final int coordinateSystemId = epsgCoordinateSystem.getCoordinateSystemId();
      return floating(coordinateSystemId, 3);
    }
  }

  private static Set<DataType> getGeometryDataTypes(
    final Collection<? extends Geometry> geometries) {
    final Set<DataType> dataTypes = new LinkedHashSet<>();
    for (final Geometry geometry : geometries) {
      final DataType dataType = geometry.getDataType();
      dataTypes.add(dataType);
    }
    return dataTypes;
  }

  @SuppressWarnings("unchecked")
  public static <G extends Geometry> G newGeometry(final List<? extends Geometry> geometries) {
    if (geometries == null || geometries.size() == 0) {
      return (G)GeometryFactory.DEFAULT_3D.geometry();
    } else {
      final GeometryFactory geometryFactory = geometries.get(0).getGeometryFactory();
      return geometryFactory.geometry(geometries);
    }
  }

  public static GeometryFactory newGeometryFactory(final Map<String, Object> properties) {
    final int coordinateSystemId = Maps.getInteger(properties, "srid", 0);
    final int axisCount = Maps.getInteger(properties, "axisCount", 2);
    final double scaleXY = Maps.getDouble(properties, "scaleXy", 0.0);
    final double scaleX = Maps.getDouble(properties, "scaleX", scaleXY);
    final double scaleY = Maps.getDouble(properties, "scaleY", scaleXY);
    final double scaleZ = Maps.getDouble(properties, "scaleZ", 0.0);
    return GeometryFactory.fixed(coordinateSystemId, axisCount, scaleX, scaleY, scaleZ);
  }

  public static double[] newScalesFixed(final int axisCount, final double scale) {
    final double[] scales = new double[Math.max(axisCount, 2)];
    Arrays.fill(scales, scale);
    return scales;
  }

  public static double[] newScalesFloating(final int axisCount) {
    if (axisCount < 3) {
      return SCALES_FLOATING_2;
    } else if (axisCount == 3) {
      return SCALES_FLOATING_3;
    } else {
      return new double[axisCount];
    }
  }

  public static double toResolution(final double scale) {
    if (scale > 0) {
      return 1 / scale;
    } else {
      return 0;
    }
  }

  public static GeometryFactory wgs84() {
    return floating3(4326);
  }

  public static GeometryFactory worldMercator() {
    return floating3(3857);
  }

  private int axisCount = 2;

  private final BoundingBox boundingBoxEmpty = new BoundingBoxGeometryFactory(this);

  private final CoordinateSystem coordinateSystem;

  private final int coordinateSystemId;

  private final EmptyPoint emptyPoint = new EmptyPoint();

  private transient final WktParser parser = new WktParser(this);

  protected double resolutionX = 0;

  protected double resolutionY = 0;

  protected double resolutionZ = 0;

  protected double[] scales;

  protected double scaleX = 0;

  protected double scaleY = 0;

  protected double scaleZ = 0;

  protected GeometryFactory(final CoordinateSystem coordinateSystem, final int axisCount,
    final double... scales) {
    this.coordinateSystem = coordinateSystem;
    if (coordinateSystem == null) {
      this.coordinateSystemId = 0;
    } else {
      this.coordinateSystemId = coordinateSystem.getCoordinateSystemId();
    }
    init(axisCount, scales);
  }

  protected GeometryFactory(final CoordinateSystem coordinateSystem, final int coordinateSystemId,
    final int axisCount, final double... scales) {
    this.coordinateSystemId = coordinateSystemId;
    if (coordinateSystem == null && coordinateSystemId > 0) {
      this.coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(coordinateSystemId);
    } else {
      this.coordinateSystem = coordinateSystem;
    }
    init(axisCount, scales);
  }

  protected GeometryFactory(final int coordinateSystemId, final int axisCount,
    final double... scales) {
    this.coordinateSystemId = coordinateSystemId;
    if (coordinateSystemId > 0) {
      this.coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(coordinateSystemId);
    } else {
      this.coordinateSystem = null;
    }
    init(axisCount, scales);
  }

  public void addGeometries(final List<Geometry> geometryList, final Geometry geometry) {
    if (geometry != null && !geometry.isEmpty()) {
      for (final Geometry part : geometry.geometries()) {
        if (part != null && !part.isEmpty()) {
          geometryList.add(part.newGeometry(this));
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
  public Geometry buildGeometry(final Iterable<? extends Geometry> geometries) {
    DataType collectionDataType = null;
    boolean isHeterogeneous = false;
    boolean hasGeometryCollection = false;
    final List<Geometry> geometryList = new ArrayList<>();
    for (final Geometry geometry : geometries) {
      if (geometry != null) {
        geometryList.add(geometry);
        DataType geometryDataType = geometry.getDataType();
        if (geometry instanceof LinearRing) {
          geometryDataType = DataTypes.LINE_STRING;
        }
        if (collectionDataType == null) {
          collectionDataType = geometryDataType;
        } else if (geometryDataType != collectionDataType) {
          isHeterogeneous = true;
        }
        if (geometry.isGeometryCollection()) {
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
      return geometryCollection(geometryList);
    } else if (geometryList.size() == 1) {
      return geometryList.iterator().next();
    } else if (DataTypes.POINT.equals(collectionDataType)) {
      return punctual(geometryList);
    } else if (DataTypes.LINE_STRING.equals(collectionDataType)) {
      return lineal(geometryList);
    } else if (DataTypes.POLYGON.equals(collectionDataType)) {
      return polygonal(geometryList);
    } else {
      throw new IllegalArgumentException("Unknown geometry type " + collectionDataType);
    }
  }

  @Override
  public GeometryFactory clone() {
    return this;
  }

  public GeometryFactory convertAxisCount(final int axisCount) {
    if (axisCount == getAxisCount()) {
      return this;
    } else {
      return fixed(this.coordinateSystem, this.coordinateSystemId, axisCount, this.scales);
    }
  }

  public GeometryFactory convertAxisCountAndScales(final int axisCount, final double... scales) {
    return fixed(this.coordinateSystem, this.coordinateSystemId, axisCount, scales);
  }

  public GeometryFactory convertCoordinateSystem(final CoordinateSystem coordinateSystem) {
    if (isSameCoordinateSystem(coordinateSystem)) {
      return this;
    } else {
      return GeometryFactory.fixed(coordinateSystem, this.axisCount, this.scales);
    }
  }

  public GeometryFactory convertScales(final double... scales) {
    return fixed(this.coordinateSystem, this.coordinateSystemId, this.axisCount, scales);
  }

  public GeometryFactory convertSrid(final int coordinateSystemId) {
    if (coordinateSystemId == getCoordinateSystemId()) {
      return this;
    } else {
      return GeometryFactory.fixed(coordinateSystemId, this.axisCount, this.scales);
    }
  }

  public double[] copyPrecise(final double[] values) {
    final double[] valuesPrecise = new double[values.length];
    makePrecise(values, valuesPrecise);
    return valuesPrecise;
  }

  private boolean equalsScales(final double[] scales) {
    final int minLength = Math.min(this.scales.length, scales.length);
    for (int i = 0; i < minLength; i++) {
      final double scale1 = this.scales[i];
      final double scale2 = scales[i];
      if (scale1 != scale2) {
        return false;
      }
    }
    return true;
  }

  public Geometry geometry() {
    return this.emptyPoint;
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
      geometry = geometry.newGeometry(this);
      if (geometry.isGeometryCollection()) {
        if (geometry.getGeometryCount() == 1) {
          geometry = geometry.getGeometry(0);
        } else {
          geometry = geometry.union();
          // Union doesn't use this geometry factory
          geometry = geometry.newGeometry(this);
        }
      }
      final Class<?> geometryClass = geometry.getClass();
      if (targetClass.isAssignableFrom(geometryClass)) {
        // TODO if geometry collection then clean up
        return (V)geometry;
      } else if (Point.class.isAssignableFrom(targetClass)) {
        if (geometry.getGeometryCount() == 1) {
          final Geometry part = geometry.getGeometry(0);
          if (part instanceof Point) {
            return (V)part;
          }
        }
      } else if (LineString.class.isAssignableFrom(targetClass)) {
        if (geometry.getGeometryCount() == 1) {
          final Geometry part = geometry.getGeometry(0);
          if (part instanceof LineString) {
            return (V)part;
          }
        } else {
          final List<LineString> mergedLineStrings = LineMerger.merge(geometry);
          if (mergedLineStrings.size() == 1) {
            return (V)mergedLineStrings.get(0);
          }
        }
      } else if (Polygon.class.isAssignableFrom(targetClass)) {
        if (geometry.getGeometryCount() == 1) {
          final Geometry part = geometry.getGeometry(0);
          if (part instanceof Polygon) {
            return (V)part;
          }
        }
      } else if (Punctual.class.isAssignableFrom(targetClass)) {
        if (geometry instanceof Punctual) {
          return (V)geometry;
        }
      } else if (Lineal.class.isAssignableFrom(targetClass)) {
        if (geometry instanceof Lineal) {
          return (V)geometry;
        }
      } else if (Polygonal.class.isAssignableFrom(targetClass)) {
        if (geometry instanceof Polygonal) {
          return (V)polygonal(geometry);
        }
      }
    }
    return null;
  }

  /**
   * Construct a new new geometry by flattening the input geometries, ignoring and null or empty
   * geometries. If there are no geometries, then an empty {@link Geometry} will be returned.
   * If there is one geometry that single geometry will be returned. Otherwise the result
   * will be a subclass of {@link GeometryCollection}.
   *
   * @author Paul Austin <paul.austin@revolsys.com>
   * @param geometries
   * @return
   */
  @SuppressWarnings("unchecked")
  public <V extends Geometry> V geometry(final Collection<? extends Geometry> geometries) {
    final List<Geometry> geometryList = getGeometries(geometries);
    if (geometryList == null || geometryList.size() == 0) {
      return (V)geometryCollection();
    } else if (geometryList.size() == 1) {
      return (V)geometryList.get(0);
    } else {
      final Set<DataType> dataTypes = getGeometryDataTypes(geometryList);
      if (dataTypes.size() == 1) {
        final DataType dataType = CollectionUtil.get(dataTypes, 0);
        if (dataType.equals(DataTypes.POINT)) {
          return (V)punctual(geometryList);
        } else if (dataType.equals(DataTypes.LINE_STRING)) {
          return (V)lineal(geometryList);
        } else if (dataType.equals(DataTypes.POLYGON)) {
          return (V)polygonal(geometryList);
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
          getScaleX(), getScaleY(), getScaleZ());
        return geometryFactory.geometry(geometry);
      } else if (coordinateSystemId != 0 && geometrySrid != 0
        && geometrySrid != coordinateSystemId) {
        if (geometry instanceof Point) {
          return geometry.newGeometry(this);
        } else if (geometry instanceof LineString) {
          return geometry.newGeometry(this);
        } else if (geometry instanceof Polygon) {
          return geometry.newGeometry(this);
        } else if (geometry instanceof Punctual) {
          final List<Geometry> geometries = new ArrayList<>();
          addGeometries(geometries, geometry);
          return punctual(geometries);
        } else if (geometry instanceof Lineal) {
          final List<Geometry> geometries = new ArrayList<>();
          addGeometries(geometries, geometry);
          return lineal(geometries);
        } else if (geometry instanceof Polygonal) {
          final List<Geometry> geometries = new ArrayList<>();
          addGeometries(geometries, geometry);
          return polygonal(geometries);
        } else if (geometry.isGeometryCollection()) {
          final List<Geometry> geometries = new ArrayList<>();
          addGeometries(geometries, geometry);
          return geometryCollection(geometries);
        } else {
          return geometry.newGeometry(this);
        }
      } else if (geometry instanceof Point) {
        final Point point = (Point)geometry;
        return point.newGeometry(this);
      } else if (geometry instanceof LinearRing) {
        final LinearRing linearRing = (LinearRing)geometry;
        return linearRing.newGeometry(this);
      } else if (geometry instanceof LineString) {
        final LineString lineString = (LineString)geometry;
        return lineString.newGeometry(this);
      } else if (geometry instanceof Polygon) {
        final Polygon polygon = (Polygon)geometry;
        return polygon(polygon);
      } else if (geometry instanceof Punctual) {
        final List<Geometry> geometries = new ArrayList<>();
        addGeometries(geometries, geometry);
        return punctual(geometries);
      } else if (geometry instanceof Lineal) {
        final List<Geometry> geometries = new ArrayList<>();
        addGeometries(geometries, geometry);
        return lineal(geometries);
      } else if (geometry instanceof Polygonal) {
        final List<Geometry> geometries = new ArrayList<>();
        addGeometries(geometries, geometry);
        return polygonal(geometries);
      } else if (geometry instanceof GeometryCollection) {
        final List<Geometry> geometries = new ArrayList<>();
        addGeometries(geometries, geometry);
        return geometryCollection(geometries);
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

  public Geometry geometryCollection() {
    return new GeometryCollectionImpl(this);
  }

  /**
   * Does not flattern nested geometry collections.
   *
   * @param geometries
   * @return
   */
  @SuppressWarnings("unchecked")
  public <G extends Geometry> G geometryCollection(final Iterable<? extends Geometry> geometries) {
    if (geometries == null) {
      return (G)geometryCollection();
    } else {
      DataType dataType = null;
      boolean heterogeneous = false;
      final List<Geometry> geometryList = new ArrayList<>();
      if (geometries != null) {
        for (final Geometry geometry : geometries) {
          if (geometry != null) {
            if (heterogeneous) {
            } else {
              final DataType geometryDataType = geometry.getDataType();
              if (dataType == null) {
                dataType = geometryDataType;
              } else if (dataType != geometryDataType) {
                heterogeneous = true;
                dataType = null;
              }
            }

            final Geometry copy = geometry.newGeometry(this);
            geometryList.add(copy);
          }
        }
      }
      if (geometryList.size() == 0) {
        return (G)geometryCollection();
      } else if (geometryList.size() == 1) {
        return (G)geometryList.get(0);
      } else if (dataType == DataTypes.POINT) {
        return (G)punctual(geometryList);
      } else if (dataType == DataTypes.LINE_STRING) {
        return (G)lineal(geometryList);
      } else if (dataType == DataTypes.POLYGON) {
        return (G)polygonal(geometryList);
      } else {
        final Geometry[] geometryArray = new Geometry[geometryList.size()];
        geometryList.toArray(geometryArray);
        return (G)new GeometryCollectionImpl(this, geometryArray);
      }
    }
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
   * @param geometryFactory The geometry factory to convert to.
   * @return The coordinates operation or null if no conversion is available.
   */
  @Override
  public CoordinatesOperation getCoordinatesOperation(final GeometryFactory geometryFactory) {
    if (geometryFactory == this) {
      return null;
    } else if (geometryFactory == null) {
      return null;
    } else if (!geometryFactory.isHasCoordinateSystem()) {
      return null;
    } else if (!isHasCoordinateSystem()) {
      return null;
    } else {
      if (hasSameCoordinateSystem(geometryFactory)) {
        return null;
      } else {
        final CoordinateSystem coordinateSystem = getCoordinateSystem();
        final CoordinateSystem otherCoordinateSystem = geometryFactory.getCoordinateSystem();
        return ProjectionFactory.getCoordinatesOperation(coordinateSystem, otherCoordinateSystem);
      }
    }
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
      final int coordinateSystemId = geographicCs.getCoordinateSystemId();
      return floating(coordinateSystemId, getAxisCount());
    } else {
      return floating(4326, getAxisCount());
    }
  }

  public List<Geometry> getGeometries(final Collection<? extends Geometry> geometries) {
    final List<Geometry> geometryList = new ArrayList<>();
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
      maxSigDigits = 1 + (int)Math.ceil(Math.log(this.scaleX) / Math.log(10));
    }
    return maxSigDigits;
  }

  public double getOffsetX() {
    return 0;
  }

  public double getOffsetY() {
    return 0;
  }

  public double getOffsetZ() {
    return 0;
  }

  public Point[] getPointArray(final Iterable<?> pointsList) {
    final List<Point> points = new ArrayList<>();
    for (final Object object : pointsList) {
      final Point point = point(object);
      if (point != null && !point.isEmpty()) {
        points.add(point);
      }
    }
    return points.toArray(new Point[points.size()]);
  }

  @SuppressWarnings("unchecked")
  public Polygon[] getPolygonArray(final Iterable<?> polygonList) {
    final List<Polygon> polygons = new ArrayList<>();
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

  public double getResolutionX() {
    return this.resolutionX;
  }

  public double getResolutionXy() {
    return this.resolutionX;
  }

  public double getResolutionY() {
    return this.resolutionY;
  }

  public double getResolutionZ() {
    return this.resolutionZ;
  }

  public double getScale(final int axisIndex) {
    switch (axisIndex) {
      case 0:
        return this.scaleX;
      case 1:
        return this.scaleY;
      case 2:
        return this.scaleZ;
      default:
        if (axisIndex < 0 || axisIndex >= this.scales.length) {
          return 0;
        } else {
          return this.scales[axisIndex - 1];
        }
    }
  }

  public double getScaleX() {
    return this.scaleX;
  }

  public double getScaleXY() {
    return this.scaleX;
  }

  public double getScaleY() {
    return this.scaleY;
  }

  public double getScaleZ() {
    return this.scaleZ;
  }

  @Override
  public int hashCode() {
    return this.coordinateSystemId;
  }

  public boolean hasM() {
    return this.axisCount > 3;
  }

  public boolean hasSameCoordinateSystem(final GeometryFactory geometryFactory) {
    if (geometryFactory == null) {
      return false;
    } else {
      final int coordinateSystemId1 = getCoordinateSystemId();
      final int coordinateSystemId2 = geometryFactory.getCoordinateSystemId();
      if (coordinateSystemId1 == coordinateSystemId2) {
        if (coordinateSystemId1 >= 0) {
          return true;
        }
      }
      final CoordinateSystem coordinateSystem1 = getCoordinateSystem();
      final CoordinateSystem coordinateSystem2 = geometryFactory.getCoordinateSystem();
      if (coordinateSystem1 == coordinateSystem2) {
        return true;
      } else if (coordinateSystem1 == null || coordinateSystem2 == null) {
        return false;
      } else if (coordinateSystem1.equals(coordinateSystem2)) {
        return true;
      } else {
        return false;
      }
    }
  }

  public boolean hasZ() {
    return this.axisCount > 2;
  }

  protected void init(final int axisCount, final double... scales) {
    this.axisCount = Math.max(axisCount, 2);
    this.scales = new double[axisCount];
    for (int axisIndex = 0; axisIndex < axisCount && axisIndex < scales.length; axisIndex++) {
      final double scale = scales[axisIndex];
      this.scales[axisIndex] = scale;
    }

    this.scaleX = this.scales[0];
    this.resolutionX = toResolution(this.scaleX);

    this.scaleY = this.scales[1];
    this.resolutionY = toResolution(this.scaleY);

    if (axisCount > 2) {
      this.scaleZ = this.scales[2];
      this.resolutionZ = toResolution(this.scaleZ);
    }
  }

  public boolean isFloating() {
    return this.scaleX == 0;
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

  public boolean isSameCoordinateSystem(final CoordinateSystem coordinateSystem) {
    final int coordinateSystemId = getCoordinateSystemId();
    if (coordinateSystem == null) {
      return this.coordinateSystem == null;
    } else {
      final int coordinateSystemId2 = coordinateSystem.getCoordinateSystemId();
      if (coordinateSystemId == coordinateSystemId2) {
        return true;
      } else {
        final CoordinateSystem coordinateSystem2 = this.coordinateSystem;
        if (coordinateSystem2 == null) {
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

  @Override
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

  public Lineal lineal(final Geometry geometry) {
    if (geometry instanceof Lineal) {
      final Lineal lineal = (Lineal)geometry;
      return lineal.convertGeometry(this);
    } else if (geometry.isGeometryCollection()) {
      final List<LineString> lines = new ArrayList<>();
      for (final Geometry part : geometry.geometries()) {
        if (part instanceof LineString) {
          lines.add((LineString)part);
        } else {
          throw new IllegalArgumentException(
            "Cannot convert class " + part.getGeometryType() + " to Lineal\n" + geometry);
        }
      }
      return lineal(lines);
    } else {
      throw new IllegalArgumentException(
        "Cannot convert class " + geometry.getGeometryType() + " to Lineal\n" + geometry);
    }
  }

  public Lineal lineal(final Geometry... lines) {
    return lineal(Arrays.asList(lines));
  }

  public Lineal lineal(final int axisCount, final double[]... linesCoordinates) {
    if (linesCoordinates == null) {
      return lineString();
    } else {
      final int lineCount = linesCoordinates.length;
      final LineString[] lines = new LineString[lineCount];
      for (int i = 0; i < lineCount; i++) {
        final double[] coordinates = linesCoordinates[i];
        lines[i] = lineString(axisCount, coordinates);
      }
      return lineal(lines);
    }
  }

  public Lineal lineal(final Iterable<?> lines) {
    if (Property.isEmpty(lines)) {
      return lineString();
    } else {
      final List<LineString> lineStrings = new ArrayList<>();
      for (final Object value : lines) {
        if (value instanceof LineString) {
          final LineString line = (LineString)value;
          lineStrings.add(line.convertGeometry(this));
        } else if (value instanceof Lineal) {
          for (final LineString line : ((Lineal)value).lineStrings()) {
            lineStrings.add(line.convertGeometry(this));
          }
        } else if (value instanceof double[]) {
          final double[] points = (double[])value;
          final int axisCount = getAxisCount();
          final LineString line = lineString(axisCount, points);
          lineStrings.add(line);
        }
      }
      final int lineCount = lineStrings.size();
      if (lineCount == 0) {
        return lineString();
      } else if (lineCount == 1) {
        return lineStrings.get(0);
      } else {
        final LineString[] lineArray = new LineString[lineCount];
        lineStrings.toArray(lineArray);
        return lineal(lineArray);
      }
    }
  }

  /**
   * Creates a MultiLineString using the given LineStrings; a null or empty
   * array will Construct a new empty MultiLineString.
   *
   * @param lineStrings LineStrings, each of which may be empty but not null
   * @return the created MultiLineString
   */
  public Lineal lineal(final LineString... lines) {
    if (lines == null || lines.length == 0) {
      return lineString();
    } else if (lines.length == 1) {
      return lines[0];
    } else {
      return new MultiLineStringImpl(this, lines);
    }
  }

  public LinearRing linearRing() {
    return new LinearRingDoubleGf(this);
  }

  public LinearRing linearRing(final Collection<?> points) {
    if (points.isEmpty()) {
      return linearRing();
    } else {
      final LineStringDoubleBuilder lineBuilder = newLineStringBuilder(points);
      return lineBuilder.newLinearRing();
    }
  }

  public LinearRing linearRing(final int axisCount, double... coordinates) {
    final int vertexCount = coordinates.length / axisCount;
    coordinates = LineStringDoubleGf.getNewCoordinates(this, axisCount, vertexCount, coordinates);
    return new LinearRingDoubleGf(this, this.axisCount, vertexCount, coordinates);
  }

  public LinearRing linearRing(final int axisCount, final int vertexCount, double... coordinates) {
    coordinates = LineStringDoubleGf.getNewCoordinates(this, axisCount, vertexCount, coordinates);
    return new LinearRingDoubleGf(this, this.axisCount, vertexCount, coordinates);
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
  public LinearRing linearRing(final LineString line) {
    if (line == null || line.isEmpty()) {
      return linearRing();
    } else {
      final int vertexCount = line.getVertexCount();
      final double[] coordinates = LineStringDoubleGf.getNewCoordinates(this, line);
      return new LinearRingDoubleGf(this, this.axisCount, vertexCount, coordinates);
    }
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
    if (points == null || points.isEmpty()) {
      return lineString();
    } else {
      final LineStringDoubleBuilder lineBuilder = newLineStringBuilder(points);
      return lineBuilder.newLineString();
    }
  }

  public LineString lineString(final int axisCount, double... coordinates) {
    if (coordinates == null || coordinates.length == 1) {
      return lineString();
    } else if (axisCount < 2) {
      return lineString();
    } else {
      final int vertexCount = coordinates.length / axisCount;
      coordinates = LineStringDoubleGf.getNewCoordinates(this, axisCount, vertexCount, coordinates);
      return new LineStringDoubleGf(this, this.axisCount, vertexCount, coordinates);
    }
  }

  public LineString lineString(final int axisCount, final int vertexCount, double... coordinates) {
    coordinates = LineStringDoubleGf.getNewCoordinates(this, axisCount, vertexCount, coordinates);
    return new LineStringDoubleGf(this, this.axisCount, vertexCount, coordinates);
  }

  public LineString lineString(final int axisCount, final Number[] coordinates) {
    final int vertexCount = coordinates.length / axisCount;
    final double[] coordinatesDouble = LineStringDoubleGf.getNewCoordinates(this, axisCount,
      vertexCount, coordinates);
    return new LineStringDoubleGf(this, this.axisCount, vertexCount, coordinatesDouble);
  }

  public LineString lineString(final LineString line) {
    if (line == null || line.isEmpty()) {
      return lineString();
    } else {
      final int vertexCount = line.getVertexCount();
      final double[] coordinates = LineStringDoubleGf.getNewCoordinates(this, line);
      return new LineStringDoubleGf(this, this.axisCount, vertexCount, coordinates);
    }
  }

  public LineString lineString(final Point... points) {
    if (points == null) {
      return lineString();
    } else {
      final List<Point> linePoints = new ArrayList<>();
      for (final Point point : points) {
        if (point != null && !point.isEmpty()) {
          linePoints.add(point);
        }
      }
      return lineString(linePoints);
    }
  }

  public void makePrecise(final double[] values, final double[] valuesPrecise) {
    for (int i = 0; i < valuesPrecise.length; i++) {
      final int axisIndex = i % this.axisCount;
      valuesPrecise[i] = makePrecise(axisIndex, values[i]);
    }
  }

  public double makePrecise(final int axisIndex, final double value) {
    final double scale = getScale(axisIndex);
    if (scale > 0 && Double.isFinite(value)) {
      final double multiple = value * scale;
      final double scaledValue = Math.round(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  public void makePrecise(final int axisCount, final double... coordinates) {
    for (int i = 0; i < coordinates.length; i++) {
      final double value = coordinates[i];
      final int axisIndex = i % axisCount;
      final double scale = getScale(axisIndex);
      if (scale > 0) {
        final double multiple = value * scale;
        final long scaledValue = Math.round(multiple);
        final double preciseValue = scaledValue / scale;
        coordinates[i] = preciseValue;
      }
    }
  }

  public double makePreciseCeil(final int axisIndex, final double value) {
    final double scale = getScale(axisIndex);
    if (scale > 0) {
      final double multiple = value * scale;
      final long scaledValue = (long)Math.ceil(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  public double makePreciseFloor(final int axisIndex, final double value) {
    final double scale = getScale(axisIndex);
    if (scale > 0) {
      final double multiple = value * scale;
      final long scaledValue = (long)Math.floor(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  public double makeXPrecise(final double value) {
    final double scale = this.scaleX;
    if (scale > 0 && Double.isFinite(value)) {
      final double multiple = value * scale;
      final double scaledValue = Math.round(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  public double makeXPreciseCeil(final double value) {
    final double scale = this.scaleX;
    if (scale > 0) {
      final double multiple = value * scale;
      final long scaledValue = (long)Math.ceil(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  public double makeXPreciseFloor(final double value) {
    final double scale = this.scaleX;
    if (scale > 0) {
      final double multiple = value * scale;
      final long scaledValue = (long)Math.floor(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  public double makeXyPrecise(final double value) {
    final double scale = this.scaleX;
    if (scale > 0 && Double.isFinite(value)) {
      final double multiple = value * scale;
      final double scaledValue = Math.round(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  public double makeXyPreciseCeil(final double value) {
    final double scale = this.scaleX;
    if (scale > 0) {
      final double multiple = value * scale;
      final long scaledValue = (long)Math.ceil(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  public double makeXyPreciseFloor(final double value) {
    final double scale = this.scaleX;
    if (scale > 0) {
      final double multiple = value * scale;
      final long scaledValue = (long)Math.floor(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  public double makeYPrecise(final double value) {
    final double scale = this.scaleY;
    if (scale > 0 && Double.isFinite(value)) {
      final double multiple = value * scale;
      final double scaledValue = Math.round(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  public double makeYPreciseCeil(final double value) {
    final double scale = this.scaleY;
    if (scale > 0) {
      final double multiple = value * scale;
      final long scaledValue = (long)Math.ceil(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  public double makeYPreciseFloor(final double value) {
    final double scale = this.scaleY;
    if (scale > 0) {
      final double multiple = value * scale;
      final long scaledValue = (long)Math.floor(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  public double makeZPrecise(final double value) {
    final double scale = this.scaleZ;
    if (scale > 0 && Double.isFinite(value)) {
      final double multiple = value * scale;
      final double scaledValue = Math.round(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  public double makeZPreciseCeil(final double value) {
    final double scale = this.scaleZ;
    if (scale > 0) {
      final double multiple = value * scale;
      final long scaledValue = (long)Math.ceil(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  public double makeZPreciseFloor(final double value) {
    final double scale = this.scaleZ;
    if (scale > 0) {
      final double multiple = value * scale;
      final long scaledValue = (long)Math.floor(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  public BoundingBox newBoundingBox(final double x, final double y) {
    return new BoundingBoxDoubleXYGeometryFactory(this, x, y);
  }

  public BoundingBox newBoundingBox(final double minX, final double minY, final double maxX,
    final double maxY) {
    return new BoundingBoxDoubleXYGeometryFactory(this, minX, minY, maxX, maxY);
  }

  public BoundingBox newBoundingBox(final int axisCount) {
    return new BoundingBoxDoubleGf(this, axisCount);
  }

  public BoundingBox newBoundingBox(final int axisCount, final double... bounds) {
    if (axisCount == 2) {
      final double x1 = bounds[0];
      final double y1 = bounds[1];
      final double x2 = bounds[2];
      final double y2 = bounds[3];
      return new BoundingBoxDoubleXYGeometryFactory(this, x1, y1, x2, y2);
    } else {
      return new BoundingBoxDoubleGf(this, axisCount, bounds);
    }
  }

  public BoundingBox newBoundingBox(int axisCount, final Iterable<? extends Point> points) {
    axisCount = Math.min(axisCount, getAxisCount());
    double[] bounds = null;
    if (points != null) {
      for (final Point point : points) {
        if (point != null) {
          if (bounds == null) {
            bounds = BoundingBoxUtil.newBounds(this, axisCount, point);
          } else {
            BoundingBoxUtil.expand(this, bounds, point);
          }
        }
      }
    }
    if (bounds == null) {
      return this.boundingBoxEmpty;
    } else {
      return newBoundingBox(axisCount, bounds);
    }
  }

  public BoundingBox newBoundingBox(int axisCount, final Point... points) {
    axisCount = Math.min(axisCount, getAxisCount());
    double[] bounds = null;
    if (points != null) {
      for (final Point point : points) {
        if (point != null) {
          if (bounds == null) {
            bounds = BoundingBoxUtil.newBounds(this, axisCount, point);
          } else {
            BoundingBoxUtil.expand(this, bounds, point);
          }
        }
      }
    }
    if (bounds == null) {
      return this.boundingBoxEmpty;
    } else {
      return newBoundingBox(axisCount, bounds);
    }
  }

  public BoundingBox newBoundingBox(final Iterable<? extends Point> points) {
    double minX = Double.POSITIVE_INFINITY;
    double maxX = Double.NEGATIVE_INFINITY;
    double minY = Double.POSITIVE_INFINITY;
    double maxY = Double.NEGATIVE_INFINITY;

    for (final Point point : points) {
      final double x = point.getX();
      final double y = point.getY();
      if (x < minX) {
        minX = x;
      }
      if (y < minY) {
        minY = y;
      }

      if (x > maxX) {
        maxX = x;
      }
      if (y > maxY) {
        maxY = y;
      }
    }
    final boolean nullX = minX > maxX;
    final boolean nullY = minY > maxY;
    if (nullX) {
      if (nullY) {
        return this.boundingBoxEmpty;
      } else {
        return new BoundingBoxDoubleXYGeometryFactory(this, Double.NEGATIVE_INFINITY, minY,
          Double.POSITIVE_INFINITY, maxY);
      }
    } else {
      if (nullY) {
        return new BoundingBoxDoubleXYGeometryFactory(this, minX, Double.NEGATIVE_INFINITY, maxX,
          Double.POSITIVE_INFINITY);
      } else {
        return new BoundingBoxDoubleXYGeometryFactory(this, minX, minY, maxX, maxY);
      }
    }
  }

  public BoundingBox newBoundingBox(final Point point) {
    final double x = point.getX();
    final double y = point.getY();
    return newBoundingBox(x, y);
  }

  public BoundingBox newBoundingBoxEmpty() {
    return this.boundingBoxEmpty;
  }

  public LineStringDoubleBuilder newLineStringBuilder() {
    return new LineStringDoubleBuilder(this);
  }

  private LineStringDoubleBuilder newLineStringBuilder(final Collection<?> points) {
    final LineStringDoubleBuilder lineBuilder = new LineStringDoubleBuilder(this, points.size());
    for (final Object object : points) {
      if (object == null) {
      } else if (object instanceof Point) {
        final Point point = (Point)object;
        lineBuilder.appendVertex(point);
      } else if (object instanceof double[]) {
        final double[] coordinates = (double[])object;
        lineBuilder.appendVertex(coordinates);
      } else if (object instanceof List<?>) {
        @SuppressWarnings("unchecked")
        final List<Number> list = (List<Number>)object;
        final double[] coordinates = MathUtil.toDoubleArray(list);
        lineBuilder.appendVertex(coordinates);
      } else if (object instanceof LineString) {
        final LineString LineString = (LineString)object;
        final Point point = LineString.getPoint(0);
        lineBuilder.appendVertex(point);
      } else {
        throw new IllegalArgumentException("Unexepected data type: " + object);
      }
    }
    return lineBuilder;
  }

  /**
   * <p>Construct a newn empty {@link Point}.</p>
   *
   * @return The point.
   */
  public Point point() {
    return this.emptyPoint;
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
    } else if (this.axisCount == 2) {
      return new PointDoubleXYGeometryFactory(this, coordinates[0], coordinates[1]);
    } else {
      return new PointDoubleGf(this, coordinates);
    }
  }

  public Point point(final double x, final double y) {
    return new PointDoubleXYGeometryFactory(this, x, y);
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
   *   <li>Instances of {@link Point} using {@link Point#newGeometry(GeometryFactory)}</li>
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
      return point.newGeometry(this);
    } else if (object instanceof double[]) {
      return point((double[])object);
    } else if (object instanceof List<?>) {
      @SuppressWarnings("unchecked")
      final List<Number> list = (List<Number>)object;
      final double[] pointCoordinates = MathUtil.toDoubleArray(list);
      return point(pointCoordinates);
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
    if (point == null || point.isEmpty()) {
      return point();
    } else {
      if (point.isSameCoordinateSystem(this)) {
        final double[] coordinates = point.getCoordinates();
        return point(coordinates);
      } else {
        return point.newGeometry(this);
      }
    }
  }

  public PolygonImpl polygon() {
    return new PolygonImpl(this);
  }

  public Polygon polygon(final Geometry... rings) {
    return polygon(Arrays.asList(rings));
  }

  public Polygon polygon(final int axisCount, final double... ringCoordinates) {
    if (ringCoordinates == null) {
      return polygon();
    } else {
      final LinearRing[] rings = {
        linearRing(axisCount, ringCoordinates)
      };
      return new PolygonImpl(this, rings);
    }
  }

  public Polygon polygon(final int axisCount, final double[]... ringsCoordinates) {
    if (ringsCoordinates == null) {
      return polygon();
    } else {
      final int ringCount = ringsCoordinates.length;
      final LinearRing[] rings = new LinearRing[ringCount];
      for (int i = 0; i < ringCount; i++) {
        final double[] ringCoordinates = ringsCoordinates[i];
        rings[i] = linearRing(axisCount, ringCoordinates);
      }
      return new PolygonImpl(this, rings);
    }
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

  public Polygon polygon(final Polygon polygon) {
    return polygon.newGeometry(this);
  }

  public Polygonal polygonal(final Geometry geometry) {
    if (geometry instanceof Polygonal) {
      final Polygonal polygonal = (Polygonal)geometry.convertGeometry(this);
      return polygonal;
    } else if (geometry.isGeometryCollection()) {
      final List<Polygon> polygons = new ArrayList<>();
      for (final Geometry part : geometry.geometries()) {
        if (part instanceof Polygon) {
          polygons.add((Polygon)part);
        } else {
          throw new IllegalArgumentException(
            "Cannot convert class " + part.getGeometryType() + " to Polygonal\n" + geometry);
        }
      }
      return polygonal(polygons);
    } else {
      throw new IllegalArgumentException(
        "Cannot convert class " + geometry.getGeometryType() + " to Polygonal\n" + geometry);
    }
  }

  public Polygonal polygonal(final Iterable<?> polygons) {
    final Polygon[] polygonArray = getPolygonArray(polygons);
    return polygonal(polygonArray);
  }

  public Polygonal polygonal(final Object... polygons) {
    return polygonal(Arrays.asList(polygons));
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
  public Polygonal polygonal(final Polygon... polygons) {
    if (polygons == null || polygons.length == 0) {
      return polygon();
    } else if (polygons.length == 1) {
      return polygons[0];
    } else {
      return new MultiPolygonImpl(this, polygons);
    }
  }

  /**
   * Project the geometry if it is in a different coordinate system
   *
   * @param geometry
   * @return
   */
  public <G extends Geometry> G project(final G geometry) {
    return geometry.convertGeometry(this);
  }

  public Punctual punctual(final Geometry... points) {
    return punctual(Arrays.asList(points));
  }

  public Punctual punctual(final Geometry geometry) {
    if (geometry instanceof Punctual) {
      final Punctual punctual = (Punctual)geometry.convertGeometry(this);
      return punctual;
    } else if (geometry.isGeometryCollection()) {
      final List<Point> points = new ArrayList<>();
      for (final Geometry part : geometry.geometries()) {
        if (part instanceof Point) {
          points.add((Point)part);
        } else {
          throw new IllegalArgumentException(
            "Cannot convert class " + part.getGeometryType() + " to Punctual\n" + geometry);
        }
      }
      return punctual(points);
    } else {
      throw new IllegalArgumentException(
        "Cannot convert class " + geometry.getGeometryType() + " to Punctual\n" + geometry);
    }
  }

  public Punctual punctual(final int axisCount, final double... coordinates) {
    if (coordinates == null || coordinates.length == 0 || axisCount < 2) {
      return point();
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
      return punctual(points);
    }
  }

  public Punctual punctual(final Iterable<?> points) {
    final Point[] pointArray = getPointArray(points);
    return punctual(pointArray);
  }

  /**
   * Creates a {@link Punctual} using the
   * points in the given {@link LineString}.
   * A <code>null</code> or empty LineString creates an empty {@link Point}.
   *
   * @param coordinates a LineString (possibly empty), or <code>null</code>
   * @return a MultiPoint geometry
   */
  public Punctual punctual(final LineString coordinatesList) {
    if (coordinatesList == null) {
      return punctual();
    } else {
      final Point[] points = new Point[coordinatesList.getVertexCount()];
      for (int i = 0; i < points.length; i++) {
        final Point coordinates = coordinatesList.getPoint(i);
        final Point point = point(coordinates);
        points[i] = point;
      }
      return punctual(points);
    }
  }

  /**
   * Creates a {@link Punctual} using the given {@link Point}s.
   * A null or empty array will Construct a new empty Point.
   *
   * @param coordinates an array (without null elements), or an empty array, or <code>null</code>
   * @return a {@link Punctual} object
   */
  public Punctual punctual(final Point... points) {
    if (points == null || points.length == 0) {
      return point();
    } else if (points.length == 1) {
      return points[0];
    } else {
      return new MultiPointImpl(this, points);
    }
  }

  public GeometryFactory to2dFloating() {
    return fixed(this.coordinateSystem, this.coordinateSystemId, 2, SCALES_FLOATING_2);
  }

  @Override
  public double toDoubleX(final int x) {
    return x / this.scaleX;
  }

  @Override
  public double toDoubleY(final int y) {
    return y / this.scaleY;
  }

  @Override
  public double toDoubleZ(final int z) {
    return z / this.scaleZ;
  }

  @Override
  public int toIntX(final double x) {
    return (int)Math.round(x / this.resolutionX);
  }

  @Override
  public int toIntY(final double y) {
    return (int)Math.round(y / this.resolutionY);
  }

  @Override
  public int toIntZ(final double z) {
    return (int)Math.round(z / this.resolutionZ);
  }

  @Override
  public MapEx toMap() {
    final MapEx map = new LinkedHashMapEx();
    addTypeToMap(map, "geometryFactory");
    map.put("srid", getCoordinateSystemId());
    map.put("axisCount", getAxisCount());

    final double scaleX = getScaleX();
    addToMap(map, "scaleX", scaleX, 0.0);

    final double scaleY = getScaleY();
    addToMap(map, "scaleY", scaleY, 0.0);

    if (this.axisCount > 2) {
      final double scaleZ = getScaleZ();
      addToMap(map, "scaleZ", scaleZ, 0.0);
    }
    return map;
  }

  @Override
  public String toString() {
    final StringBuilder string = new StringBuilder();
    final int coordinateSystemId = getCoordinateSystemId();
    if (this.coordinateSystem != null) {
      string.append(this.coordinateSystem.getCoordinateSystemName());
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
