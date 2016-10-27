package com.revolsys.geometry.model.impl;

import java.util.Arrays;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;

public class LineStringDoubleBuilder extends AbstractLineString {
  private static final long serialVersionUID = 7579865828939708871L;

  private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

  private static int hugeCapacity(final int minCapacity) {
    if (minCapacity < 0) {
      throw new OutOfMemoryError();
    }
    return minCapacity > MAX_ARRAY_SIZE ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
  }

  public static LineStringDoubleBuilder newLineStringDoubleBuilder(final LineString line) {
    final GeometryFactory geometryFactory = line.getGeometryFactory();
    final int axisCount = line.getAxisCount();
    final double[] coordinates = line.getCoordinates();
    return new LineStringDoubleBuilder(geometryFactory, axisCount, coordinates);
  }

  protected GeometryFactory geometryFactory;

  private final int axisCount;

  private double[] coordinates;

  private int vertexCount;

  public LineStringDoubleBuilder(final GeometryFactory geometryFactory, final int axisCount) {
    if (axisCount < 2) {
      throw new IllegalArgumentException("axisCount=" + axisCount + " must be >= 2");
    }
    this.geometryFactory = geometryFactory.convertAxisCount(axisCount);
    this.axisCount = axisCount;
    this.coordinates = new double[0];
    this.vertexCount = this.coordinates.length / axisCount;
  }

  public LineStringDoubleBuilder(final GeometryFactory geometryFactory, final int axisCount,
    final double... coordinates) {
    if (axisCount < 2) {
      throw new IllegalArgumentException("axisCount=" + axisCount + " must be >= 2");
    }
    this.geometryFactory = geometryFactory.convertAxisCount(axisCount);
    this.axisCount = axisCount;
    if (coordinates == null || coordinates.length == 0) {
      this.coordinates = new double[0];
    } else {
      this.coordinates = coordinates;
    }
    this.vertexCount = this.coordinates.length / axisCount;
  }

  public LineStringDoubleBuilder(final GeometryFactory geometryFactory, final int vertexCount,
    final int axisCount) {
    if (axisCount < 2) {
      throw new IllegalArgumentException("axisCount=" + axisCount + " must be >= 2");
    }
    if (vertexCount < 0) {
      throw new IllegalArgumentException("vertexCount=" + vertexCount + " must be >= 0");
    }
    this.geometryFactory = geometryFactory.convertAxisCount(axisCount);
    this.coordinates = new double[vertexCount * axisCount];
    Arrays.fill(this.coordinates, Double.NaN);
    this.axisCount = (byte)axisCount;
    this.vertexCount = getCalculatedVertexCount();
  }

  public LineStringDoubleBuilder(final int axisCount, final int vertexCount,
    final double... coordinates) {
    if (coordinates == null || coordinates.length == 0) {
      this.axisCount = 2;
      this.coordinates = new double[0];
      this.vertexCount = 0;
    } else {
      assert axisCount >= 2;
      this.axisCount = (byte)axisCount;
      final int coordinateCount = vertexCount * axisCount;
      if (coordinates.length % axisCount != 0) {
        throw new IllegalArgumentException("coordinates.length=" + coordinates.length
          + " must be a multiple of axisCount=" + axisCount);
      } else if (coordinateCount == coordinates.length) {
        this.coordinates = coordinates;
      } else if (coordinateCount > coordinates.length) {
        throw new IllegalArgumentException("axisCount=" + axisCount + " * vertexCount="
          + vertexCount + " > coordinates.length=" + coordinates.length);
      } else {
        this.coordinates = coordinates;
        this.vertexCount = 0;
      }
    }
  }

  public int appendVertex(final double... coordinates) {
    final int index = getVertexCount();
    insertVertex(index, coordinates);
    return index;
  }

  public int appendVertex(final double x, final double y) {
    final int index = getVertexCount();
    insertVertex(index, x, y);
    return index;
  }

  public int appendVertex(final Point point) {
    final int index = getVertexCount();
    insertVertex(index, point);
    return index;
  }

  public int appendVertex(final Point point, final boolean allowRepeated) {
    final int index = getVertexCount();
    insertVertex(index, point, allowRepeated);
    return index;
  }

  @Override
  public LineStringDoubleBuilder clone() {
    final LineStringDoubleBuilder clone = (LineStringDoubleBuilder)super.clone();
    clone.coordinates = this.coordinates.clone();
    return clone;
  }

  private void ensureCapacity(final int vertexCount) {
    if (vertexCount >= this.vertexCount) {
      final int coordinateCount = vertexCount * this.axisCount;
      if (coordinateCount - this.coordinates.length > 0) {
        grow(coordinateCount);
      }
    }
  }

  @Override
  public int getAxisCount() {
    return this.axisCount;
  }

  private int getCalculatedVertexCount() {
    return this.coordinates.length / this.axisCount;
  }

  @Override
  public double getCoordinate(final int index, final int axisIndex) {
    final int axisCount = getAxisCount();
    if (index >= 0 && index < this.vertexCount && axisIndex < axisCount) {
      return this.coordinates[index * axisCount + axisIndex];
    } else {
      return Double.NaN;
    }
  }

