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
package com.revolsys.jts.geom.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.prep.PreparedMultiPolygon;

/**
 * Models a collection of {@link Polygon}s.
 * <p>
 * As per the OGC SFS specification, 
 * the Polygons in a MultiPolygon may not overlap, 
 * and may only touch at single points.
 * This allows the topological point-set semantics
 * to be well-defined.
 *  
 *
 *@version 1.7
 */
public class MultiPolygonImpl extends AbstractMultiPolygon implements
  MultiPolygon {

  private static final long serialVersionUID = 8166665132445433741L;

  /**
   *  The bounding box of this <code>Geometry</code>.
   */
  private BoundingBox boundingBox;

  /**
   * An object reference which can be used to carry ancillary data defined
   * by the client.
   */
  private Object userData;

  private final GeometryFactory geometryFactory;

  private Polygon[] polygons;

  public MultiPolygonImpl(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public MultiPolygonImpl(final GeometryFactory geometryFactory,
    final Polygon[] polygons) {
    this.geometryFactory = geometryFactory;
    if (polygons == null || polygons.length == 0) {
      this.polygons = null;
    } else if (hasNullElements(polygons)) {
      throw new IllegalArgumentException(
        "geometries must not contain null elements");
    } else {
      this.polygons = polygons;
    }
  }

  @Override
  public BoundingBox getBoundingBox() {
    if (boundingBox == null) {
      if (isEmpty()) {
        boundingBox = new Envelope(getGeometryFactory());
      } else {
        boundingBox = computeBoundingBox();
      }
    }
    return boundingBox;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> List<V> getGeometries() {
    if (polygons == null) {
      return new ArrayList<V>();
    } else {
      return (List<V>)new ArrayList<>(Arrays.asList(polygons));
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> V getGeometry(final int n) {
    if (polygons == null) {
      return null;
    } else {
      return (V)polygons[n];
    }
  }

  @Override
  public int getGeometryCount() {
    if (polygons == null) {
      return 0;
    } else {
      return polygons.length;
    }
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  /**
   * Gets the user data object for this geometry, if any.
   *
   * @return the user data object, or <code>null</code> if none set
   */
  @Override
  public Object getUserData() {
    return userData;
  }

  @Override
  public boolean isEmpty() {
    return polygons == null;
  }

  @Override
  public MultiPolygon prepare() {
    return new PreparedMultiPolygon(this);
  }

  /**
   * A simple scheme for applications to add their own custom data to a Geometry.
   * An example use might be to add an object representing a Point Reference System.
   * <p>
   * Note that user data objects are not present in geometries created by
   * construction methods.
   *
   * @param userData an object, the semantics for which are defined by the
   * application using this Geometry
   */
  @Override
  public void setUserData(final Object userData) {
    this.userData = userData;
  }

}
