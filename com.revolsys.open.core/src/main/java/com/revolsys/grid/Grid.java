package com.revolsys.grid;

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

import com.revolsys.awt.WebColors;
import com.revolsys.geometry.cs.projection.CoordinatesOperation;
import com.revolsys.geometry.cs.projection.CoordinatesOperationPoint;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.editor.LineStringEditor;
import com.revolsys.properties.ObjectWithProperties;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.MathUtil;
import com.revolsys.util.function.Consumer3Double;

public interface Grid extends ObjectWithProperties, BoundingBoxProxy {
  String GEOMETRY_FACTORY = "geometryFactory";

  int NULL_COLOUR = WebColors.colorToRGB(0, 0, 0, 0);

  static int getGridCellX(final double minX, final double gridCellSize, final double x) {
    final double deltaX = x - minX;
    final double cellDiv = deltaX / gridCellSize;
    final int gridX = (int)Math.floor(cellDiv);
    return gridX;
  }

  static int getGridCellY(final double minY, final double gridCellSize, final double y) {
    final double deltaY = y - minY;
    final double cellDiv = deltaY / gridCellSize;
    final int gridY = (int)Math.floor(cellDiv);
    return gridY;
  }

  void clear();

  default void forEachPoint(final Consumer3Double action) {
    final double gridCellSize = getGridCellSize();
    final double minY = getGridMinY();
    final double minX = getGridMinX();
    final int gridWidth = getGridWidth();
    final int gridHeight = getGridHeight();
    for (int gridY = 0; gridY < gridHeight; gridY++) {
      final double y = minY + gridY * gridCellSize;
      for (int gridX = 0; gridX < gridWidth; gridX++) {
        final double x = minX + gridX * gridCellSize;
        final double value = getValueFast(gridX, gridY);
        action.accept(x, y, value);
      }
    }
  }

  default void forEachPointFinite(final BoundingBox boundingBox, final Consumer<Point> action) {
    final GeometryFactory targetGeometryFactory = boundingBox.getGeometryFactory();
    final GeometryFactory geometryFactory = getGeometryFactory();

    final CoordinatesOperation projection = geometryFactory
      .getCoordinatesOperation(targetGeometryFactory);

    final BoundingBox convertexBoundingBox = boundingBox.convert(geometryFactory);
    final double gridCellSize = getGridCellSize();
    final double minY = getGridMinY();
    final double minX = getGridMinX();
    final int gridWidth = getGridWidth();
    final int gridHeight = getGridHeight();

    int startGridX = (int)Math.floor((convertexBoundingBox.getMinX() - minX) / gridCellSize);
    if (startGridX < 0) {
      startGridX = 0;
    }
    int endGridX = (int)Math.ceil((convertexBoundingBox.getMaxX() - minX) / gridCellSize);
    if (endGridX > gridWidth) {
      endGridX = gridWidth;
    }

    int startGridY = (int)Math.floor((convertexBoundingBox.getMinY() - minY) / gridCellSize);
    if (startGridY < 0) {
      startGridY = 0;
    }
    int endGridY = (int)Math.ceil((convertexBoundingBox.getMaxY() - minY) / gridCellSize);
    if (endGridY > gridHeight) {
      endGridY = gridHeight;
    }

    if (projection == null) {
      for (int gridY = startGridY; gridY < endGridY; gridY++) {
        final double y = minY + gridY * gridCellSize;
        for (int gridX = startGridX; gridX < endGridX; gridX++) {
          final double x = minX + gridX * gridCellSize;
          final double value = getValueFast(gridX, gridY);
          if (Double.isFinite(value)) {
            if (boundingBox.covers(x, y)) {
              final Point point = targetGeometryFactory.point(x, y, value);
              action.accept(point);
            }
          }
        }
      }
    } else {
      final CoordinatesOperationPoint point = new CoordinatesOperationPoint();
      for (int gridY = startGridY; gridY < endGridY; gridY++) {
        final double y = minY + gridY * gridCellSize;
        for (int gridX = startGridX; gridX < endGridX; gridX++) {
          final double x = minX + gridX * gridCellSize;
          final double value = getValueFast(gridX, gridY);
          if (Double.isFinite(value)) {
            point.setPoint(x, y, value);
            projection.perform(point);
            final double targetX = point.x;
            final double targetY = point.y;
            final double targetZ = point.z;
            if (boundingBox.covers(targetX, targetY)) {
              final Point targetPoint = targetGeometryFactory.point(targetX, targetY, targetZ);
              action.accept(targetPoint);
            }
          }
        }
      }
    }
  }

