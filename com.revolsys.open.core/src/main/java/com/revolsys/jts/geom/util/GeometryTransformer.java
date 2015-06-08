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

package com.revolsys.jts.geom.util;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;

/**
 * A framework for processes which transform an input {@link Geometry} into
 * an output {@link Geometry}, possibly changing its structure and type(s).
 * This class is a framework for implementing subclasses
 * which perform transformations on
 * various different Geometry subclasses.
 * It provides an easy way of applying specific transformations
 * to given geometry types, while allowing unhandled types to be simply copied.
 * Also, the framework ensures that if subcomponents change type
 * the parent geometries types change appropriately to maintain valid structure.
 * Subclasses will override whichever <code>transformX</code> methods
 * they need to to handle particular Geometry types.
 * <p>
 * A typically usage would be a transformation class that transforms <tt>Polygons</tt> into
 * <tt>Polygons</tt>, <tt>LineStrings</tt> or <tt>Points</tt>, depending on the geometry of the input
 * (For instance, a simplification operation).
 * This class would likely need to override the {@link #transformMultiPolygon(MultiPolygon, Geometry)transformMultiPolygon}
 * method to ensure that if input Polygons change type the result is a <tt>GeometryCollection</tt>,
 * not a <tt>MultiPolygon</tt>.
 * <p>
 * The default behaviour of this class is simply to recursively transform
 * each Geometry component into an identical object by deep copying down
 * to the level of, but not including, coordinates.
 * <p>
 * All <code>transformX</code> methods may return <code>null</code>,
 * to avoid creating empty or invalid geometry objects. This will be handled correctly
 * by the transformer.   <code>transform<i>XXX</i></code> methods should always return valid
 * geometry - if they cannot do this they should return <code>null</code>
 * (for instance, it may not be possible for a transformLineString implementation
 * to return at least two points - in this case, it should return <code>null</code>).
 * The {@link #transform(Geometry)transform} method itself will always
 * return a non-null Geometry object (but this may be empty).
 *
 * @version 1.7
 *
 */
public abstract class GeometryTransformer {

  /**
   * Possible extensions:
   * getParent() method to return immediate parent e.g. of LinearRings in Polygons
   */

  private Geometry inputGeom;

  protected GeometryFactory factory = null;

  // these could eventually be exposed to clients
  /**
   * <code>true</code> if empty geometries should not be included in the result
   */
  private final boolean pruneEmptyGeometry = true;

  /**
   * <code>true</code> if a homogenous collection result
   * from a {@link GeometryCollection} should still
   * be a general GeometryCollection
   */
  private final boolean preserveGeometryCollectionType = true;

  /**
   * <code>true</code> if the type of the input should be preserved
   */
  private final boolean preserveType = false;

  public GeometryTransformer() {
  }

  /**
   * Convenience method which provides statndard way of copying {@link LineString}s
   * @param seq the sequence to copy
   * @return a deep copy of the sequence
   */
  protected final LineString copy(final LineString seq) {
    return seq.clone();
  }

  /**
   * Utility function to make input geometry available
   *
   * @return the input geometry
   */
  public Geometry getInputGeometry() {
    return this.inputGeom;
  }

  public final Geometry transform(final Geometry inputGeom) {
    this.inputGeom = inputGeom;
    this.factory = inputGeom.getGeometryFactory();

    if (inputGeom instanceof Point) {
      return transformPoint((Point)inputGeom, null);
    }
    if (inputGeom instanceof MultiPoint) {
      return transformMultiPoint((MultiPoint)inputGeom, null);
    }
    if (inputGeom instanceof LinearRing) {
      return transformLinearRing((LinearRing)inputGeom, null);
    }
    if (inputGeom instanceof LineString) {
      return transformLineString((LineString)inputGeom, null);
    }
    if (inputGeom instanceof MultiLineString) {
      return transformMultiLineString((MultiLineString)inputGeom, null);
    }
    if (inputGeom instanceof Polygon) {
      return transformPolygon((Polygon)inputGeom, null);
    }
    if (inputGeom instanceof MultiPolygon) {
      return transformMultiPolygon((MultiPolygon)inputGeom, null);
    }
    if (inputGeom instanceof GeometryCollection) {
      return transformGeometryCollection((GeometryCollection)inputGeom, null);
    }

    throw new IllegalArgumentException("Unknown Geometry subtype: "
      + inputGeom.getClass().getName());
  }

  /**
   * Transforms a {@link LineString}.
   * This method should always return a valid coordinate list for
   * the desired result type.  (E.g. a coordinate list for a LineString
   * must have 0 or at least 2 points).
   * If this is not possible, return an empty sequence -
   * this will be pruned out.
   *
   * @param coords the coordinates to transform
   * @param parent the parent geometry
   * @return the transformed coordinates
   */
  protected LineString transformCoordinates(final LineString coords, final Geometry parent) {
    return copy(coords);
  }

