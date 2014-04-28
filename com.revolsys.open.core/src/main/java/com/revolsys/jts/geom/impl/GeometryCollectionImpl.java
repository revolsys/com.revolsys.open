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

import com.revolsys.io.Reader;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.segment.Segment;
import com.revolsys.jts.geom.vertex.GeometryCollectionVertex;
import com.revolsys.jts.geom.vertex.Vertex;

/**
 * Models a collection of {@link Geometry}s of
 * arbitrary type and dimension.
 * 
 *
 *@version 1.7
 */
public class GeometryCollectionImpl extends AbstractGeometryCollection
  implements GeometryCollection {
  // With contributions from Markus Schaber [schabios@logi-track.com] 2004-03-26
  private static final long serialVersionUID = -5694727726395021467L;

  /**
   * The {@link GeometryFactory} used to create this Geometry
   */
  private final GeometryFactory geometryFactory;

  /**
   *  Internal representation of this <code>GeometryCollection</code>.
   */
  private Geometry[] geometries;

  public GeometryCollectionImpl(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  /**
   * @param geometries
   *            the <code>Geometry</code>s for this <code>GeometryCollection</code>,
   *            or <code>null</code> or an empty array to create the empty
   *            geometry. Elements may be empty <code>Geometry</code>s,
   *            but not <code>null</code>s.
   */
  public GeometryCollectionImpl(final GeometryFactory geometryFactory,
    final Geometry[] geometries) {
    this.geometryFactory = geometryFactory;
    if (geometries == null || geometries.length == 0) {
      this.geometries = null;
    } else if (hasNullElements(geometries)) {
      throw new IllegalArgumentException(
        "geometries must not contain null elements");
    } else {
      this.geometries = geometries;
    }
  }

  /**
   * Creates and returns a full copy of this {@link GeometryCollection} object.
   * (including all coordinates contained by it).
   *
   * @return a clone of this instance
   */
  @Override
  public GeometryCollectionImpl clone() {
    final GeometryCollectionImpl gc = (GeometryCollectionImpl)super.clone();
    gc.geometries = new Geometry[geometries.length];
    for (int i = 0; i < geometries.length; i++) {
      gc.geometries[i] = geometries[i].clone();
    }
    return gc;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> List<V> getGeometries() {
    if (geometries == null) {
      return new ArrayList<V>();
    } else {
      return (List<V>)new ArrayList<>(Arrays.asList(geometries));
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> V getGeometry(final int n) {
    if (geometries == null) {
      return null;
    } else {
      return (V)geometries[n];
    }
  }

  @Override
  public int getGeometryCount() {
    if (geometries == null) {
      return 0;
    } else {
      return geometries.length;
    }
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  @Override
  public Segment getSegment(final int... segmentId) {
    return new GeometryCollectionSegment(this, segmentId);
  }

  @Override
  public Vertex getVertex(final int... vertexId) {
    return new GeometryCollectionVertex(this, vertexId);
  }

  @Override
  public Reader<Segment> segments() {
    final GeometryCollectionSegment iterator = new GeometryCollectionSegment(
      this, -1);
    return iterator.reader();
  }

  @Override
  public Reader<Vertex> vertices() {
    final GeometryCollectionVertex iterator = new GeometryCollectionVertex(
      this, -1);
    return iterator.reader();
  }

}