  default void forEachPointFinite(final Consumer3Double action) {
    final double gridCellSize = getGridCellSize();
    final double minY = getGridMinY();
    final double minX = getGridMinX();
    final int gridWidth = getGridWidth();
    final int gridHeight = getGridHeight();
    for (int gridY = 0; gridY < gridHeight; gridY++) {
      final double y = minY + gridY * gridCellSize;
      for (int gridX = 0; gridX < gridWidth; gridX++) {
        final double x = minX + gridX * gridCellSize;
        final double value = getValueFast(gridX, gridY);
        if (Double.isFinite(value)) {
          action.accept(x, y, value);
        }
      }
    }
  }

  default void forEachValueFinite(final DoubleConsumer action) {
    final int gridWidth = getGridWidth();
    final int gridHeight = getGridHeight();
    for (int gridY = 0; gridY < gridHeight; gridY++) {
      for (int gridX = 0; gridX < gridWidth; gridX++) {
        final double value = getValueFast(gridX, gridY);
        if (Double.isFinite(value)) {
          action.accept(value);
        }
      }
    }
  }

  default double getAspectRatio() {
    final int width = getGridWidth();
    final int height = getGridHeight();
    if (width > 0 && height > 0) {
      return (double)width / height;
    } else {
      return 0;
    }
  }