  protected Geometry transformGeometryCollection(final GeometryCollection geom,
    final Geometry parent) {
    final List<Geometry> transGeomList = new ArrayList<>();
    for (int i = 0; i < geom.getGeometryCount(); i++) {
      final Geometry transformGeom = transform(geom.getGeometry(i));
      if (transformGeom == null) {
        continue;
      }
      if (this.pruneEmptyGeometry && transformGeom.isEmpty()) {
        continue;
      }
      transGeomList.add(transformGeom);
    }
    if (this.preserveGeometryCollectionType) {
      return this.factory.geometryCollection(transGeomList);
    }
    return this.factory.buildGeometry(transGeomList);
  }

  /**
   * Transforms a LinearRing.
   * The transformation of a LinearRing may result in a coordinate sequence
   * which does not form a structurally valid ring (i.e. a degnerate ring of 3 or fewer points).
   * In this case a LineString is returned.
   * Subclasses may wish to override this method and check for this situation
   * (e.g. a subclass may choose to eliminate degenerate linear rings)
   *
   * @param geom the ring to simplify
   * @param parent the parent geometry
   * @return a LinearRing if the transformation resulted in a structurally valid ring
   * @return a LineString if the transformation caused the LinearRing to collapse to 3 or fewer points
   */
  protected Geometry transformLinearRing(final LinearRing geometry, final Geometry parent) {
    if (geometry == null) {
      return this.factory.linearRing();
    } else {
      final LineString points = transformCoordinates(geometry, geometry);
      if (points == null) {
        return this.factory.linearRing();
      } else {
        final int seqSize = points.getVertexCount();
        // ensure a valid LinearRing
        if (seqSize > 0 && seqSize < 4 && !this.preserveType) {
          return this.factory.lineString(points);
        } else {
          return this.factory.linearRing(points);
        }
      }
    }
  }

  /**
   * Transforms a {@link LineString} geometry.
   *
   * @param geom
   * @param parent
   * @return
   */
  protected Geometry transformLineString(final LineString geom, final Geometry parent) {
    // should check for 1-point sequences and downgrade them to points
    return this.factory.lineString(transformCoordinates(geom, geom));
  }

  protected Geometry transformMultiLineString(final MultiLineString geom, final Geometry parent) {
    final List transGeomList = new ArrayList();
    for (int i = 0; i < geom.getGeometryCount(); i++) {
      final Geometry transformGeom = transformLineString((LineString)geom.getGeometry(i), geom);
      if (transformGeom == null) {
        continue;
      }
      if (transformGeom.isEmpty()) {
        continue;
      }
      transGeomList.add(transformGeom);
    }
    return this.factory.buildGeometry(transGeomList);
  }

  protected Geometry transformMultiPoint(final MultiPoint geom, final Geometry parent) {
    final List transGeomList = new ArrayList();
    for (int i = 0; i < geom.getGeometryCount(); i++) {
      final Geometry transformGeom = transformPoint((Point)geom.getGeometry(i), geom);
      if (transformGeom == null) {
        continue;
      }
      if (transformGeom.isEmpty()) {
        continue;
      }
      transGeomList.add(transformGeom);
    }
    return this.factory.buildGeometry(transGeomList);
  }

  protected Geometry transformMultiPolygon(final MultiPolygon geom, final Geometry parent) {
    final List transGeomList = new ArrayList();
    for (int i = 0; i < geom.getGeometryCount(); i++) {
      final Geometry transformGeom = transformPolygon((Polygon)geom.getGeometry(i), geom);
      if (transformGeom == null) {
        continue;
      }
      if (transformGeom.isEmpty()) {
        continue;
      }
      transGeomList.add(transformGeom);
    }
    return this.factory.buildGeometry(transGeomList);
  }

  protected abstract Geometry transformPoint(final Point point, final Geometry parent);

  protected Geometry transformPolygon(final Polygon geom, final Geometry parent) {
    boolean isAllValidLinearRings = true;
    final Geometry shell = transformLinearRing(geom.getShell(), geom);

    if (shell == null || !(shell instanceof LinearRing) || shell.isEmpty()) {
      isAllValidLinearRings = false;
      // return factory.createPolygon(null, null);
    }

    final List<LinearRing> rings = new ArrayList<>();
    rings.add((LinearRing)shell);
    for (int i = 0; i < geom.getHoleCount(); i++) {
      final Geometry hole = transformLinearRing(geom.getHole(i), geom);
      if (hole == null || hole.isEmpty()) {
        continue;
      }
      if (!(hole instanceof LinearRing)) {
        isAllValidLinearRings = false;
      }

      rings.add((LinearRing)hole);
    }

    if (isAllValidLinearRings) {
      return this.factory.polygon(rings);
    } else {
      final List components = new ArrayList();
      if (shell != null) {
        components.add(shell);
      }
      components.addAll(rings);
      return this.factory.buildGeometry(components);
    }
  }

}
