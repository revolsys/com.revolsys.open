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
import java.util.Collections;
import java.util.List;

import com.revolsys.gis.data.io.IteratorReader;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.io.Reader;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.segment.MultiPolygonSegment;
import com.revolsys.jts.geom.segment.Segment;
import com.revolsys.jts.geom.vertex.MultiPolygonVertex;
import com.revolsys.jts.geom.vertex.Vertex;

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
public class MultiPolygonImpl extends GeometryCollectionImpl implements
  MultiPolygon {

  private static final long serialVersionUID = -551033529766975875L;

  /**
   * @param polygons
   *            the <code>Polygon</code>s for this <code>MultiPolygon</code>,
   *            or <code>null</code> or an empty array to create the empty
   *            geometry. Elements may be empty <code>Polygon</code>s, but
   *            not <code>null</code>s. The polygons must conform to the
   *            assertions specified in the <A
   *            HREF="http://www.opengis.org/techno/specs.htm">OpenGIS Simple
   *            Features Specification for SQL</A>.
   */
  public MultiPolygonImpl(final Polygon[] polygons,
    final GeometryFactory factory) {
    super(factory, polygons);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> V copy(final GeometryFactory geometryFactory) {
    final List<Polygon> polygons = new ArrayList<>();
    for (final Polygon polygon : getPolygons()) {
      final Polygon newPolygon = polygon.copy(geometryFactory);
      polygons.add(newPolygon);
    }
    return (V)geometryFactory.multiPolygon(polygons);
  }

  @Override
  public boolean equalsExact(final Geometry other, final double tolerance) {
    if (!isEquivalentClass(other)) {
      return false;
    }
    return super.equalsExact(other, tolerance);
  }

  /**
   * Computes the boundary of this geometry
   *
   * @return a lineal geometry (which may be empty)
   * @see Geometry#getBoundary
   */
  @Override
  public Geometry getBoundary() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (isEmpty()) {
      return geometryFactory.multiLineString();
    }
    final List<LineString> allRings = new ArrayList<>();
    for (final Polygon polygon : getPolygons()) {
      final Geometry rings = polygon.getBoundary();
      for (int j = 0; j < rings.getGeometryCount(); j++) {
        final LineString ring = (LineString)rings.getGeometry(j);
        allRings.add(ring);
      }
    }
    return geometryFactory.multiLineString(allRings);
  }

  @Override
  public int getBoundaryDimension() {
    return 1;
  }

  @Override
  public DataType getDataType() {
    return DataTypes.MULTI_POLYGON;
  }

  @Override
  public int getDimension() {
    return 2;
  }

  @Override
  public Polygon getPolygon(final int partIndex) {
    return (Polygon)super.getGeometry(partIndex);
  }

  @Override
  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public <V extends Polygon> List<V> getPolygons() {
    return (List)super.getGeometries();
  }

  @Override
  public Segment getSegment(final int... segmentId) {
    if (segmentId == null || segmentId.length != 3) {
      return null;
    } else {
      final int partIndex = segmentId[0];
      if (partIndex >= 0 && partIndex < getGeometryCount()) {
        final Polygon polygon = getPolygon(partIndex);
        final int ringIndex = segmentId[1];
        if (ringIndex >= 0 && ringIndex < polygon.getRingCount()) {
          final LinearRing ring = polygon.getRing(ringIndex);
          final int segmentIndex = segmentId[2];
          if (segmentIndex >= 0 && segmentIndex < ring.getSegmentCount()) {
            return new MultiPolygonSegment(this, segmentId);
          }
        }
      }
      return null;
    }
  }

  @Override
  public Vertex getVertex(final int... vertexId) {
    if (vertexId == null || vertexId.length != 3) {
      return null;
    } else {
      final int partIndex = vertexId[0];
      if (partIndex >= 0 && partIndex < getGeometryCount()) {
        final Polygon polygon = getPolygon(partIndex);
        final int ringIndex = vertexId[1];
        if (ringIndex >= 0 && ringIndex < polygon.getRingCount()) {
          final LinearRing ring = polygon.getRing(ringIndex);
          final int vertexIndex = vertexId[2];
          if (vertexIndex >= 0 && vertexIndex < ring.getVertexCount()) {
            return new MultiPolygonVertex(this, vertexId);
          }
        }
      }
      return null;
    }
  }

  @Override
  public MultiPolygon normalize() {
    if (isEmpty()) {
      return this;
    } else {
      final List<Polygon> geometries = new ArrayList<>();
      for (final Geometry part : geometries()) {
        final Polygon normalizedPart = (Polygon)part.normalize();
        geometries.add(normalizedPart);
      }
      Collections.sort(geometries);
      final GeometryFactory geometryFactory = getGeometryFactory();
      final MultiPolygon normalizedGeometry = geometryFactory.multiPolygon(geometries);
      return normalizedGeometry;
    }
  }

  /**
   * Creates a {@link MultiPolygon} with
   * every component reversed.
   * The order of the components in the collection are not reversed.
   *
   * @return a MultiPolygon in the reverse order
   */
  @Override
  public MultiPolygon reverse() {
    final List<Polygon> polygons = new ArrayList<>();
    for (final Geometry geometry : geometries()) {
      final Polygon polygon = (Polygon)geometry;
      final Polygon reverse = polygon.reverse();
      polygons.add(reverse);
    }
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.multiPolygon(polygons);
  }

  @Override
  public Reader<Segment> segments() {
    final MultiPolygonSegment iterator = new MultiPolygonSegment(this, 0, 0, -1);
    return new IteratorReader<>(iterator);
  }

  @Override
  public Reader<Vertex> vertices() {
    final MultiPolygonVertex vertex = new MultiPolygonVertex(this, 0, 0, -1);
    return vertex.reader();
  }
}