  default Polygon getBoundaryXY() {
    final GeometryFactory geometryFactory = getGeometryFactory().convertAxisCount(2);
    final LineStringEditor points = new LineStringEditor(geometryFactory);
    final double minX = getGridMinX();
    final double minY = getGridMinY();

    final double gridCellSize = getGridCellSize();
    final int gridHeight = getGridHeight();
    final int gridWidth = getGridWidth();
    final int maxGridXIndex = gridWidth - 1;
    final int maxGridYIndex = gridHeight - 1;

    int firstGridY = -1;
    int minGridX = -1;
    int maxGridX = 0;
    int minGridY = Integer.MAX_VALUE;
    int maxGridY = 0;
    int lastGridX = 0;
    int lastGridY = 0;
    int gridX = 0;
    int gridY = 0;
    // Process South edge
    while (gridX != gridWidth) {
      final double value = getValue(gridX, gridY);
      if (Double.isFinite(value)) {
        if (minGridX == -1) {
          minGridX = gridX;
        }
        maxGridX = gridX;
        if (firstGridY == -1) {
          firstGridY = gridY;
        }
        if (gridY < minGridY) {
          minGridY = gridY;
        }
        if (gridY > maxGridY) {
          maxGridY = gridY;
        }
        double x = minX + gridX * gridCellSize;
        if (gridX == maxGridXIndex) {
          x += gridCellSize;
        }
        final double y = minY + gridY * gridCellSize;
        final int vertexCount = points.getVertexCount();
        if (vertexCount < 2) {
          points.appendVertex(x, y);
        } else {
          final double lastY = points.getY(vertexCount - 1);
          if (lastY == y) {
            if (points.getY(vertexCount - 2) == y) {
              points.setX(vertexCount - 1, x);
            } else {
              points.appendVertex(x, y);
            }
          } else {
            if (points.getY(vertexCount - 2) == lastY) {
              points.setX(vertexCount - 1, x);
            } else {
              points.appendVertex(x, lastY);
            }
            points.appendVertex(x, y);
          }
        }
        lastGridX = gridX;
        lastGridY = gridY;
        gridX++;
        gridY = 0;
      } else {
        gridY++;
        if (gridY == gridHeight) {
          gridX++;
          gridY = 0;
        }
      }
    }
    // Process East edge
    gridX = maxGridX;
    gridY = lastGridY + 1;
    while (gridY != gridHeight) {
      final double value = getValue(gridX, gridY);
      if (Double.isFinite(value)) {
        maxGridY = gridY;
        double x = minX + gridX * gridCellSize;
        if (gridX == maxGridXIndex) {
          x += gridCellSize;
        }
        double y = minY + gridY * gridCellSize;
        if (gridY == maxGridYIndex) {
          y += gridCellSize;
        }

        final int vertexCount = points.getVertexCount();
        if (vertexCount < 2) {
          points.appendVertex(x, y);
        } else {
          final double lastX = points.getX(vertexCount - 1);
          if (lastX == x) {
            if (points.getX(vertexCount - 2) == x) {
              points.setY(vertexCount - 1, y);
            } else {
              points.appendVertex(x, y);
            }
          } else {
            if (points.getX(vertexCount - 2) == lastX) {
              points.setY(vertexCount - 1, y);
            } else {
              points.appendVertex(lastX, y);
            }
            points.appendVertex(x, y);
          }
        }
        lastGridX = gridX;
        lastGridY = gridY;
        gridX = maxGridX;
        gridY++;
      } else {
        gridX--;
        if (gridX < minGridX) {
          gridY++;
          gridX = maxGridX;
        }
      }
    }
    // Process North edge
    gridX = lastGridX - 1;
    gridY = maxGridY;
    while (gridX >= minGridX) {
      final double value = getValue(gridX, gridY);
      if (Double.isFinite(value)) {
        double x = minX + gridX * gridCellSize;
        if (gridX == maxGridXIndex) {
          x += gridCellSize;
        }
        double y = minY + gridY * gridCellSize;
        if (gridY == maxGridYIndex) {
          y += gridCellSize;
        }
        final int vertexCount = points.getVertexCount();
        if (vertexCount < 2) {
          points.appendVertex(x, y);
        } else {
          final double lastY = points.getY(vertexCount - 1);
          if (lastY == y) {
            if (points.getY(vertexCount - 2) == y) {
              points.setX(vertexCount - 1, x);
            } else {
              points.appendVertex(x, y);
            }
          } else {
            if (points.getY(vertexCount - 2) == lastY) {
              points.setX(vertexCount - 1, x);
            } else {
              points.appendVertex(x, lastY);
            }
            points.appendVertex(x, y);
          }
        }
        lastGridX = gridX;
        lastGridY = gridY;
        gridX--;
        gridY = maxGridY;
      } else {
        gridY--;
        if (gridY < minGridY) {
          gridX--;
          gridY = maxGridY;
        }
      }
    }
    // Process West edge
    gridX = minGridX;
    gridY = lastGridY - 1;
    while (gridY > firstGridY) {
      final double value = getValue(gridX, gridY);
      if (Double.isFinite(value)) {
        final double x = minX + gridX * gridCellSize;
        final double y = minY + gridY * gridCellSize;
        final int vertexCount = points.getVertexCount();
        if (vertexCount < 2) {
          points.appendVertex(x, y);
        } else {
          final double lastX = points.getX(vertexCount - 1);
          if (lastX == x) {
            if (points.getX(vertexCount - 2) == x) {
              points.setY(vertexCount - 1, y);
            } else {
              points.appendVertex(x, y);
            }
          } else {
            if (points.getX(vertexCount - 2) == lastX) {
              points.setY(vertexCount - 1, y);
            } else {
              points.appendVertex(lastX, y);
            }
            points.appendVertex(x, y);
          }
        }
        lastGridX = gridX;
        lastGridY = gridY;
        gridX = minGridX;
        gridY--;
      } else {
        gridX++;
        if (gridX > maxGridX) {
          gridY--;
          gridX = minGridX;
        }
      }
    }
    final int vertexCount = points.getVertexCount();
    if (vertexCount > 2) {
      final double x = points.getX(0);
      final double y = points.getY(0);

      final double lastX = points.getX(vertexCount - 1);
      if (lastX == x) {
        if (points.getX(vertexCount - 2) == x) {
          points.setY(vertexCount - 1, y);
        } else {
          points.appendVertex(x, y);
        }
      } else {
        if (points.getX(vertexCount - 2) == lastX) {
          points.setY(vertexCount - 1, y);
        } else {
          points.appendVertex(lastX, y);
        }
      }
      points.appendVertex(x, y);
    }
    if (points.isEmpty()) {
      return getBoundingBox().toPolygon();
    } else {
      return points.newPolygon();
    }
  }

