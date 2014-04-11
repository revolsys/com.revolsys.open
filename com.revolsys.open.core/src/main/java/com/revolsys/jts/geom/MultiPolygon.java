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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import com.revolsys.collection.AbstractIterator;

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
public class MultiPolygon extends GeometryCollection implements Polygonal {
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
  public MultiPolygon(final Polygon[] polygons, final GeometryFactory factory) {
    super(polygons, factory);
  }

  /**
   *  Constructs a <code>MultiPolygon</code>.
   *
   *@param  polygons        the <code>Polygon</code>s for this <code>MultiPolygon</code>
   *      , or <code>null</code> or an empty array to create the empty geometry.
   *      Elements may be empty <code>Polygon</code>s, but not <code>null</code>
   *      s. The polygons must conform to the assertions specified in the <A
   *      HREF="http://www.opengis.org/techno/specs.htm">OpenGIS Simple Features
   *      Specification for SQL</A> .
   *@param  precisionModel  the specification of the grid of allowable points
   *      for this <code>MultiPolygon</code>
   *@param  SRID            the ID of the Spatial Reference System used by this
   *      <code>MultiPolygon</code>
   * @deprecated Use GeometryFactory instead
   */
  @Deprecated
  public MultiPolygon(final Polygon[] polygons,
    final PrecisionModel precisionModel, final int SRID) {
    this(polygons, new GeometryFactory(precisionModel, SRID));
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
    if (isEmpty()) {
      return getGeometryFactory().createMultiLineString();
    }
    final ArrayList allRings = new ArrayList();
    for (int i = 0; i < geometries.length; i++) {
      final Polygon polygon = (Polygon)geometries[i];
      final Geometry rings = polygon.getBoundary();
      for (int j = 0; j < rings.getNumGeometries(); j++) {
        allRings.add(rings.getGeometry(j));
      }
    }
    final LineString[] allRingsArray = new LineString[allRings.size()];
    return getGeometryFactory().createMultiLineString(
      (LineString[])allRings.toArray(allRingsArray));
  }

  @Override
  public int getBoundaryDimension() {
    return 1;
  }

  @Override
  public int getDimension() {
    return 2;
  }

  @Override
  public String getGeometryType() {
    return "MultiPolygon";
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public <V extends Polygon> List<V> getPolygons() {
    return (List)super.getGeometries();
  }

  /*
   * public boolean isSimple() { return true; }
   */

  @Override
  public MultiPolygon normalize() {
    if (isEmpty()) {
      return this;
    } else {
      final List<Polygon> geometries = new ArrayList<>();
      for (final Geometry part : this.geometries) {
        final Polygon normalizedPart = (Polygon)part.normalize();
        geometries.add(normalizedPart);
      }
      Collections.sort(geometries);
      final GeometryFactory geometryFactory = getGeometryFactory();
      final MultiPolygon normalizedGeometry = geometryFactory.createMultiPolygon(geometries);
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
  public Geometry reverse() {
    final int n = geometries.length;
    final Polygon[] revGeoms = new Polygon[n];
    for (int i = 0; i < geometries.length; i++) {
      revGeoms[i] = (Polygon)geometries[i].reverse();
    }
    return getGeometryFactory().createMultiPolygon(revGeoms);
  }

  /**
   * @author Paul Austin <paul.austin@revolsys.com>
   */
  @Override
  public Iterable<Vertex> vertices() {
    return new AbstractIterator<Vertex>() {
      private VertexImpl vertex = new VertexImpl(MultiPolygon.this, 0);

      private int vertexIndex = 0;

      private int ringIndex = 0;

      private int partIndex = 0;

      private Polygon polygon = getPolygons().get(0);

      private LineString ring = polygon.getExteriorRing();

      @Override
      protected Vertex getNext() throws NoSuchElementException {
        while (vertexIndex >= ring.getNumPoints()) {
          vertexIndex = 0;
          ringIndex++;
          if (ringIndex < 1 + polygon.getNumInteriorRing()) {
            ring = polygon.getInteriorRingN(ringIndex - 1);
          } else {
            partIndex++;
            if (partIndex < getNumGeometries()) {
              polygon = getPolygons().get(partIndex);
              ring = polygon.getExteriorRing();
            } else {
              vertex = null;
              throw new NoSuchElementException();
            }
          }
        }

        vertex.setVertexId(ringIndex, vertexIndex);
        vertexIndex++;
        return vertex;
      }
    };
  }
}
