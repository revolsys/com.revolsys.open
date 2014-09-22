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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.revolsys.data.io.IteratorReader;
import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.io.Reader;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Dimension;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.segment.MultiLineStringSegment;
import com.revolsys.jts.geom.segment.Segment;
import com.revolsys.jts.geom.vertex.MultiLineStringVertex;
import com.revolsys.jts.geom.vertex.Vertex;
import com.revolsys.jts.operation.BoundaryOp;

/**
 * Models a collection of (@link LineString}s.
 * <p>
 * Any collection of LineStrings is a valid MultiLineString.
 *
 *@version 1.7
 */
public abstract class AbstractMultiLineString extends
AbstractGeometryCollection implements MultiLineString {

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

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> V copy(final GeometryFactory geometryFactory) {
    final List<LineString> lines = new ArrayList<LineString>();
    for (final LineString line : getLineStrings()) {
      final LineString newLine = line.copy(geometryFactory);
      lines.add(newLine);
    }
    return (V)geometryFactory.multiLineString(lines);
  }

  @Override
  public boolean equalsExact(final Geometry other, final double tolerance) {
    if (!isEquivalentClass(other)) {
      return false;
    } else {
      return super.equalsExact(other, tolerance);
    }
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
    return new BoundaryOp(this).getBoundary();
  }

  @Override
  public int getBoundaryDimension() {
    if (isClosed()) {
      return Dimension.FALSE;
    }
    return 0;
  }

  @Override
  public BoundingBox getBoundingBox() {
    if (this.boundingBox == null) {
      if (isEmpty()) {
        this.boundingBox = new BoundingBoxDoubleGf(getGeometryFactory());
      } else {
        this.boundingBox = computeBoundingBox();
      }
    }
    return this.boundingBox;
  }

  @Override
  public DataType getDataType() {
    return DataTypes.MULTI_LINE_STRING;
  }

  @Override
  public int getDimension() {
    return 1;
  }

  @Override
  public LineString getLineString(final int partIndex) {
    return (LineString)getGeometry(partIndex);
  }

  @Override
  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public <V extends LineString> List<V> getLineStrings() {
    return (List)getGeometries();
  }

  @Override
  public Segment getSegment(final int... segmentId) {
    if (segmentId == null || segmentId.length != 2) {
      return null;
    } else {
      final int partIndex = segmentId[0];
      if (partIndex >= 0 && partIndex < getGeometryCount()) {
        final LineString line = getLineString(partIndex);
        final int segmentIndex = segmentId[1];
        if (segmentIndex >= 0 && segmentIndex < line.getSegmentCount()) {
          return new MultiLineStringSegment(this, segmentId);
        }
      }
      return null;
    }
  }

  @Override
  public Vertex getToVertex(int... vertexId) {
    if (vertexId == null || vertexId.length != 2) {
      return null;
    } else {
      final int partIndex = vertexId[0];
      if (partIndex >= 0 && partIndex < getGeometryCount()) {
        final LineString line = getLineString(partIndex);
        int vertexIndex = vertexId[1];
        final int vertexCount = line.getVertexCount();
        vertexIndex = vertexCount - 1 - vertexIndex;
        if (vertexIndex <= vertexCount) {
          while (vertexIndex < 0) {
            vertexIndex += vertexCount - 1;
          }
          vertexId = setVertexIndex(vertexId, vertexIndex);
          return new MultiLineStringVertex(this, vertexId);
        }
      }
      return null;
    }
  }

  /**
   * Gets the user data object for this geometry, if any.
   *
   * @return the user data object, or <code>null</code> if none set
   */
  @Override
  public Object getUserData() {
    return this.userData;
  }

  @Override
  public Vertex getVertex(final int... vertexId) {
    if (vertexId == null || vertexId.length != 2) {
      return null;
    } else {
      final int partIndex = vertexId[0];
      if (partIndex >= 0 && partIndex < getGeometryCount()) {
        final LineString line = getLineString(partIndex);
        int vertexIndex = vertexId[1];
        final int vertexCount = line.getVertexCount();
        if (vertexIndex <= vertexCount) {
          while (vertexIndex < 0) {
            vertexIndex += vertexCount - 1;
          }
          return new MultiLineStringVertex(this, vertexId);
        }
      }
      return null;
    }
  }

  @Override
  public boolean isClosed() {
    if (isEmpty()) {
      return false;
    }
    for (final LineString line : getLineStrings()) {
      if (line.isEmpty()) {
        return false;
      } else if (!line.isClosed()) {
        return false;
      }
    }
    return true;
  }

  @Override
  protected boolean isEquivalentClass(final Geometry other) {
    return other instanceof MultiLineString;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> V moveVertex(final Point newPoint,
    final int... vertexId) {
    if (newPoint == null || newPoint.isEmpty()) {
      return (V)this;
    } else if (vertexId.length == 2) {
      if (isEmpty()) {
        throw new IllegalArgumentException(
            "Cannot move vertex for empty MultiLineString");
      } else {
        final int partIndex = vertexId[0];
        final int vertexIndex = vertexId[1];
        final int partCount = getGeometryCount();
        if (partIndex >= 0 && partIndex < partCount) {
          final GeometryFactory geometryFactory = getGeometryFactory();

          final LineString line = getLineString(partIndex);
          final LineString newLine = line.moveVertex(newPoint, vertexIndex);
          final List<LineString> lines = new ArrayList<>(getLineStrings());
          lines.set(partIndex, newLine);
          return (V)geometryFactory.multiLineString(lines);
        } else {
          throw new IllegalArgumentException(
            "Part index must be between 0 and " + partCount + " not "
                + partIndex);
        }
      }
    } else {
      throw new IllegalArgumentException(
        "Vertex id's for MultiLineStrings must have length 2. "
            + Arrays.toString(vertexId));
    }
  }

  @Override
  public MultiLineString normalize() {
    if (isEmpty()) {
      return this;
    } else {
      final List<LineString> geometries = new ArrayList<>();
      for (final Geometry part : geometries()) {
        final LineString normalizedPart = (LineString)part.normalize();
        geometries.add(normalizedPart);
      }
      Collections.sort(geometries);
      final GeometryFactory geometryFactory = getGeometryFactory();
      final MultiLineString normalizedGeometry = geometryFactory.multiLineString(geometries);
      return normalizedGeometry;
    }
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
  public MultiLineString reverse() {
    final LinkedList<LineString> revLines = new LinkedList<LineString>();
    for (final Geometry geometry : geometries()) {
      final LineString line = (LineString)geometry;
      final LineString reverse = line.reverse();
      revLines.addFirst(reverse);
    }
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.multiLineString(revLines);
  }

  @Override
  public Reader<Segment> segments() {
    final MultiLineStringSegment iterator = new MultiLineStringSegment(this, 0,
      -1);
    return new IteratorReader<Segment>(iterator);
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

  @Override
  public Reader<Vertex> vertices() {
    final MultiLineStringVertex vertex = new MultiLineStringVertex(this, 0, -1);
    return vertex.reader();
  }

}