  default int getColour(final int gridX, final int gridY) {
    throw new UnsupportedOperationException();
  }

  @Override
  default GeometryFactory getGeometryFactory() {
    final BoundingBox boundingBox = getBoundingBox();
    return boundingBox.getGeometryFactory();
  }

  double getGridCellSize();

  default int getGridCellX(final double x) {
    final double minX = getGridMinX();
    final double gridCellSize = getGridCellSize();
    final double deltaX = x - minX;
    final double cellDiv = deltaX / gridCellSize;
    return (int)Math.floor(cellDiv);
  }

  default int getGridCellXRound(final double x) {
    final double minX = getGridMinX();
    final double gridCellSize = getGridCellSize();
    final double deltaX = x - minX;
    final double cellDiv = deltaX / gridCellSize;
    return (int)Math.round(cellDiv);
  }

  default int getGridCellY(final double y) {
    final double minY = getGridMinY();
    final double gridCellSize = getGridCellSize();
    final double deltaY = y - minY;
    final double cellDiv = deltaY / gridCellSize;
    return (int)Math.floor(cellDiv);
  }

  default int getGridCellYRound(final double y) {
    final double minY = getGridMinY();
    final double gridCellSize = getGridCellSize();
    final double deltaY = y - minY;
    final double cellDiv = deltaY / gridCellSize;
    return (int)Math.round(cellDiv);
  }

  int getGridHeight();

  default double getGridMinX() {
    final BoundingBox boundingBox = getBoundingBox();
    return boundingBox.getMinX();
  }

  default double getGridMinY() {
    final BoundingBox boundingBox = getBoundingBox();
    return boundingBox.getMinY();
  }

  int getGridWidth();

  double getMaxValue();

  double getMinValue();

  Resource getResource();

