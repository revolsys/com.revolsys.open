package com.revolsys.geometry.model.editor;

import java.util.Arrays;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.impl.LinearRingDoubleGf;
import com.revolsys.util.number.Doubles;

public class LineStringEditor extends AbstractGeometryEditor implements LineString, LinealEditor {
  private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

  private static final long serialVersionUID = 1L;

  private static int hugeCapacity(final int minCapacity) {
    if (minCapacity < 0) {
      throw new OutOfMemoryError();
    }
    return minCapacity > MAX_ARRAY_SIZE ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
  }

  public static LineStringEditor newLineStringEditor(final LineString line) {
    final GeometryFactory geometryFactory = line.getGeometryFactory();
    final int axisCount = line.getAxisCount();
    final double[] coordinates = line.getCoordinates();
    return new LineStringEditor(geometryFactory, axisCount, coordinates);
  }

  private final int axisCount;

  private double[] coordinates;

  private final LineString line;

  private int vertexCount;

  public LineStringEditor(final AbstractGeometryEditor parentEditor, final LineString line) {
    super(parentEditor, line);
    this.axisCount = line.getAxisCount();
    this.line = line;
    this.vertexCount = line.getVertexCount();
  }

  public LineStringEditor(final GeometryFactory geometryFactory) {
    super(geometryFactory);
    this.axisCount = geometryFactory.getAxisCount();
    this.line = newLineStringEmpty();
    this.coordinates = new double[0];
    this.vertexCount = this.coordinates.length / this.axisCount;
  }

  public LineStringEditor(final GeometryFactory geometryFactory, int vertexCapacity) {
    super(geometryFactory);
    if (vertexCapacity < 0) {
      vertexCapacity = 0;
    }
    this.axisCount = geometryFactory.getAxisCount();
    this.line = newLineStringEmpty();
    this.coordinates = new double[vertexCapacity * this.axisCount];
    Arrays.fill(this.coordinates, Double.NaN);
    this.vertexCount = 0;
  }

  public LineStringEditor(final GeometryFactory geometryFactory, final int axisCount,
    final double... coordinates) {
    super(geometryFactory.convertAxisCount(axisCount));
    if (axisCount < 2) {
      throw new IllegalArgumentException("axisCount=" + axisCount + " must be >= 2");
    }
    this.axisCount = axisCount;
    this.line = newLineStringEmpty();
    if (coordinates == null || coordinates.length == 0) {
      this.coordinates = new double[0];
    } else {
      this.coordinates = coordinates;
    }
    this.vertexCount = this.coordinates.length / axisCount;
  }

  public LineStringEditor(final int axisCount, final int vertexCount, final double... coordinates) {
    super(GeometryFactory.floating(0, axisCount));
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
    this.line = newLineStringEmpty();
  }

  public LineStringEditor(final LineString line) {
    this(null, line);
  }

  public int appendVertex(final double... coordinates) {
    final int index = getVertexCount();
    insertVertex(index, coordinates);
    return index;
  }

  public int appendVertex(final double x, final double y) {
    final int index = getVertexCount();
    if (insertVertex(index, x, y)) {
      return index;
    } else {
      return -1;
    }
  }

  public int appendVertex(final double x, final double y, final double z) {
    final int index = getVertexCount();
    if (insertVertex(index, x, y, z)) {
      return index;
    } else {
      return -1;
    }
  }

  public int appendVertex(final Point point) {
    final int index = getVertexCount();
    if (insertVertex(index, point)) {
      return index;
    } else {
      return -1;
    }
  }

  public int appendVertex(final Point point, final boolean allowRepeated) {
    final int index = getVertexCount();
    if (insertVertex(index, point, allowRepeated)) {
      return index;
    } else {
      return -1;
    }
  }

  public void appendVertices(final Geometry points) {
    final Iterable<? extends Point> vertices = points.vertices();
    appendVertices(vertices);
  }

  public void appendVertices(final Iterable<? extends Point> points) {
    for (final Point point : points) {
      appendVertex(point);
    }
  }

  public void clear() {
    this.vertexCount = 0;
  }

