package com.revolsys.jts.geom.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.revolsys.data.io.IteratorReader;
import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.io.Reader;
import com.revolsys.jts.geom.Dimension;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.segment.Segment;
import com.revolsys.jts.geom.vertex.MultiPointVertex;
import com.revolsys.jts.geom.vertex.Vertex;

public abstract class AbstractMultiPoint extends AbstractGeometryCollection implements MultiPoint {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> V appendVertex(final Point newPoint, final int... geometryId) {
    if (newPoint == null || newPoint.isEmpty()) {
      return (V)this;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      if (isEmpty()) {
        return newPoint.copy(geometryFactory);
      } else {
        final List<Point> points = getPoints();
        points.add(newPoint);
        return (V)geometryFactory.multiPoint(points);
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> V copy(final GeometryFactory geometryFactory) {
    final List<Point> newPoints = new ArrayList<>();
    final List<Point> points = getPoints();
    for (final Point point : points) {
      final Point newPoint = point.copy(geometryFactory);
      newPoints.add(newPoint);
    }
    return (V)geometryFactory.multiPoint(newPoints);
  }

  @Override
  protected double doDistance(final Geometry geometry, final double terminateDistance) {
    double minDistance = Double.MAX_VALUE;
    for (final Point point : getPoints()) {
      final double distance = geometry.distance(point, terminateDistance);
      if (distance < minDistance) {
        minDistance = distance;
        if (distance <= terminateDistance) {
          return distance;
        }
      }
    }
    return minDistance;
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
   * Zero-dimensional geometries have no boundary by definition,
   * so an empty GeometryCollection is returned.
   *
   * @return an empty GeometryCollection
   * @see Geometry#getBoundary
   */
  @Override
  public Geometry getBoundary() {
    return getGeometryFactory().geometryCollection();
  }

  @Override
  public int getBoundaryDimension() {
    return Dimension.FALSE;
  }

  /**
   *  Returns the <code>Coordinate</code> at the given position.
   *
   *@param  n  the partIndex of the <code>Coordinate</code> to retrieve, beginning
   *      at 0
   *@return    the <code>n</code>th <code>Coordinate</code>
   */
  protected Point getCoordinate(final int n) {
    return getPoint(n);
  }

  @Override
  public double getCoordinate(final int partIndex, final int axisIndex) {
    final Point point = getPoint(partIndex);
    return point.getCoordinate(axisIndex);
  }

  @Override
  public DataType getDataType() {
    return DataTypes.MULTI_POINT;
  }

  @Override
  public int getDimension() {
    return 0;
  }

  @Override
  public Point getPoint(final int partIndex) {
    return (Point)getGeometry(partIndex);
  }

  /**
   * @author Paul Austin <paul.austin@revolsys.com>
   */
  @Override
  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public <V extends Point> List<V> getPoints() {
    return (List)getGeometries();
  }

  @Override
  public Segment getSegment(final int... segmentId) {
    return null;
  }

  @Override
  public Vertex getToVertex(final int... vertexId) {
    if (vertexId.length <= 2) {
      if (vertexId.length == 1 || vertexId[1] == 0) {
        final int vertexIndex = vertexId[0];
        final int geometryCount = getGeometryCount();
        if (vertexIndex >= 0 || vertexIndex < geometryCount) {
          return new MultiPointVertex(this, geometryCount - vertexIndex - 1);
        }
      }
    }
    return null;
  }

  @Override
  public Vertex getVertex(final int... vertexId) {
    if (vertexId.length <= 2) {
      if (vertexId.length == 1 || vertexId[1] == 0) {
        final int vertexIndex = vertexId[0];
        if (vertexIndex >= 0 || vertexIndex < getGeometryCount()) {
          return new MultiPointVertex(this, vertexId);
        }
      }
    }
    return null;
  }

  @Override
  protected boolean isEquivalentClass(final Geometry other) {
    return other instanceof MultiPoint;
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> V moveVertex(Point newPoint, final int... vertexId) {
    if (newPoint == null || newPoint.isEmpty()) {
      return (V)this;
    } else if (vertexId.length <= 2) {
      if (isEmpty()) {
        throw new IllegalArgumentException("Cannot move vertex for empty MultiPoint");
      } else {
        final int partIndex = vertexId[0];
        final int partCount = getGeometryCount();
        if (partIndex >= 0 && partIndex < partCount) {
          final GeometryFactory geometryFactory = getGeometryFactory();

          newPoint = newPoint.copy(geometryFactory);
          final List<Point> points = new ArrayList<>(getPoints());
          points.set(partIndex, newPoint);
          return (V)geometryFactory.multiPoint(points);
        } else {
          throw new IllegalArgumentException("Part index must be between 0 and " + partCount
            + " not " + partIndex);
        }
      }
    } else {
      throw new IllegalArgumentException("Vertex id's for MultiPoint must have length 1. "
        + Arrays.toString(vertexId));
    }
  }

  @Override
  public MultiPoint normalize() {
    if (isEmpty()) {
      return this;
    } else {
      final List<Point> geometries = new ArrayList<>();
      for (final Geometry part : geometries()) {
        final Point normalizedPart = (Point)part.normalize();
        geometries.add(normalizedPart);
      }
      Collections.sort(geometries);
      final GeometryFactory geometryFactory = getGeometryFactory();
      final MultiPoint normalizedGeometry = geometryFactory.multiPoint(geometries);
      return normalizedGeometry;
    }
  }

  @Override
  public Iterable<Point> points() {
    return getGeometries();
  }

  @Override
  public IteratorReader<Segment> segments() {
    return new IteratorReader<Segment>();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <G extends Geometry> G toClockwise() {
    return (G)this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <G extends Geometry> G toCounterClockwise() {
    return (G)this;
  }

  @Override
  public Reader<Vertex> vertices() {
    final MultiPointVertex vertex = new MultiPointVertex(this, -1);
    return vertex.reader();
  }
}
