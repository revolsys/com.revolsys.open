package com.revolsys.geometry.model.editor;

import java.util.Arrays;
import java.util.Collections;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.util.number.Doubles;

public class PointEditor extends AbstractGeometryEditor<PointEditor>
  implements Point, PunctualEditor {
  private static final long serialVersionUID = 1L;

  private Point point;

  private double[] newCoordinates;

  public PointEditor(final AbstractGeometryCollectionEditor<?, ?, ?> parentEditor,
    final Point point) {
    super(parentEditor, point);
    this.point = point;
  }

  public PointEditor(final GeometryFactory geometryFactory) {
    super(geometryFactory);
  }

  public PointEditor(final Point point) {
    this(null, point);
  }

  @Override
  public GeometryEditor<?> appendVertex(final int[] geometryId, final Point point) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (point == null || point.isEmpty()) {
      return this;
    } else if (isEmpty()) {
      setVertex(point);
      return this;
    } else {
      final PointEditor editorThis = newGeometryEditor();
      final Point newPoint = point.convertGeometry(geometryFactory);
      final PointEditor editorOther = newPoint.newGeometryEditor();
      final GeometryCollectionImplEditor parentEditor = (GeometryCollectionImplEditor)getParentEditor();
      return new MultiPointEditor(parentEditor, geometryFactory, editorThis, editorOther);
    }
  }

  @Override
  public Point clone() {
    return (Point)super.clone();
  }

  @Override
  public PointEditor deleteVertex(final int[] vertexId) {
    // TODO deal with empty points
    return this;
  }

  @Override
  public Iterable<PointEditor> editors() {
    return Collections.singletonList(this);
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof Point) {
      final Point point = (Point)other;
      return equals(point);
    } else {
      return false;
    }
  }

  @Override
  public double getCoordinate(final int axisIndex) {
    if (this.newCoordinates == null) {
      return this.point.getCoordinate(axisIndex);
    } else {
      final int axisCount = getAxisCount();
      if (axisIndex >= 0 && axisIndex < axisCount) {
        return this.newCoordinates[axisIndex];
      } else {
        return java.lang.Double.NaN;
      }
    }
  }

  @Override
  public double getX() {
    if (this.newCoordinates == null) {
      return this.point.getX();
    } else {
      return this.newCoordinates[X];
    }
  }

  @Override
  public double getY() {
    if (this.newCoordinates == null) {
      return this.point.getY();
    } else {
      return this.newCoordinates[Y];
    }
  }

  @Override
  public int hashCode() {
    final double x = getX();
    final double y = getY();
    long bits = java.lang.Double.doubleToLongBits(x);
    bits ^= java.lang.Double.doubleToLongBits(y) * 31;
    return (int)bits ^ (int)(bits >> 32);
  }

  @Override
  public GeometryEditor<?> insertVertex(final int[] vertexId, final Point point) {
    if (vertexId.length == 1) {
      final int vertexIndex = vertexId[0];
      if (vertexIndex < 0 || vertexIndex > 1) {
        throw new IllegalArgumentException(
          "Vertex index must be 0 or 1 for point: " + Arrays.toString(vertexId));
      } else {
        final GeometryFactory geometryFactory = getGeometryFactory();
        if (point == null || point.isEmpty()) {
          return this;
        } else if (isEmpty()) {
          return setVertex(this.point);
        } else if (point.isEmpty()) {
          return this;
        } else {
          final PointEditor editorThis = newGeometryEditor();
          final Point newPoint = point.convertGeometry(geometryFactory);
          final PointEditor editorOther = newPoint.newGeometryEditor();
          final GeometryCollectionImplEditor parentEditor = (GeometryCollectionImplEditor)getParentEditor();
          if (vertexIndex == 0) {
            return new MultiPointEditor(parentEditor, geometryFactory, editorOther, editorThis);
          } else {
            return new MultiPointEditor(parentEditor, geometryFactory, editorThis, editorOther);
          }
        }
      }
    } else {
      throw new IllegalArgumentException("Vertex id's for " + getGeometryType()
        + " must have length 1. " + Arrays.toString(vertexId));
    }

  }

  @Override
  public boolean isEmpty() {
    return this.point.isEmpty();
  }

  @Override
  public Point newGeometry() {
    if (this.newCoordinates == null) {
      return this.point;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      return this.point.newPoint(geometryFactory, this.newCoordinates);
    }
  }

  @Override
  public Point newPoint(final GeometryFactory geometryFactory, final double... coordinates) {
    return this.point.newPoint(geometryFactory, coordinates);
  }

  @Override
  public GeometryEditor<?> setAxisCount(final int axisCount) {
    final int oldAxisCount = getAxisCount();
    if (oldAxisCount != axisCount) {
      this.newCoordinates = getCoordinates(axisCount);
      super.setAxisCount(axisCount);
    }
    return this;
  }

  @Override
  public double setCoordinate(final int axisIndex, final double coordinate) {
    final double oldValue = this.point.getCoordinate(axisIndex);
    if (!Doubles.equal(coordinate, oldValue)) {
      final int axisCount = getAxisCount();
      if (axisIndex >= 0 && axisIndex < axisCount) {
        if (this.newCoordinates == null) {
          this.newCoordinates = this.point.getCoordinates(axisCount);
        }
        this.newCoordinates[axisIndex] = coordinate;
      }
    }
    return oldValue;
  }

  @Override
  public double setCoordinate(final int partIndex, final int axisIndex, final double coordinate) {
    if (partIndex == 0) {
      return setCoordinate(axisIndex, coordinate);
    } else {
      return Double.NaN;
    }
  }

  @Override
  public PointEditor setCoordinate(final int[] vertexId, final int axisIndex,
    final double coordinate) {
    if (vertexId.length == 0) {
      setCoordinate(axisIndex, coordinate);
    }
    return this;
  }

  @Override
  public PointEditor setVertex(final int[] vertexId, final Point newPoint) {
    final int axisCount = getAxisCount();
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      final double coordinate = newPoint.getCoordinate(axisIndex);
      setCoordinate(vertexId, axisIndex, coordinate);
    }
    return this;
  }

  public PointEditor setVertex(final Point point) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final Point newPoint = point.convertGeometry(geometryFactory);
    final int axisCount = getAxisCount();
    if (this.newCoordinates == null) {
      this.newCoordinates = newPoint.getCoordinates(axisCount);
    } else {
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        final double coordinate = newPoint.getCoordinate(axisIndex);
        this.newCoordinates[axisIndex] = coordinate;
      }
    }
    return this;
  }

  @Override
  public String toString() {
    return toEwkt();
  }
}
