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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.gis.cs.ProjectedCoordinateSystem;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.cs.esri.EsriCoordinateSystems;
import com.revolsys.gis.cs.projection.CoordinatesOperation;
import com.revolsys.gis.cs.projection.GeometryProjectionUtil;
import com.revolsys.gis.cs.projection.ProjectionFactory;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.PrecisionModelUtil;
import com.revolsys.gis.model.coordinates.SimpleCoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesListFactory;
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
import com.revolsys.jts.util.Assert;
import com.revolsys.util.CollectionUtil;

/**
 * Supplies a set of utility methods for building Geometry objects from lists
 * of Coordinates.
 * <p>
 * Note that the factory constructor methods do <b>not</b> change the input coordinates in any way.
 * In particular, they are not rounded to the supplied <tt>PrecisionModel</tt>.
 * It is assumed that input Coordinates meet the given precision.
 *
 *
 * @version 1.7
 */
public class GeometryFactory implements Serializable,
  CoordinatesPrecisionModel, MapSerializer {
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
    final int numAxis = CollectionUtil.getInteger(properties, "numAxis", 2);
    final double scaleXY = CollectionUtil.getDouble(properties, "scaleXy", 0.0);
    final double scaleZ = CollectionUtil.getDouble(properties, "scaleZ", 0.0);
    return GeometryFactory.getFactory(srid, numAxis, scaleXY, scaleZ);
  }

  public static Point createPointFromInternalCoord(final Coordinates coord,
    final Geometry exemplar) {
    exemplar.getPrecisionModel().makePrecise(coord);
    return exemplar.getGeometryFactory().point(coord);
  }

  private static CoordinateSequenceFactory getDefaultCoordinateSequenceFactory() {
    return new DoubleCoordinatesListFactory();
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
    final CoordinateSystem coordinateSystem, final int numAxis,
    final double scaleXY, final double scaleZ) {
    return new GeometryFactory(coordinateSystem, numAxis, scaleXY, scaleZ);
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
  public static GeometryFactory getFactory(final int srid, final double scaleXY) {
    return getFactory(srid, 2, scaleXY, 0);
  }

  /**
   * <p>
   * Get a GeometryFactory with no coordinate system, 3D axis (x, y &amp; z) and
   * a fixed x, y &amp; floating z precision models.
   * </p>
   * 
   * @param srid The <a href="http://spatialreference.org/ref/epsg/">EPSG
   *          coordinate system id</a>.
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
  public static GeometryFactory getFactory(final int srid,
    final double scaleXY, final double scaleZ) {
    return getFactory(srid, 3, scaleXY, scaleZ);
  }

  /**
   * <p>
   * Get a GeometryFactory with the coordinate system, number of axis and a
   * floating precision model.
   * </p>
   * 
   * @param srid The <a href="http://spatialreference.org/ref/epsg/">EPSG
   *          coordinate system id</a>.
   * @param numAxis The number of coordinate axis. 2 for 2D x &amp; y
   *          coordinates. 3 for 3D x, y &amp; z coordinates.
   * @return The geometry factory.
   */
  public static GeometryFactory getFactory(final int srid, final int numAxis) {
    return getFactory(srid, numAxis, 0, 0);
  }

  /**
   * <p>
   * Get a GeometryFactory with the coordinate system, number of axis and a
   * fixed x, y &amp; fixed z precision models.
   * </p>
   * 
   * @param srid The <a href="http://spatialreference.org/ref/epsg/">EPSG
   *          coordinate system id</a>.
   * @param numAxis The number of coordinate axis. 2 for 2D x &amp; y
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

  /**
   *  Converts the <code>List</code> to an array.
   *
   *@param  geometries  the list of <code>Geometry's</code> to convert
   *@return            the <code>List</code> in array format
   */
  public static Geometry[] toGeometryArray(final Collection geometries) {
    if (geometries == null) {
      return null;
    }
    final Geometry[] geometryArray = new Geometry[geometries.size()];
    return (Geometry[])geometries.toArray(geometryArray);
  }

  /**
   *  Converts the <code>List</code> to an array.
   *
   *@param  linearRings  the <code>List</code> of LinearRings to convert
   *@return              the <code>List</code> in array format
   */
  public static LinearRing[] toLinearRingArray(final Collection linearRings) {
    final LinearRing[] linearRingArray = new LinearRing[linearRings.size()];
    return (LinearRing[])linearRings.toArray(linearRingArray);
  }

  /**
   *  Converts the <code>List</code> to an array.
   *
   *@param  lineStrings  the <code>List</code> of LineStrings to convert
   *@return              the <code>List</code> in array format
   */
  public static LineString[] toLineStringArray(final Collection lineStrings) {
    final LineString[] lineStringArray = new LineString[lineStrings.size()];
    return (LineString[])lineStrings.toArray(lineStringArray);
  }

  /**
   *  Converts the <code>List</code> to an array.
   *
   *@param  multiLineStrings  the <code>List</code> of MultiLineStrings to convert
   *@return                   the <code>List</code> in array format
   */
  public static MultiLineString[] toMultiLineStringArray(
    final Collection multiLineStrings) {
    final MultiLineString[] multiLineStringArray = new MultiLineString[multiLineStrings.size()];
    return (MultiLineString[])multiLineStrings.toArray(multiLineStringArray);
  }

  /**
   *  Converts the <code>List</code> to an array.
   *
   *@param  multiPoints  the <code>List</code> of MultiPoints to convert
   *@return              the <code>List</code> in array format
   */
  public static MultiPoint[] toMultiPointArray(final Collection multiPoints) {
    final MultiPoint[] multiPointArray = new MultiPoint[multiPoints.size()];
    return (MultiPoint[])multiPoints.toArray(multiPointArray);
  }

  /**
   *  Converts the <code>List</code> to an array.
   *
   *@param  multiPolygons  the <code>List</code> of MultiPolygons to convert
   *@return                the <code>List</code> in array format
   */
  public static MultiPolygon[] toMultiPolygonArray(
    final Collection multiPolygons) {
    final MultiPolygon[] multiPolygonArray = new MultiPolygon[multiPolygons.size()];
    return (MultiPolygon[])multiPolygons.toArray(multiPolygonArray);
  }

  /**
   *  Converts the <code>List</code> to an array.
   *
   *@param  points  the <code>List</code> of Points to convert
   *@return         the <code>List</code> in array format
   */
  public static Point[] toPointArray(final Collection points) {
    final Point[] pointArray = new Point[points.size()];
    return (Point[])points.toArray(pointArray);
  }

  /**
   *  Converts the <code>List</code> to an array.
   *
   *@param  polygons  the <code>List</code> of Polygons to convert
   *@return           the <code>List</code> in array format
   */
  public static Polygon[] toPolygonArray(final Collection polygons) {
    final Polygon[] polygonArray = new Polygon[polygons.size()];
    return (Polygon[])polygons.toArray(polygonArray);
  }

  public static GeometryFactory wgs84() {
    return getFactory(4326);
  }

  public static GeometryFactory worldMercator() {
    return getFactory(3857);
  }

  private final PrecisionModel precisionModel;

  private final CoordinatesPrecisionModel coordinatesPrecisionModel;

  private final CoordinateSystem coordinateSystem;

  private int numAxis = 2;

  private CoordinateSequenceFactory coordinateSequenceFactory;

  private final int srid;

  private final WktParser parser = new WktParser(this);

  protected GeometryFactory(final CoordinateSystem coordinateSystem,
    final int numAxis, final double scaleXY, final double scaleZ) {
    this.precisionModel = PrecisionModelUtil.getPrecisionModel(scaleXY);
    this.coordinateSequenceFactory = new DoubleCoordinatesListFactory();
    this.srid = coordinateSystem.getId();

    this.coordinateSystem = coordinateSystem;
    this.coordinatesPrecisionModel = new SimpleCoordinatesPrecisionModel(
      scaleXY, scaleZ);
    this.numAxis = Math.max(numAxis, 2);
  }

  protected GeometryFactory(final int srid, final int numAxis,
    final double scaleXY, final double scaleZ) {
    this.precisionModel = PrecisionModelUtil.getPrecisionModel(scaleXY);
    this.coordinateSequenceFactory = new DoubleCoordinatesListFactory();
    this.srid = srid;

    this.coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(srid);
    this.coordinatesPrecisionModel = new SimpleCoordinatesPrecisionModel(
      scaleXY, scaleZ);
    this.numAxis = Math.max(numAxis, 2);
  }

  /**
   * Constructs a GeometryFactory that generates Geometries having the given
   * {@link PrecisionModel} and the default CoordinatesList
   * implementation.
   *
   * @param precisionModel the PrecisionModel to use
   */
  @Deprecated
  public GeometryFactory(final PrecisionModel precisionModel) {
    this(precisionModel, 0, getDefaultCoordinateSequenceFactory());
  }

  /**
   * Constructs a GeometryFactory that generates Geometries having the given
   * {@link PrecisionModel} and spatial-reference ID, and the default CoordinatesList
   * implementation.
   *
   * @param precisionModel the PrecisionModel to use
   * @param srid the srid to use
   */
  @Deprecated
  public GeometryFactory(final PrecisionModel precisionModel, final int SRID) {
    this(precisionModel, SRID, getDefaultCoordinateSequenceFactory());
  }

  /**
   * Constructs a GeometryFactory that generates Geometries having the given
   * PrecisionModel, spatial-reference ID, and CoordinatesList implementation.
   */
  @Deprecated
  public GeometryFactory(final PrecisionModel precisionModel, final int srid,
    final CoordinateSequenceFactory coordinateSequenceFactory) {
    this(srid, 3, precisionModel.getScale(), precisionModel.getScale());
    this.coordinateSequenceFactory = coordinateSequenceFactory;
  }

  public void addGeometries(final List<Geometry> geometryList,
    final Geometry geometry) {
    if (geometry != null && !geometry.isEmpty()) {
      for (int i = 0; i < geometry.getNumGeometries(); i++) {
        final Geometry part = geometry.getGeometry(i);
        if (part != null && !part.isEmpty()) {
          geometryList.add(copy(part));
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
   *@param  geomList  the <code>Geometry</code>s to combine
   *@return           a <code>Geometry</code> of the "smallest", "most
   *      type-specific" class that can contain the elements of <code>geomList</code>
   *      .
   */
  public Geometry buildGeometry(final Collection geomList) {

    /**
     * Determine some facts about the geometries in the list
     */
    Class geomClass = null;
    boolean isHeterogeneous = false;
    boolean hasGeometryCollection = false;
    for (final Iterator i = geomList.iterator(); i.hasNext();) {
      final Geometry geom = (Geometry)i.next();
      final Class partClass = geom.getClass();
      if (geomClass == null) {
        geomClass = partClass;
      }
      if (partClass != geomClass) {
        isHeterogeneous = true;
      }
      if (geom instanceof GeometryCollection) {
        hasGeometryCollection = true;
      }
    }

    /**
     * Now construct an appropriate geometry to return
     */
    // for the empty geometry, return an empty GeometryCollection
    if (geomClass == null) {
      return createGeometryCollection();
    }
    if (isHeterogeneous || hasGeometryCollection) {
      return createGeometryCollection(toGeometryArray(geomList));
    }
    // at this point we know the collection is hetereogenous.
    // Determine the type of the result from the first Geometry in the list
    // this should always return a geometry, since otherwise an empty collection
    // would have already been returned
    final Geometry geom0 = (Geometry)geomList.iterator().next();
    final boolean isCollection = geomList.size() > 1;
    if (isCollection) {
      if (geom0 instanceof Polygon) {
        return createMultiPolygon(toPolygonArray(geomList));
      } else if (geom0 instanceof LineString) {
        return createMultiLineString(toLineStringArray(geomList));
      } else if (geom0 instanceof Point) {
        return createMultiPoint(toPointArray(geomList));
      }
      Assert.shouldNeverReachHere("Unhandled class: "
        + geom0.getClass().getName());
    }
    return geom0;
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
          point = projectedPoint;
        } else if (object instanceof double[]) {
          point = new DoubleCoordinates((double[])object);
        } else if (object instanceof CoordinatesList) {
          final CoordinatesList coordinates = (CoordinatesList)object;
          point = coordinates.get(0);
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
    return point((Coordinates)null);
  }

  public GeometryCollection createEmptyGeometryCollection() {
    return new GeometryCollectionImpl(null, this);
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
  public <V extends Geometry> V createGeometry(final Class<?> targetClass,
    Geometry geometry) {
    if (geometry != null && !geometry.isEmpty()) {
      geometry = copy(geometry);
      if (geometry instanceof GeometryCollection) {
        if (geometry.getNumGeometries() == 1) {
          geometry = geometry.getGeometry(0);
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
            return (V)geometry.getGeometry(0);
          }
        }
      } else if (LineString.class.isAssignableFrom(targetClass)) {
        if (geometry instanceof MultiLineString) {
          if (geometry.getNumGeometries() == 1) {
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
          if (geometry.getNumGeometries() == 1) {
            return (V)geometry.getGeometry(0);
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
        final Geometry[] geometryArray = com.revolsys.jts.geom.GeometryFactory.toGeometryArray(geometries);
        return (V)createGeometryCollection(geometryArray);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public <V extends Geometry> V createGeometry(final Geometry... geometries) {
    return (V)createGeometry(Arrays.asList(geometries));
  }

  /**
   * Creates a deep copy of the input {@link Geometry}.
   * The {@link CoordinateSequenceFactory} defined for this factory
   * is used to copy the {@link CoordinatesList}s
   * of the input geometry.
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
  public Geometry createGeometry(final Geometry geometry) {
    if (geometry == null) {
      return null;
    } else {
      final int srid = getSrid();
      final int geometrySrid = geometry.getSrid();
      if (srid == 0 && geometrySrid != 0) {
        final GeometryFactory geometryFactory = GeometryFactory.getFactory(
          geometrySrid, numAxis, getScaleXY(), getScaleZ());
        return geometryFactory.createGeometry(geometry);
      } else if (srid != 0 && geometrySrid != 0 && geometrySrid != srid) {
        if (geometry instanceof MultiPoint) {
          final List<Geometry> geometries = new ArrayList<Geometry>();
          addGeometries(geometries, geometry);
          return createMultiPoint(geometries);
        } else if (geometry instanceof MultiLineString) {
          final List<Geometry> geometries = new ArrayList<Geometry>();
          addGeometries(geometries, geometry);
          return createMultiLineString(geometries);
        } else if (geometry instanceof MultiPolygon) {
          final List<Geometry> geometries = new ArrayList<Geometry>();
          addGeometries(geometries, geometry);
          return createMultiPolygon(geometries);
        } else if (geometry instanceof GeometryCollection) {
          final List<Geometry> geometries = new ArrayList<Geometry>();
          addGeometries(geometries, geometry);
          return createGeometryCollection(geometries);
        } else {
          return GeometryProjectionUtil.performCopy(geometry, this);
        }
      } else if (geometry instanceof MultiPoint) {
        final List<Geometry> geometries = new ArrayList<Geometry>();
        addGeometries(geometries, geometry);
        return createMultiPoint(geometries);
      } else if (geometry instanceof MultiLineString) {
        final List<Geometry> geometries = new ArrayList<Geometry>();
        addGeometries(geometries, geometry);
        return createMultiLineString(geometries);
      } else if (geometry instanceof MultiPolygon) {
        final List<Geometry> geometries = new ArrayList<Geometry>();
        addGeometries(geometries, geometry);
        return createMultiPolygon(geometries);
      } else if (geometry instanceof GeometryCollection) {
        final List<Geometry> geometries = new ArrayList<Geometry>();
        addGeometries(geometries, geometry);
        return createGeometryCollection(geometries);
      } else if (geometry instanceof Point) {
        final Point point = (Point)geometry;
        return point.copy(this);
      } else if (geometry instanceof LinearRing) {
        final LinearRing linearRing = (LinearRing)geometry;
        return createLinearRing(linearRing);
      } else if (geometry instanceof LineString) {
        final LineString lineString = (LineString)geometry;
        return lineString(lineString);
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
    return (T)parser.parseGeometry(wkt);
  }

  @SuppressWarnings("unchecked")
  public <T extends Geometry> T createGeometry(final String wkt,
    final boolean useNumAxisFromGeometryFactory) {
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
        return (V)createGeometryCollection(geometryArray);
      }
    }
  }

  /**
   * Creates a GeometryCollection using the given Geometries; a null or empty
   * array will create an empty GeometryCollection.
   * 
   * @param geometries an array of Geometries, each of which may be empty but not null, or null
   * @return the created GeometryCollection
   */
  public GeometryCollection createGeometryCollection(
    final Geometry... geometries) {
    return new GeometryCollectionImpl(geometries, this);
  }

  public LinearRing createLinearRing(final Collection<?> points) {
    final CoordinatesList coordinatesList = createCoordinatesList(points);
    return createLinearRing(coordinatesList);
  }

  /**
   * Creates a {@link LinearRing} using the given {@link Coordinates}s.
   * A null or empty array creates an empty LinearRing. 
   * The points must form a closed and simple linestring. 
   * @param coordinates an array without null elements, or an empty array, or null
   * @return the created LinearRing
   * @throws IllegalArgumentException if the ring is not closed, or has too few points
   */
  public LinearRing createLinearRing(final Coordinates[] coordinates) {
    return createLinearRing(coordinates != null ? getCoordinateSequenceFactory().create(
      coordinates)
      : null);
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
  public LinearRing createLinearRing(final CoordinatesList points) {
    final CoordinatesList coordinatesList = createCoordinatesList(points);
    return new LinearRingImpl(coordinatesList, this);
  }

  public LinearRing createLinearRing(final double... coordinates) {
    final int numAxis = getNumAxis();
    final DoubleCoordinatesList points = new DoubleCoordinatesList(numAxis,
      coordinates);
    return createLinearRing(points);
  }

  public LinearRing createLinearRing(final LinearRing linearRing) {
    final CoordinatesList points = CoordinatesListUtil.get(linearRing);
    final CoordinatesList newPoints = createCoordinatesList(points);
    return createLinearRing(newPoints);
  }

  public LineString lineString(final Collection<?> points) {
    final CoordinatesList coordinatesList = createCoordinatesList(points);
    return lineString(coordinatesList);
  }

  public LineString lineString(final Coordinates... points) {
    if (points == null) {
      return lineString();
    } else {
      final List<Coordinates> p = Arrays.asList(points);
      return lineString(p);
    }
  }

  /**
   * Creates a LineString using the given CoordinatesList.
   * A null or empty CoordinatesList creates an empty LineString. 
   * 
   * @param coordinates a CoordinatesList (possibly empty), or null
   */
  public LineString lineString(final CoordinatesList points) {
    final CoordinatesList newPoints = createCoordinatesList(points);
    final LineString line = new LineStringImpl(newPoints, this);
    return line;
  }

  public LineString lineString(final double... coordinates) {
    final int numAxis = getNumAxis();
    final DoubleCoordinatesList points = new DoubleCoordinatesList(numAxis,
      coordinates);
    return lineString(points);
  }

  public LineString lineString(final LineString lineString) {
    final CoordinatesList points = CoordinatesListUtil.get(lineString);
    final CoordinatesList newPoints = createCoordinatesList(points);
    return lineString(newPoints);
  }

  public MultiLineString createMultiLineString(final Collection<?> lines) {
    final LineString[] lineArray = getLineStringArray(lines);
    return createMultiLineString(lineArray);
  }

  /**
   * Creates a MultiLineString using the given LineStrings; a null or empty
   * array will create an empty MultiLineString.
   * 
   * @param lineStrings LineStrings, each of which may be empty but not null
   * @return the created MultiLineString
   */
  public MultiLineString createMultiLineString(final LineString[] lineStrings) {
    return new MultiLineStringImpl(lineStrings, this);
  }

  public MultiLineString createMultiLineString(final Object... lines) {
    return createMultiLineString(Arrays.asList(lines));
  }

  public MultiPoint createMultiPoint() {
    return new MultiPointImpl(this);
  }

  public MultiPoint createMultiPoint(final Collection<?> points) {
    final Point[] pointArray = getPointArray(points);
    return createMultiPoint(pointArray);
  }

  /**
   * Creates a {@link MultiPoint} using the given {@link Coordinates}s.
   * A null or empty array will create an empty MultiPoint.
   *
   * @param coordinates an array (without null elements), or an empty array, or <code>null</code>
   * @return a MultiPoint object
   */
  public MultiPoint createMultiPoint(final Coordinates[] coordinates) {
    return createMultiPoint(coordinates != null ? getCoordinateSequenceFactory().create(
      coordinates)
      : null);
  }

  /**
   * Creates a {@link MultiPoint} using the 
   * points in the given {@link CoordinatesList}.
   * A <code>null</code> or empty CoordinatesList creates an empty MultiPoint.
   *
   * @param coordinates a CoordinatesList (possibly empty), or <code>null</code>
   * @return a MultiPoint geometry
   */
  public MultiPoint createMultiPoint(final CoordinatesList coordinatesList) {
    if (coordinatesList == null) {
      return createMultiPoint();
    } else {
      final Point[] points = new Point[coordinatesList.size()];
      for (int i = 0; i < points.length; i++) {
        final Coordinates coordinates = coordinatesList.get(i);
        final Point point = point(coordinates);
        points[i] = point;
      }
      return createMultiPoint(points);
    }
  }

  public MultiPoint createMultiPoint(final Object... points) {
    return createMultiPoint(Arrays.asList(points));
  }

  /**
   * Creates a {@link MultiPoint} using the given {@link Point}s.
   * A null or empty array will create an empty MultiPoint.
   *
   * @param point an array of Points (without null elements), or an empty array, or <code>null</code>
   * @return a MultiPoint object
   */
  public MultiPoint createMultiPoint(final Point[] point) {
    return new MultiPointImpl(this, point);
  }

  public MultiPolygon createMultiPolygon(final Collection<?> polygons) {
    final Polygon[] polygonArray = getPolygonArray(polygons);
    return createMultiPolygon(polygonArray);
  }

  public MultiPolygon createMultiPolygon(final Object... polygons) {
    return createMultiPolygon(Arrays.asList(polygons));
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
  public MultiPolygon createMultiPolygon(final Polygon[] polygons) {
    return new MultiPolygonImpl(polygons, this);
  }

  public List<Point> createPointList(final CoordinatesList sourcePoints) {
    final List<Point> points = new ArrayList<Point>(sourcePoints.size());
    for (final Coordinates coordinates : sourcePoints) {
      final Point point = point(coordinates);
      points.add(point);
    }
    return points;
  }

  public Polygon createPolygon() {
    final DoubleCoordinatesList points = new DoubleCoordinatesList(0,
      getNumAxis());
    return createPolygon(points);
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
  public Polygon createPolygon(final Coordinates[] coordinates) {
    return createPolygon(createLinearRing(coordinates));
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
  public Polygon createPolygon(final CoordinatesList coordinates) {
    return createPolygon(createLinearRing(coordinates));
  }

  public Polygon createPolygon(final CoordinatesList... rings) {
    final List<CoordinatesList> ringList = Arrays.asList(rings);
    return createPolygon(ringList);
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
  public Polygon createPolygon(final LinearRing shell) {
    return createPolygon(shell, null);
  }

  /**
   * Constructs a <code>Polygon</code> with the given exterior boundary and
   * interior boundaries.
   *
   * @param shell
   *            the outer boundary of the new <code>Polygon</code>, or
   *            <code>null</code> or an empty <code>LinearRing</code> if
   *            the empty geometry is to be created.
   * @param holes
   *            the inner boundaries of the new <code>Polygon</code>, or
   *            <code>null</code> or empty <code>LinearRing</code> s if
   *            the empty geometry is to be created.
   * @throws IllegalArgumentException if a ring is invalid
   */
  public Polygon createPolygon(final LinearRing shell, final LinearRing[] holes) {
    return new PolygonImpl(shell, holes, this);
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
    final LinearRing exteriorRing = polygon.getExteriorRing();
    final LinearRing newExteriorRing = createLinearRing(exteriorRing);
    rings.add(newExteriorRing);
    for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
      final LinearRing interiorRing = polygon.getInteriorRingN(i);
      final LinearRing newInteriorRing = createLinearRing(interiorRing);
      rings.add(newInteriorRing);

    }
    return createPolygon(rings);
  }

  public Coordinates getCoordinates(final Point point) {
    final Point convertedPoint = project(point);
    return CoordinatesUtil.getInstance(convertedPoint);
  }

  public CoordinateSequenceFactory getCoordinateSequenceFactory() {
    return coordinateSequenceFactory;
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
        lineString = lineString(coordinates);
      } else if (value instanceof double[]) {
        final double[] points = (double[])value;
        lineString = lineString(points);
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

  /**
   * Returns the PrecisionModel that Geometries created by this factory
   * will be associated with.
   * 
   * @return the PrecisionModel for this factory
   */
  public PrecisionModel getPrecisionModel() {
    return precisionModel;
  }

  @Override
  public double getResolutionXy() {
    return coordinatesPrecisionModel.getResolutionXy();
  }

  @Override
  public double getResolutionZ() {
    return coordinatesPrecisionModel.getResolutionZ();
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

  /**
   * Gets the srid value defined for this factory.
   * 
   * @return the factory srid value
   */
  public int getSrid() {
    return srid;
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

  public LineString lineString() {
    final DoubleCoordinatesList points = new DoubleCoordinatesList(0,
      getNumAxis());
    return lineString(points);
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
  public void makePrecise(final int numAxis, final double... coordinates) {
    coordinatesPrecisionModel.makePrecise(numAxis, coordinates);
  }

  @Override
  public double makeXyPrecise(final double value) {
    return coordinatesPrecisionModel.makeXyPrecise(value);
  }

  @Override
  public double makeZPrecise(final double value) {
    return coordinatesPrecisionModel.makeZPrecise(value);
  }

  public Point point() {
    return new PointImpl(this);
  }

  public Point point(final Coordinates point) {
    if (point == null) {
      return point();
    } else {
      return point(point.getCoordinates());
    }
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
        final int numAxis = Math.min(points.getDimension(), getNumAxis());
        final double[] coordinates = new double[numAxis];
        for (int i = 0; i < numAxis; i++) {
          final double coordinate = points.getOrdinate(0, i);
          coordinates[i] = coordinate;
        }
        return point(coordinates);
      } else {
        throw new IllegalArgumentException("Point can only have 1 vertex not "
          + size);
      }
    }
  }

  public Point point(final double... coordinates) {
    if (coordinates == null || coordinates.length < 2) {
      return point();
    } else {
      return new PointImpl(this, coordinates);
    }
  }

  public Point point(final Object object) {
    if (object == null) {
      return point();
    } else if (object instanceof Point) {
      final Point point = (Point)object;
      return point.copy(this);
    } else if (object instanceof double[]) {
      return point((double[])object);
    } else if (object instanceof Coordinates) {
      return point((Coordinates)object);
    } else if (object instanceof CoordinatesList) {
      return point((CoordinatesList)object);
    } else {
      throw new IllegalArgumentException("Cannot create a point from "
        + object.getClass());
    }
  }

  /**
   * Project the geometry if it is in a different coordinate system
   * 
   * @param geometry
   * @return
   */
  public <G extends Geometry> G project(final G geometry) {
    return GeometryProjectionUtil.perform(geometry, this);
  }

  /**
   * Creates a {@link Geometry} with the same extent as the given envelope.
   * The Geometry returned is guaranteed to be valid.  
   * To provide this behaviour, the following cases occur:
   * <p>
   * If the <code>Envelope</code> is:
   * <ul>
   * <li>null : returns an empty {@link Point}
   * <li>a point : returns a non-empty {@link Point}
   * <li>a line : returns a two-point {@link LineString}
   * <li>a rectangle : returns a {@link Polygon}> whose points are (minx, miny),
   *  (minx, maxy), (maxx, maxy), (maxx, miny), (minx, miny).
   * </ul>
   * 
   *@param  envelope the <code>Envelope</code> to convert
   *@return an empty <code>Point</code> (for null <code>Envelope</code>s), 
   *	a <code>Point</code> (when min x = max x and min y = max y) or a
   *      <code>Polygon</code> (in all other cases)
   */
  public Geometry toGeometry(final Envelope envelope) {
    // null envelope - return empty point geometry
    if (envelope.isNull()) {
      return point((CoordinatesList)null);
    }

    // point?
    if (envelope.getMinX() == envelope.getMaxX()
      && envelope.getMinY() == envelope.getMaxY()) {
      return point(new Coordinate(envelope.getMinX(), envelope.getMinY(),
        Coordinates.NULL_ORDINATE));
    }

    // vertical or horizontal line?
    if (envelope.getMinX() == envelope.getMaxX()
      || envelope.getMinY() == envelope.getMaxY()) {
      return lineString(new Coordinates[] {
        new Coordinate(envelope.getMinX(), envelope.getMinY(),
          Coordinates.NULL_ORDINATE),
        new Coordinate(envelope.getMaxX(), envelope.getMaxY(),
          Coordinates.NULL_ORDINATE)
      });
    }

    // create a CW ring for the polygon
    return createPolygon(
      createLinearRing(new Coordinates[] {
        new Coordinate(envelope.getMinX(), envelope.getMinY(),
          Coordinates.NULL_ORDINATE),
        new Coordinate(envelope.getMinX(), envelope.getMaxY(),
          Coordinates.NULL_ORDINATE),
        new Coordinate(envelope.getMaxX(), envelope.getMaxY(),
          Coordinates.NULL_ORDINATE),
        new Coordinate(envelope.getMaxX(), envelope.getMinY(),
          Coordinates.NULL_ORDINATE),
        new Coordinate(envelope.getMinX(), envelope.getMinY(),
          Coordinates.NULL_ORDINATE)
      }), null);
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("type", "geometryFactory");
    map.put("srid", getSrid());
    map.put("numAxis", getNumAxis());

    final double scaleXY = getScaleXY();
    if (scaleXY > 0) {
      map.put("scaleXy", scaleXY);
    }
    if (numAxis > 2) {
      final double scaleZ = getScaleZ();
      if (scaleZ > 0) {
        map.put("scaleZ", scaleZ);
      }
    }
    return map;
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
