package com.revolsys.elevation.tin;

import java.util.function.Consumer;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Triangle;
import com.revolsys.geometry.model.impl.AbstractTriangle;
import com.revolsys.geometry.model.impl.BaseBoundingBox;

public abstract class BaseCompactTriangulatedIrregularNetwork {
  private class TinTriangle extends AbstractTriangle {
    private static final long serialVersionUID = 1L;

    private final int triangleIndex;

    public TinTriangle(final int triangleIndex) {
      this.triangleIndex = triangleIndex;
    }

    @Override
    public double getCoordinate(final int vertexIndex, final int axisIndex) {
      if (this.triangleIndex >= 0
        && this.triangleIndex < BaseCompactTriangulatedIrregularNetwork.this.triangleCount
        && axisIndex >= 0 && axisIndex < 3) {
        final int triangleVertexIndex = this.triangleIndex * 3 + vertexIndex;
        final int triangleVertexVertexIndex = BaseCompactTriangulatedIrregularNetwork.this.triangleVertexIndices[triangleVertexIndex];
        final double coordinate = getVertexCoordinate(triangleVertexVertexIndex, axisIndex);
        return coordinate;
      }
      return Double.NaN;
    }

    @Override
    public double[] getCoordinates() {
      final double[] coordinates = new double[9];

      int triangleVertexIndex = this.triangleIndex * 3;
      final int triangleVertexIndexMax = triangleVertexIndex + 3;
      int coordinateIndex = 0;
      while (triangleVertexIndex < triangleVertexIndexMax) {
        final int vertexIndex = BaseCompactTriangulatedIrregularNetwork.this.triangleVertexIndices[triangleVertexIndex++];
        for (int i = 0; i < 3; i++) {
          coordinates[coordinateIndex++] = getVertexCoordinate(vertexIndex, i);
        }
      }
      return coordinates;
    }

    @Override
    public GeometryFactory getGeometryFactory() {
      return BaseCompactTriangulatedIrregularNetwork.this.geometryFactory;
    }
  }

  private class TinTriangleBoundingBox extends BaseBoundingBox {
    private static final long serialVersionUID = 1L;

    private final int triangleIndex;

    public TinTriangleBoundingBox(final int triangleIndex) {
      this.triangleIndex = triangleIndex;
    }

    @Override
    public GeometryFactory getGeometryFactory() {
      return BaseCompactTriangulatedIrregularNetwork.this.geometryFactory;
    }

    @Override
    public double getMax(final int axisIndex) {
      if (axisIndex == 0 || axisIndex == 1) {
        double max = Double.NEGATIVE_INFINITY;
        int triangleVertexIndex = this.triangleIndex * 3;
        final int triangleVertexIndexMax = triangleVertexIndex + 3;
        while (triangleVertexIndex < triangleVertexIndexMax) {
          final int vertexIndex = BaseCompactTriangulatedIrregularNetwork.this.triangleVertexIndices[triangleVertexIndex++];
          final double value = getVertexCoordinate(vertexIndex, axisIndex);
          if (value > max) {
            max = value;
          }
        }
        return max;
      } else {
        return Double.NaN;
      }
    }

    @Override
    public double getMin(final int axisIndex) {
      if (axisIndex == 0 || axisIndex == 1) {
        double min = Double.POSITIVE_INFINITY;
        int triangleVertexIndex = this.triangleIndex * 3;
        final int triangleVertexIndexMax = triangleVertexIndex + 3;
        while (triangleVertexIndex < triangleVertexIndexMax) {
          final int vertexIndex = BaseCompactTriangulatedIrregularNetwork.this.triangleVertexIndices[triangleVertexIndex++];
          final double value = getVertexCoordinate(vertexIndex, axisIndex);
          if (value < min) {
            min = value;
          }
        }
        return min;
      } else {
        return Double.NaN;
      }
    }

    @Override
    public boolean isEmpty() {
      return false;
    }
  }

  private int[] triangleVertexIndices;

  private int triangleCount;

  private final GeometryFactory geometryFactory;

  public BaseCompactTriangulatedIrregularNetwork(final GeometryFactory geometryFactory,
    final int triangleCount, final int[] triangleVertexIndices) {
    this.geometryFactory = geometryFactory.convertAxisCount(3);
    this.triangleCount = triangleCount;
    this.triangleVertexIndices = triangleVertexIndices;

  }

  public BaseCompactTriangulatedIrregularNetwork(final GeometryFactory geometryFactory,
    final int[] triangleVertexIndices) {
    this(geometryFactory, triangleVertexIndices.length / 3, triangleVertexIndices);
  }

  protected int appendTriangleVertexIndices(final int vertexIndex1, final int vertexIndex2,
    final int vertexIndex3) {

    final int triangleCount = this.triangleCount;
    final int offset = triangleCount * 3;
    int[] triangleVertexIndices = this.triangleVertexIndices;
    if (triangleVertexIndices.length <= offset) {
      final int newLength = (triangleCount + (triangleCount >>> 1)) * 3;
      final int[] newTriangleVertexIndices = new int[newLength];
      System.arraycopy(triangleVertexIndices, 0, newTriangleVertexIndices, 0, offset);
      triangleVertexIndices = newTriangleVertexIndices;
      this.triangleVertexIndices = newTriangleVertexIndices;
    }

    triangleVertexIndices[offset] = vertexIndex1;
    triangleVertexIndices[offset + 1] = vertexIndex2;
    triangleVertexIndices[offset + 2] = vertexIndex3;

    this.triangleCount++;
    return triangleCount;
  }

  public void forEachTriangle(final Consumer<? super Triangle> action) {
    for (int i = 0; i < this.triangleCount; i++) {
      final Triangle triangle = newTriangle(i);
      action.accept(triangle);
    }
  }

  public void forEachVertex(final Consumer<Point> action) {
    for (int i = 0; i < getVertexCount(); i++) {
      final Point point = getVertex(i);
      action.accept(point);
    }
  }

  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  public int getTriangleCount() {
    return this.triangleCount;
  }

  public int getTriangleVertexIndex(final int triangleIndex, final int axisIndex) {
    return this.triangleVertexIndices[triangleIndex * 3 + axisIndex];
  }

  protected int[] getTriangleVertexIndices() {
    return this.triangleVertexIndices;
  }

  protected abstract Point getVertex(int i);

  protected abstract double getVertexCoordinate(int vertexIndex, int axisIndex);

  public abstract int getVertexCount();

  public Triangle newTriangle(final int triangleIndex) {
    if (triangleIndex >= 0 && triangleIndex < this.triangleCount) {
      return new TinTriangle(triangleIndex);
    } else {
      return null;
    }
  }

  public BoundingBox newTriangleBoundingBox(final int triangleIndex) {
    return new TinTriangleBoundingBox(triangleIndex);
  }

  protected void setTriangleVertexIndices(final int triangleIndex, final int vertexIndex1,
    final int vertexIndex2, final int vertexIndex3) {
    final int offset = triangleIndex * 3;
    this.triangleVertexIndices[offset] = vertexIndex1;
    this.triangleVertexIndices[offset + 1] = vertexIndex2;
    this.triangleVertexIndices[offset + 2] = vertexIndex3;
  }
}