  @Override
  public double[] getCoordinates() {
    final double[] coordinates = new double[this.vertexCount * this.axisCount];
    System.arraycopy(this.coordinates, 0, coordinates, 0, coordinates.length);
    return coordinates;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  public int getVertexCount() {
    return this.vertexCount;
  }

  private void grow(final int minCapacity) {
    // overflow-conscious code
    final int oldCapacity = this.coordinates.length;
    int newCapacity;
    if (oldCapacity == 0) {
      newCapacity = 10;
    } else {
      newCapacity = oldCapacity + (oldCapacity >> 1);
    }
    if (newCapacity - minCapacity < 0) {
      newCapacity = minCapacity;
    }
    if (newCapacity - MAX_ARRAY_SIZE > 0) {
      newCapacity = hugeCapacity(minCapacity);
    }
    // minCapacity is usually close to size, so this is a win:
    this.coordinates = Arrays.copyOf(this.coordinates, newCapacity);
    Arrays.fill(this.coordinates, oldCapacity, this.coordinates.length, Double.NaN);
  }

  public void insertVertex(final int index, final double... coordinates) {
    final int axisCount = getAxisCount();
    if (index >= this.vertexCount) {
      ensureCapacity(index);
      this.vertexCount = index + 1;
    } else {
      ensureCapacity(this.vertexCount + 1);
      final int offset = index * axisCount;
      final int newOffset = offset + axisCount;
      System.arraycopy(this.coordinates, offset, this.coordinates, newOffset,
        this.coordinates.length - newOffset);
      this.vertexCount++;
    }
    setVertex(index, coordinates);
  }

  public void insertVertex(final int index, final double x, final double y) {
    final int axisCount = getAxisCount();
    if (index >= this.vertexCount) {
      ensureCapacity(index + 1);
      this.vertexCount = index + 1;
    } else {
      ensureCapacity(this.vertexCount + 1);
      final int offset = index * axisCount;
      final int newOffset = offset + axisCount;
      System.arraycopy(this.coordinates, offset, this.coordinates, newOffset,
        this.coordinates.length - newOffset);
      this.vertexCount++;
    }
    setVertex(index, x, y);
  }

  public void insertVertex(final int index, final Point point) {
    final int axisCount = getAxisCount();
    if (index >= this.vertexCount) {
      ensureCapacity(index);
      this.vertexCount = index + 1;
    } else {
      ensureCapacity(this.vertexCount + 1);
      final int offset = index * axisCount;
      final int newOffset = offset + axisCount;
      System.arraycopy(this.coordinates, offset, this.coordinates, newOffset,
        this.vertexCount * axisCount - offset);
      this.vertexCount++;
    }
    setVertex(index, point);
  }

  public void insertVertex(final int index, final Point point, final boolean allowRepeated) {
    if (!allowRepeated) {
      final int vertexCount = getVertexCount();
      if (vertexCount > 0) {
        if (index > 0) {
          if (equalsVertex(index - 1, point)) {
            return;
          }
        }
        if (index < vertexCount) {
          if (equalsVertex(index, point)) {
            return;
          }
        }
      }
    }
    insertVertex(index, point);
  }

  @Override
  public boolean isEmpty() {
    return this.vertexCount == 0;
  }

  public void setCoordinate(final int index, final int axisIndex, final double coordinate) {
    if (index < 0) {
      throw new IllegalArgumentException("index=" + index + " must be >=0");
    } else {
      if (index >= this.vertexCount) {
        ensureCapacity(index + 1);
        this.vertexCount = index + 1;
      }
      final int axisCount = getAxisCount();
      if (axisIndex < axisCount) {
        this.coordinates[index * axisCount + axisIndex] = coordinate;
      }
    }
  }

  public void setVertex(final int index, final double... coordinates) {
    if (index >= 0 && index < this.vertexCount) {
      int axisCount = getAxisCount();
      final int coordinateAxisCount = coordinates.length;
      if (coordinateAxisCount < axisCount) {
        axisCount = coordinateAxisCount;
      }
      int offset = index * axisCount;
      for (int i = 0; i < coordinateAxisCount; i++) {
        this.coordinates[offset++] = coordinates[i];
      }
    }
  }

  public void setVertex(final int index, final double x, final double y) {
    if (index >= 0 && index < this.vertexCount) {
      final int axisCount = getAxisCount();
      final int offset = index * axisCount;
      this.coordinates[offset] = x;
      this.coordinates[offset + 1] = y;
    }
  }

  public void setVertex(final int index, final Point point) {
    if (index >= 0 && index < this.vertexCount) {
      int axisCount = getAxisCount();
      final int pointAxisCount = point.getAxisCount();
      if (pointAxisCount < axisCount) {
        axisCount = pointAxisCount;
      }
      int offset = index * axisCount;
      for (int i = 0; i < pointAxisCount; i++) {
        this.coordinates[offset++] = point.getCoordinate(i);
      }
    }
  }

}
