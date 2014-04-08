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

import java.util.List;
import java.util.NoSuchElementException;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.jts.operation.BoundaryOp;

/**
 * Models a collection of (@link LineString}s.
 * <p>
 * Any collection of LineStrings is a valid MultiLineString.
 *
 *@version 1.7
 */
public class MultiLineString extends GeometryCollection implements Lineal {
  private static final long serialVersionUID = 8166665132445433741L;

  /**
   * @param lineStrings
   *            the <code>LineString</code>s for this <code>MultiLineString</code>,
   *            or <code>null</code> or an empty array to create the empty
   *            geometry. Elements may be empty <code>LineString</code>s,
   *            but not <code>null</code>s.
   */
  public MultiLineString(final LineString[] lineStrings,
    final GeometryFactory factory) {
    super(lineStrings, factory);
  }

  /**
   *  Constructs a <code>MultiLineString</code>.
   *
   *@param  lineStrings     the <code>LineString</code>s for this <code>MultiLineString</code>
   *      , or <code>null</code> or an empty array to create the empty geometry.
   *      Elements may be empty <code>LineString</code>s, but not <code>null</code>
   *      s.
   *@param  precisionModel  the specification of the grid of allowable points
   *      for this <code>MultiLineString</code>
   *@param  SRID            the ID of the Spatial Reference System used by this
   *      <code>MultiLineString</code>
   * @deprecated Use GeometryFactory instead
   */
  @Deprecated
  public MultiLineString(final LineString[] lineStrings,
    final PrecisionModel precisionModel, final int SRID) {
    super(lineStrings, new GeometryFactory(precisionModel, SRID));
  }

  @Override
  public boolean equalsExact(final Geometry other, final double tolerance) {
    if (!isEquivalentClass(other)) {
      return false;
    }
    return super.equalsExact(other, tolerance);
  }

  /**
   * Gets the boundary of this geometry.
   * The boundary of a lineal geometry is always a zero-dimensional geometry (which may be empty).
   *
   * @return the boundary geometry
   * @see Geometry#getBoundary
   */
  @Override
  public Geometry getBoundary() {
    return (new BoundaryOp(this)).getBoundary();
  }

  @Override
  public int getBoundaryDimension() {
    if (isClosed()) {
      return Dimension.FALSE;
    }
    return 0;
  }

  @Override
  public int getDimension() {
    return 1;
  }

  @Override
  public String getGeometryType() {
    return "MultiLineString";
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public <V extends LineString> List<V> getLineStrings() {
    return (List)super.getGeometries();
  }

  public boolean isClosed() {
    if (isEmpty()) {
      return false;
    }
    for (int i = 0; i < geometries.length; i++) {
      if (!((LineString)geometries[i]).isClosed()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Creates a {@link MultiLineString} in the reverse
   * order to this object.
   * Both the order of the component LineStrings
   * and the order of their coordinate sequences
   * are reversed.
   *
   * @return a {@link MultiLineString} in the reverse order
   */
  @Override
  public Geometry reverse() {
    final int nLines = geometries.length;
    final LineString[] revLines = new LineString[nLines];
    for (int i = 0; i < geometries.length; i++) {
      revLines[nLines - 1 - i] = (LineString)geometries[i].reverse();
    }
    return getGeometryFactory().createMultiLineString(revLines);
  }

  /**
   * @author Paul Austin <paul.austin@revolsys.com>
   */
  @Override
  public Iterable<GeometryVertex> vertices() {
    return new AbstractIterator<GeometryVertex>() {
      private GeometryVertex vertex = new GeometryVertex(MultiLineString.this,
        0);

      private int vertexIndex = 0;

      private int partIndex = 0;

      private LineString line = getLineStrings().get(0);

      @Override
      protected GeometryVertex getNext() throws NoSuchElementException {
        while (vertexIndex >= line.getNumPoints()) {
          vertexIndex = 0;
          partIndex++;
          if (partIndex < getLineStrings().size()) {
            line = getLineStrings().get(partIndex);
          } else {
            vertex = null;
            throw new NoSuchElementException();
          }
        }

        vertex.setVertexId(partIndex, vertexIndex);
        vertexIndex++;
        return vertex;
      }
    };
  }

}