  default double getScaleXY() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final double scaleXy = geometryFactory.getScaleXY();
    return scaleXy;
  }

  /**
   * <p>Get the value at the given coordinates by rounding down to the grid cell.</p>
   *
   * <code>
   * gridX = floor(x - minX / gridCellSize);
   * gridY = floor(y - minY / gridCellSize);
   * value = getValue(gridX, gridY)
   * </code>
   *
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @return The value.
   * @see #getValue(int, int)
   */
  default double getValue(final double x, final double y) {
    final int gridX = getGridCellX(x);
    final int gridY = getGridCellY(y);
    return getValue(gridX, gridY);
  }

  default double getValue(int gridX, int gridY) {
    final int width = getGridWidth();
    final int height = getGridHeight();
    if (gridX < 0 || gridY < 0) {
      return Double.NaN;
    } else {
      if (gridX >= width) {
        if (gridX == width) {
          gridX--;
        } else {
          return Double.NaN;
        }
      }
      if (gridY >= height) {
        if (gridY == height) {
          gridY--;
        } else {
          return Double.NaN;
        }
      }
      return getValueFast(gridX, gridY);
    }
  }

  default double getValue(Point point) {
    point = convertGeometry(point);
    final double x = point.getX();
    final double y = point.getY();
    return getValue(x, y);
  }

  /**
   * Get the elevation using <a href="https://en.wikipedia.org/wiki/Bicubic_interpolation">Bicubic interpolation</a> using the 4x4 grid cells -1, 0, 1, 2.
   *
   * @param x The x-coordinate.
   * @param y The y-coordinate.
   * @return The interpolated elevation (z-coordinate).
   */
  default double getValueBicubic(final double x, final double y) {
    final double gridCellSize = getGridCellSize();

    final double xGrid = (x - getGridMinX()) / gridCellSize;
    final int gridX = (int)Math.floor(xGrid);
    final double xPercent = xGrid - gridX;

    final double yGrid = (y - getGridMinY()) / gridCellSize;
    final int gridY = (int)Math.floor(yGrid);
    final double yPercent = yGrid - gridY;

    final double z1 = getValueCubic(gridX, gridY - 1, xPercent);
    final double z2 = getValueCubic(gridX, gridY, xPercent);
    final double z3 = getValueCubic(gridX, gridY + 1, xPercent);
    final double z4 = getValueCubic(gridX, gridY + 2, xPercent);

    return MathUtil.cubicInterpolate(z1, z2, z3, z4, yPercent);
  }

  /**
   * Get the elevation of the point location using <a href="https://en.wikipedia.org/wiki/Bilinear_interpolation">Bilinear Interpolation</a> using the 2x2 grid cells 0, 1.
   *
   * @param x The x-coordinate.
   * @param y The y-coordinate.
   * @return The interpolated elevation (z-coordinate).
   */
  default double getValueBilinear(final double x, final double y) {
    final double gridCellSize = getGridCellSize();
    final double minX = getGridMinX();
    final double xGrid = (x - minX) / gridCellSize;
    final int gridX = (int)Math.floor(xGrid);
    final double minY = getGridMinY();
    final double yGrid = (y - minY) / gridCellSize;
    final int gridY = (int)Math.floor(yGrid);
    final double z11 = getValue(gridX, gridY);
    double z21 = getValue(gridX + 1, gridY);
    if (!Double.isFinite(z21)) {
      z21 = z11;
    }
    final double z12 = getValue(gridX, gridY + 1);
    if (!Double.isFinite(z12)) {
      z21 = z11;
    }
    double z22 = getValue(gridX + 1, gridY + 1);
    if (!Double.isFinite(z22)) {
      z22 = z21;
    }
    // Calculation is simplified as only the percent is required.
    final double xPercent = xGrid - gridX;
    final double yPercent = yGrid - gridY;
    final double x2x = 1 - xPercent;
    final double y2y = 1 - yPercent;

    return z11 * x2x * y2y + z21 * xPercent * y2y + z12 * x2x * yPercent
      + z22 * xPercent * yPercent;
    // MathUtil.bilinearInterpolation(double, double, double, double, double, double, double,
    // double, double, double)
  }

  default double getValueCubic(final int gridX, final int gridY, final double xPercent) {
    final double z1 = getValue(gridX - 1, gridY);
    final double z2 = getValue(gridX, gridY);
    final double z3 = getValue(gridX + 1, gridY);
    final double z4 = getValue(gridX + 2, gridY);
    return MathUtil.cubicInterpolate(z1, z2, z3, z4, xPercent);
  }

  double getValueFast(int gridX, int gridY);

  /**
   * <p>Get the elevation at the given coordinates by rounding to the nearest grid cell.</p>
   *
   * <code>
   * gridX = round(x - minX / gridCellSize);
   * gridY = round(y - minY / gridCellSize);
   * value = getValue(gridX, gridY)
   * </code>
   *
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @return The value.
   * @see #getValue(int, int)
   */
  default double getValueNearestNeighbour(final double x, final double y) {
    final int gridX = getGridCellXRound(x);
    final int gridY = getGridCellYRound(y);
    return getValue(gridX, gridY);
  }

  default double getX(final int i) {
    final double minX = getGridMinX();
    final double gridCellSize = getGridCellSize();
    return minX + i * gridCellSize;
  }

  default double getY(final int i) {
    final double maxY = getGridMinY();
    final double gridCellSize = getGridCellSize();
    return maxY + i * gridCellSize;
  }

  default boolean hasValue(final int gridX, final int gridY) {
    final double elevation = getValue(gridX, gridY);
    return Double.isFinite(elevation);
  }

  default boolean hasValueFast(final int gridX, final int gridY) {
    final double elevation = getValue(gridX, gridY);
    return Double.isFinite(elevation);
  }

  boolean isEmpty();

  default boolean isNull(final double x, final double y) {
    final int i = getGridCellX(x);
    final int j = getGridCellY(y);
    return isNull(i, j);
  }

  default boolean isNull(final int x, final int y) {
    final double elevation = getValue(x, y);
    return Double.isNaN(elevation);
  }

  default Grid newGrid(final BoundingBox boundingBox, final double gridCellSize) {
    final GeometryFactory geometryFactory = boundingBox.getGeometryFactory();
    final int minX = (int)boundingBox.getMinX();
    final int minY = (int)boundingBox.getMinY();
    final double width = boundingBox.getWidth();
    final double height = boundingBox.getHeight();

    final int modelWidth = (int)Math.ceil(width / gridCellSize);
    final int modelHeight = (int)Math.ceil(height / gridCellSize);
    final Grid grid = newGrid(geometryFactory, minX, minY, modelWidth, modelHeight, gridCellSize);
    final int maxX = (int)(minX + modelWidth * gridCellSize);
    final int maxY = (int)(minY + modelHeight * gridCellSize);
    for (double y = minY; y < maxY; y += gridCellSize) {
      for (double x = minX; x < maxX; x += gridCellSize) {
        setValue(grid, x, y);
      }
    }
    return grid;
  }

  default Grid newGrid(final double x, final double y, final int width, final int height) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final double gridCellSize = getGridCellSize();
    return newGrid(geometryFactory, x, y, width, height, gridCellSize);
  }

  default Grid newGrid(final GeometryFactory geometryFactory, final double x, final double y,
    final int width, final int height, final double gridCellSize) {
    return new IntArrayScaleGrid(geometryFactory, x, y, width, height, gridCellSize);
  }

  default Grid resample(final int newGridCellSize) {
    final int tileX = (int)getGridMinX();
    final int tileY = (int)getGridMinY();
    final double gridCellSize = getGridCellSize();
    final double cellRatio = gridCellSize / newGridCellSize;
    final int step = (int)Math.round(1 / cellRatio);
    final int gridWidth = getGridWidth();
    final int gridHeight = getGridHeight();

    final int newGridWidth = (int)Math.round(gridWidth * cellRatio);
    final int newGridHeight = (int)Math.round(gridHeight * cellRatio);

    final GeometryFactory geometryFactory = getGeometryFactory();
    final Grid newDem = new IntArrayScaleGrid(geometryFactory, tileX, tileY, newGridWidth,
      newGridHeight, newGridCellSize);

    int newGridY = 0;
    for (int gridYMin = 0; gridYMin < gridHeight; gridYMin += step) {
      final int gridYMax = gridYMin + step;
      int newGridX = 0;
      for (int gridXMin = 0; gridXMin < gridWidth; gridXMin += step) {
        final int gridXMax = gridXMin + step;
        int count = 0;
        double sum = 0;
        for (int gridY = gridYMin; gridY < gridYMax; gridY++) {
          for (int gridX = gridXMin; gridX < gridXMax; gridX++) {
            final double elevation = getValue(gridX, gridY);
            if (Double.isFinite(elevation)) {
              count++;
              sum += elevation;
            }
          }
        }
        if (count > 0) {
          final double elevation = geometryFactory.makeZPrecise(sum / count);
          newDem.setValue(newGridX, newGridY, elevation);
        }
        newGridX++;
      }
      newGridY++;
    }
    return newDem;
  }

  void setBoundingBox(BoundingBox boundingBox);

  default void setValue(final double x, final double y, final double elevation) {
    final int gridX = getGridCellX(x);
    final int gridY = getGridCellY(y);
    setValue(gridX, gridY, elevation);
  }

  default void setValue(final Grid grid, final double x, final double y) {
    final int gridX = getGridCellX(x);
    final int gridY = getGridCellY(y);
    final double elevation = grid.getValue(x, y);
    setValue(gridX, gridY, elevation);
  }

  default void setValue(final int gridX, final int gridY, final double elevation) {
    throw new UnsupportedOperationException("Value model is readonly");
  }

  default void setValue(final int gridX, final int gridY, final Grid grid, final double x,
    final double y) {
    final double elevation = grid.getValue(x, y);
    // if (Double.isFinite(elevation)) {
    setValue(gridX, gridY, elevation);
    // }
  }

  default void setValueNull(final double x, final double y) {
    final int gridX = getGridCellX(x);
    final int gridY = getGridCellY(y);
    setValueNull(gridX, gridY);
  }

  default void setValueNull(final int gridX, final int gridY) {
    setValue(gridX, gridY, Double.NaN);
  }

  default void setValues(final Geometry geometry) {
    if (geometry != null) {
      geometry.forEachVertex(getGeometryFactory(), point -> {
        final double x = point.x;
        final double y = point.y;
        final double value = point.z;
        setValue(x, y, value);
      });
    }
  }

  default void setValues(final Grid grid) {
    final double gridCellSize = getGridCellSize();
    if (grid.getGridCellSize() == gridCellSize) {
      final int gridWidth = getGridWidth();
      final int gridHeight = getGridHeight();

      final double minX1 = grid.getGridMinX();
      final double minY1 = grid.getGridMinY();

      int startX = getGridCellX(minX1);
      int endX = startX + grid.getGridWidth();
      if (startX < 0) {
        startX = 0;
      }
      if (endX > gridWidth) {
        endX = gridWidth;
      }
      int startY = getGridCellY(minY1);
      int endY = startY + grid.getGridHeight();
      if (startY < 0) {
        startY = 0;
      }
      if (endY > gridHeight) {
        endY = gridHeight;
      }
      final int minX = (int)(getGridMinX() + startX * gridCellSize);
      final int minY = (int)(getGridMinY() + startY * gridCellSize);

      double y = minY;
      for (int gridY = startY; gridY < endY; gridY++) {
        double x = minX;
        for (int gridX = startX; gridX < endX; gridX++) {
          setValue(gridX, gridY, grid, x, y);
          x += gridCellSize;
        }
        y += gridCellSize;
      }
    } else {
      throw new IllegalArgumentException(
        "gridCellSize " + grid.getGridCellSize() + " != " + gridCellSize);
    }
  }

  void setValuesForTriangle(final double x1, final double y1, final double z1, final double x2,
    final double y2, final double z2, final double x3, final double y3, final double z3);

  default void setValuesNull(final Grid grid) {
    final double minX1 = grid.getGridMinX();
    final double minY1 = grid.getGridMinY();

    int startX = getGridCellX(minX1);
    if (startX < 0) {
      startX = 0;
    }
    int startY = getGridCellY(minY1);
    if (startY < 0) {
      startY = 0;
    }

    final double gridCellSize = getGridCellSize();
    final int minX = (int)(getGridMinX() + startX * gridCellSize);
    final int minY = (int)(getGridMinY() + startY * gridCellSize);

    double y = minY;
    final int gridWidth = getGridWidth();
    final int gridHeight = getGridHeight();
    for (int gridY = startY; gridY < gridHeight; gridY++) {
      double x = minX;
      for (int gridX = startX; gridX < gridWidth; gridX++) {
        final Grid grid1 = grid;
        final double elevation = grid1.getValue(x, y);
        if (Double.isFinite(elevation)) {
          setValueNull(gridX, gridY);
        }
        x += gridCellSize;
      }
      y += gridCellSize;
    }
  }

  default void setValuesNullFast(final Iterable<? extends Point> points) {
    for (final Point point : points) {
      final double x = point.getX();
      final double y = point.getY();
      final int gridX = getGridCellX(x);
      final int gridY = getGridCellY(y);
      setValueNull(gridX, gridY);
    }
  }
}