  @Override
  public LineStringEditor clone() {
    final LineStringEditor clone = (LineStringEditor)super.clone();
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

  @Override
  public double getCoordinate(final int vertexIndex, final int axisIndex) {
    if (this.coordinates == null) {
      return this.line.getCoordinate(vertexIndex, axisIndex);
    } else {
      final int axisCount = this.axisCount;
      if (vertexIndex >= 0 && vertexIndex < this.vertexCount && axisIndex < axisCount) {
        return this.coordinates[vertexIndex * axisCount + axisIndex];
      } else {
        return Double.NaN;
      }
    }
  }

  @Override
  public double[] getCoordinates() {
    if (this.coordinates == null) {
      return this.line.getCoordinates();
    } else {
      final double[] coordinates = new double[this.vertexCount * this.axisCount];
      System.arraycopy(this.coordinates, 0, coordinates, 0, coordinates.length);
      return coordinates;
    }
  }

  public LineString getOriginalGeometry() {
    return this.line;
  }

  @Override
  public int getVertexCount() {
    return this.vertexCount;
  }

  @Override
  public double getX(final int vertexIndex) {
    if (this.coordinates == null) {
      return this.line.getX(vertexIndex);
    } else {
      final int axisCount = this.axisCount;
      if (vertexIndex >= 0 && vertexIndex < this.vertexCount) {
        return this.coordinates[vertexIndex * axisCount];
      } else {
        return Double.NaN;
      }
    }
  }

  @Override
  public double getY(final int vertexIndex) {
    if (this.coordinates == null) {
      return this.line.getX(vertexIndex);
    } else {
      if (vertexIndex >= 0 && vertexIndex < this.vertexCount) {
        return this.coordinates[vertexIndex * this.axisCount + Y];
      } else {
        return Double.NaN;
      }
    }
  }

  @Override
  public double getZ(final int vertexIndex) {
    if (this.axisCount > 2) {
      if (this.coordinates == null) {
        return this.line.getX(vertexIndex);
      } else {
        final int axisCount = this.axisCount;
        if (vertexIndex >= 0 && vertexIndex < this.vertexCount) {
          return this.coordinates[vertexIndex * axisCount + Z];
        }
      }
    }
    return Double.NaN;
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
    insertVertexShift(index);
    setVertex(index, coordinates);
  }

  public boolean insertVertex(final int index, final double x, final double y) {
    insertVertexShift(index);
    return setVertex(index, x, y);
  }

  public boolean insertVertex(final int index, final double x, final double y, final double z) {
    insertVertexShift(index);
    return setVertex(index, x, y, z);
  }

  public boolean insertVertex(final int index, final Point point) {
    insertVertexShift(index);
    return setVertex(index, point);
  }

  public boolean insertVertex(final int index, final Point point, final boolean allowRepeated) {
    if (!allowRepeated) {
      final int vertexCount = getVertexCount();
      if (vertexCount > 0) {
        if (index > 0) {
          if (equalsVertex(index - 1, point)) {
            return false;
          }
        }
        if (index < vertexCount) {
          if (equalsVertex(index, point)) {
            return false;
          }
        }
      }
    }
    return insertVertex(index, point);
  }

  private void insertVertexShift(final int index) {
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
  }

  @Override
  public boolean isEmpty() {
    return this.vertexCount == 0;
  }

  public Geometry newBestGeometry() {
    final int vertexCount = getVertexCount();
    if (vertexCount == 1) {
      return newPoint();
    } else if (vertexCount == 2) {
      return newLineString();
    } else if (vertexCount == 3) {
      if (isClosed()) {
        final GeometryFactory geometryFactory = getGeometryFactory();
        return geometryFactory.lineString(this.axisCount, 2, this.coordinates);
      }
    }
    return newPolygon();
  }

  @Override
  public LineString newGeometry() {
    if (this.coordinates == null) {
      return this.line.newLineString();
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final int axisCount = geometryFactory.getAxisCount();
      return this.line.newLineString(geometryFactory, axisCount, this.vertexCount,
        this.coordinates);
    }
  }

  @Override
  public LinearRing newLinearRing() {
    final int coordinateCount = this.vertexCount * this.axisCount;
    final double[] coordinates = new double[coordinateCount];
    System.arraycopy(this.coordinates, 0, coordinates, 0, coordinateCount);
    final GeometryFactory geometryFactory = getGeometryFactory();
    return new LinearRingDoubleGf(geometryFactory, this.axisCount, this.vertexCount, coordinates);
  }

  @Override
  public LineString newLineString(final GeometryFactory geometryFactory, final int axisCount,
    final int vertexCount, final double... coordinates) {
    return this.line.newLineString(geometryFactory, axisCount, vertexCount, coordinates);
  }

  public Point newPoint() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.point(this.coordinates);
  }

  public Polygon newPolygon() {
    final LinearRing ring = newLinearRing();
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.polygon(ring);
  }

  @Override
  public int setAxisCount(final int axisCount) {
    final int oldAxisCount = getAxisCount();
    if (oldAxisCount != axisCount) {
      this.coordinates = getCoordinates(axisCount);
      return super.setAxisCount(axisCount);
    }
    return oldAxisCount;
  }

  @Override
  public double setCoordinate(final int axisIndex, final double coordinate, final int... vertexId) {
    if (vertexId.length == 1) {
      final int vertexIndex = vertexId[0];
      return setCoordinate(vertexIndex, axisIndex, coordinate);
    } else {
      return Double.NaN;
    }
  }

