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

import com.revolsys.gis.cs.projection.CoordinatesOperation;
import com.revolsys.gis.jts.GeometryProperties;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;

/**
 *  Models an OGC-style <code>LineString</code>.
 *  A LineString consists of a sequence of two or more vertices,
 *  along with all points along the linearly-interpolated curves
 *  (line segments) between each 
 *  pair of consecutive vertices.
 *  Consecutive vertices may be equal.
 *  The line segments in the line may intersect each other (in other words, 
 *  the linestring may "curl back" in itself and self-intersect.
 *  Linestrings with exactly two identical points are invalid. 
 *  <p> 
 * A linestring must have either 0 or 2 or more points.  
 * If these conditions are not met, the constructors throw 
 * an {@link IllegalArgumentException}
 *
 *@version 1.7
 */
public class LineStringImpl extends AbstractLineString implements LineString {

  /**
   * The {@link GeometryFactory} used to create this Geometry
   */
  private final GeometryFactory geometryFactory;

  private static final long serialVersionUID = 3110669828065365560L;

  /**
   *  The points of this <code>LineString</code>.
   */
  private double[] coordinates;

  public LineStringImpl(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
    this.coordinates = null;
  }

  public LineStringImpl(final GeometryFactory geometryFactory,
    final CoordinatesList points) {
    this.geometryFactory = geometryFactory;
    if (points == null) {
      this.coordinates = null;
    } else {
      final int vertexCount = points.size();
      if (vertexCount == 0) {
        this.coordinates = null;
      } else if (vertexCount == 1) {
        throw new IllegalArgumentException(
          "Invalid number of points in LineString (found " + vertexCount
            + " - must be 0 or >= 2)");
      } else {
        final int axisCount = getAxisCount();
        this.coordinates = new double[axisCount * vertexCount];
        for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
          for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
            double value = points.getValue(vertexIndex, axisIndex);
            value = geometryFactory.makePrecise(axisIndex, value);
            this.coordinates[vertexIndex * axisCount + axisIndex] = value;
          }
        }
      }
    }
  }

  public LineStringImpl(final GeometryFactory geometryFactory,
    final int axisCount, final double... points) {
    this.geometryFactory = geometryFactory;
    if (axisCount < 0 || axisCount == 1) {
      throw new IllegalArgumentException("axisCount must 0 or > 1 not "
        + axisCount);
    } else if (points == null || axisCount == 0) {
      this.coordinates = null;
    } else {
      final int coordinateCount = points.length;
      final int vertexCount = coordinateCount / axisCount;
      if (coordinateCount == 0) {
        this.coordinates = null;
      } else if (coordinateCount % axisCount != 0) {
        throw new IllegalArgumentException("Coordinate array length "
          + coordinateCount + " is not a multiple of axisCount=" + axisCount);
      } else if (coordinateCount == axisCount) {
        throw new IllegalArgumentException(
          "Invalid number of points in LineString (found " + vertexCount
            + " - must be 0 or >= 2)");
      } else {
        final int axisCountThis = getAxisCount();
        this.coordinates = new double[axisCountThis * vertexCount];
        for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
          for (int axisIndex = 0; axisIndex < axisCountThis; axisIndex++) {
            double value;
            if (axisIndex < axisCount) {
              value = points[vertexIndex * axisCount + axisIndex];
              value = geometryFactory.makePrecise(axisIndex, value);
            } else {
              value = Double.NaN;
            }
            this.coordinates[vertexIndex * axisCountThis + axisIndex] = value;
          }
        }
      }
    }
  }

  public LineStringImpl(final GeometryFactory geometryFactory,
    final int axisCount, final int vertexCount, final double... coordinates) {
    this.geometryFactory = geometryFactory;
    if (axisCount < 0 || axisCount == 1) {
      throw new IllegalArgumentException("axisCount must 0 or > 1 not "
        + axisCount);
    } else if (coordinates == null || axisCount == 0 || vertexCount == 0
      || coordinates.length == 0) {
      this.coordinates = null;
    } else {
      final int coordinateCount = vertexCount * axisCount;
      if (coordinates.length % axisCount != 0) {
        throw new IllegalArgumentException("coordinates.length="
          + coordinates.length + " must be a multiple of axisCount="
          + axisCount);
      } else if (coordinateCount > coordinates.length) {
        throw new IllegalArgumentException("axisCount=" + axisCount
          + " * vertexCount=" + vertexCount + " > coordinates.length="
          + coordinates.length);
      } else {
        final int axisCountThis = getAxisCount();
        this.coordinates = new double[axisCountThis * vertexCount];
        for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
          for (int axisIndex = 0; axisIndex < axisCountThis; axisIndex++) {
            double value;
            if (axisIndex < axisCount) {
              value = coordinates[vertexIndex * axisCount + axisIndex];
              value = geometryFactory.makePrecise(axisIndex, value);
            } else {
              value = Double.NaN;
            }
            this.coordinates[vertexIndex * axisCountThis + axisIndex] = value;
          }
        }
      }
    }
  }

  /**
   * Creates and returns a full copy of this {@link LineString} object.
   * (including all coordinates contained by it).
   *
   * @return a clone of this instance
   */
  @Override
  public LineStringImpl clone() {
    final LineStringImpl line = (LineStringImpl)super.clone();
    if (coordinates != null) {
      line.coordinates = coordinates.clone();
    }
    return line;
  }

  @Override
  protected double[] convertCoordinates(GeometryFactory geometryFactory) {
    final GeometryFactory sourceGeometryFactory = getGeometryFactory();
    if (isEmpty()) {
      return this.coordinates;
    } else {
      geometryFactory = getNonZeroGeometryFactory(geometryFactory);
      final CoordinatesOperation coordinatesOperation = sourceGeometryFactory.getCoordinatesOperation(geometryFactory);
      if (coordinatesOperation == null) {
        return this.coordinates;
      } else {
        final int sourceAxisCount = getAxisCount();
        final int vertexCount = getVertexCount();
        final double[] targetCoordinates = new double[sourceAxisCount
          * vertexCount];
        coordinatesOperation.perform(sourceAxisCount, this.coordinates,
          sourceAxisCount, targetCoordinates);
        return targetCoordinates;
      }
    }
  }

  @Override
  public Coordinates getCoordinate(final int vertexIndex) {
    if (isEmpty()) {
      return null;
    } else {
      final int axisCount = getAxisCount();
      final double[] coordinates = new double[axisCount];
      System.arraycopy(this.coordinates, vertexIndex * axisCount, coordinates,
        0, axisCount);
      return new DoubleCoordinates(coordinates);
    }
  }

  @Override
  public double getCoordinate(int vertexIndex, final int axisIndex) {
    if (isEmpty()) {
      return Double.NaN;
    } else {
      final int axisCount = getAxisCount();
      if (axisIndex < 0 || axisIndex >= axisCount) {
        return Double.NaN;
      } else {
        final int numPoints = getVertexCount();
        if (vertexIndex < numPoints) {
          while (vertexIndex < 0) {
            vertexIndex += numPoints;
          }
          return coordinates[vertexIndex * axisCount + axisIndex];
        } else {
          return Double.NaN;
        }
      }
    }
  }

  @Override
  public double[] getCoordinates() {
    if (this.coordinates == null) {
      return null;
    } else {
      return this.coordinates.clone();
    }
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  @Override
  public int getVertexCount() {
    if (isEmpty()) {
      return 0;
    } else {
      return coordinates.length / getAxisCount();
    }
  }

  @Override
  public boolean isEmpty() {
    return this.coordinates == null;
  }

  @Override
  public LineString merge(final Coordinates point, final LineString line) {
    final int axisCount = Math.max(getAxisCount(), line.getAxisCount());
    final int vertexCount1 = getVertexCount();
    final int vertexCount2 = line.getVertexCount();
    final int vertexCount = vertexCount1 + vertexCount2 - 1;
    final double[] coordinates = new double[vertexCount * axisCount];

    int newVertexCount = 0;
    final Coordinates coordinates1Start = getVertex(0);
    final Coordinates coordinates1End = getVertex(-1);
    final Coordinates coordinates2Start = line.getVertex(0);
    final Coordinates coordinates2End = line.getVertex(-1);
    if (coordinates1Start.equals2d(coordinates2End)
      && coordinates1Start.equals2d(point)) {
      newVertexCount = CoordinatesListUtil.append(axisCount, line, 0,
        coordinates, 0, vertexCount2);
      newVertexCount = CoordinatesListUtil.append(axisCount, this, 1,
        coordinates, newVertexCount, vertexCount1 - 1);
    } else if (coordinates2Start.equals2d(coordinates1End)
      && coordinates2Start.equals2d(point)) {
      newVertexCount = CoordinatesListUtil.append(axisCount, this, 0,
        coordinates, 0, vertexCount1);
      newVertexCount = CoordinatesListUtil.append(axisCount, line, 1,
        coordinates, newVertexCount, vertexCount2 - 1);
    } else if (coordinates1Start.equals2d(coordinates2Start)
      && coordinates1Start.equals2d(point)) {
      newVertexCount = CoordinatesListUtil.appendReverse(axisCount, line, 0,
        coordinates, 0, vertexCount2);
      newVertexCount = CoordinatesListUtil.append(axisCount, this, 1,
        coordinates, newVertexCount, vertexCount);
    } else if (coordinates1End.equals2d(coordinates2End)
      && coordinates1End.equals2d(point)) {
      newVertexCount = CoordinatesListUtil.append(axisCount, this, 0,
        coordinates, newVertexCount, vertexCount);
      newVertexCount = CoordinatesListUtil.appendReverse(axisCount, line, 1,
        coordinates, newVertexCount, vertexCount2 - 1);
    } else {
      throw new IllegalArgumentException("lines don't touch\n" + this + "\n"
        + line);

    }
    final GeometryFactory factory = getGeometryFactory();
    final LineString newLine = factory.lineString(axisCount, newVertexCount,
      coordinates);
    GeometryProperties.copyUserData(this, newLine);
    return line;
  }

  @Override
  public LineString merge(final LineString line) {
    final int axisCount = Math.max(getAxisCount(), line.getAxisCount());
    final int vertexCount1 = getVertexCount();
    final int vertexCount2 = line.getVertexCount();
    final int vertexCount = vertexCount1 + vertexCount2 - 1;
    final double[] coordinates = new double[vertexCount * axisCount];

    int newVertexCount = 0;
    final Coordinates coordinates1Start = getVertex(0);
    final Coordinates coordinates1End = getVertex(-1);
    final Coordinates coordinates2Start = line.getVertex(0);
    final Coordinates coordinates2End = line.getVertex(-1);
    if (coordinates1Start.equals2d(coordinates2End)) {
      newVertexCount = CoordinatesListUtil.append(axisCount, line, 0,
        coordinates, 0, vertexCount2);
      newVertexCount = CoordinatesListUtil.append(axisCount, this, 1,
        coordinates, newVertexCount, vertexCount1 - 1);
    } else if (coordinates2Start.equals2d(coordinates1End)) {
      newVertexCount = CoordinatesListUtil.append(axisCount, this, 0,
        coordinates, 0, vertexCount1);
      newVertexCount = CoordinatesListUtil.append(axisCount, line, 1,
        coordinates, newVertexCount, vertexCount2 - 1);
    } else if (coordinates1Start.equals2d(coordinates2Start)) {
      newVertexCount = CoordinatesListUtil.appendReverse(axisCount, line, 0,
        coordinates, 0, vertexCount2);
      newVertexCount = CoordinatesListUtil.append(axisCount, this, 1,
        coordinates, newVertexCount, vertexCount);
    } else if (coordinates1End.equals2d(coordinates2End)) {
      newVertexCount = CoordinatesListUtil.append(axisCount, this, 0,
        coordinates, newVertexCount, vertexCount);
      newVertexCount = CoordinatesListUtil.appendReverse(axisCount, line, 1,
        coordinates, newVertexCount, vertexCount2 - 1);
    } else {
      throw new IllegalArgumentException("lines don't touch\n" + this + "\n"
        + line);

    }
    final GeometryFactory factory = getGeometryFactory();
    final LineString newLine = factory.lineString(axisCount, newVertexCount,
      coordinates);
    GeometryProperties.copyUserData(this, newLine);
    return line;
  }
}