  public double setCoordinate(final int vertexIndex, final int axisIndex, final double coordinate) {
    if (vertexIndex < 0) {
      throw new IllegalArgumentException("vertexIndex=" + vertexIndex + " must be >=0");
    } else {
      if (vertexIndex >= this.vertexCount) {
        ensureCapacity(vertexIndex + 1);
        setModified(true);
        this.vertexCount = vertexIndex + 1;
      }
      final int vertexCount = getVertexCount();
      if (vertexIndex >= 0 && vertexIndex < vertexCount) {
        final int axisCount = getAxisCount();
        if (axisIndex >= 0 && axisIndex < axisCount) {
          final double oldValue;
          boolean changed;
          if (this.line == null) {
            changed = true;
            oldValue = Double.NaN;
          } else {
            oldValue = this.line.getCoordinate(vertexIndex, axisIndex);
            changed = !Doubles.equal(coordinate, oldValue);
          }
          if (changed) {
            if (this.coordinates == null) {
              setModified(true);
              if (this.line == null) {
                this.coordinates = new double[(vertexIndex + 1) * axisCount];
              } else {
                this.coordinates = this.line.getCoordinates(axisCount);
              }
            }
            final GeometryFactory geometryFactory = getGeometryFactory();
            final double preciseCoordinate = geometryFactory.makePrecise(axisIndex, coordinate);
            this.coordinates[vertexIndex * axisCount + axisIndex] = preciseCoordinate;
            return oldValue;
          }
        }
      }
      return Double.NaN;
    }
  }

  @Override
  public double setCoordinate(final int partIndex, final int vertexIndex, final int axisIndex,
    final double coordinate) {
    if (partIndex == 0) {
      return setCoordinate(vertexIndex, axisIndex, coordinate);
    } else {
      return Double.NaN;
    }
  }

  public double setM(final int vertexIndex, final double m) {
    return setCoordinate(vertexIndex, M, m);
  }

  public void setVertex(final int index, final double... coordinates) {
    if (index >= 0 && index < this.vertexCount) {
      final int axisCount = getAxisCount();
      int coordinateAxisCount = coordinates.length;
      if (coordinateAxisCount > axisCount) {
        coordinateAxisCount = axisCount;
      }
      int offset = index * axisCount;
      final GeometryFactory geometryFactory = getGeometryFactory();
      this.coordinates[offset++] = geometryFactory.makeXPrecise(coordinates[0]);
      this.coordinates[offset++] = geometryFactory.makeYPrecise(coordinates[1]);
      for (int axisIndex = 2; axisIndex < coordinateAxisCount; axisIndex++) {
        this.coordinates[offset++] = geometryFactory.makePrecise(axisIndex, coordinates[axisIndex]);
      }
    }
  }

  public boolean setVertex(final int index, final double x, final double y) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (index >= 0 && index < this.vertexCount) {
      final int axisCount = getAxisCount();
      final int offset = index * axisCount;
      this.coordinates[offset] = geometryFactory.makeXPrecise(x);
      this.coordinates[offset + 1] = geometryFactory.makeYPrecise(y);
      return true;
    } else {
      return false;
    }
  }

  public boolean setVertex(final int index, final double x, final double y, final double z) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (index >= 0 && index < this.vertexCount) {
      final int axisCount = getAxisCount();
      final int offset = index * axisCount;
      this.coordinates[offset] = geometryFactory.makeXPrecise(x);
      this.coordinates[offset + 1] = geometryFactory.makeYPrecise(y);
      if (axisCount > 2) {
        this.coordinates[offset + 2] = geometryFactory.makeZPrecise(z);
      }
      return true;
    } else {
      return false;
    }
  }

  public boolean setVertex(final int index, final Point point) {
    if (index >= 0 && index < this.vertexCount && point != null && !point.isEmpty()) {
      final int axisCount = getAxisCount();
      int pointAxisCount = point.getAxisCount();
      if (pointAxisCount > axisCount) {
        pointAxisCount = axisCount;
      }
      int offset = index * axisCount;
      final GeometryFactory geometryFactory = getGeometryFactory();
      final Point convertPoint2d = point.convertPoint2d(geometryFactory);
      this.coordinates[offset++] = geometryFactory.makeXPrecise(convertPoint2d.getX());
      this.coordinates[offset++] = geometryFactory.makeYPrecise(convertPoint2d.getY());
      for (int axisIndex = 2; axisIndex < pointAxisCount; axisIndex++) {
        this.coordinates[offset++] = geometryFactory.makePrecise(axisIndex,
          point.getCoordinate(axisIndex));
      }
      return true;
    } else {
      return false;
    }
  }

  public double setX(final int vertexIndex, final double x) {
    return setCoordinate(vertexIndex, X, x);
  }

  public double setY(final int vertexIndex, final double y) {
    return setCoordinate(vertexIndex, Y, y);
  }

  public double setZ(final int vertexIndex, final double z) {
    return setCoordinate(vertexIndex, Z, z);
  }

  @Override
  public String toString() {
    return toEwkt();
  }
}
