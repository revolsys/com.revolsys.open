package com.revolsys.elevation.gridded;

import java.util.Collections;
import java.util.Map;

import com.revolsys.awt.WebColors;
import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.gridded.compactbinary.CompactBinaryGriddedElevation;
import com.revolsys.elevation.gridded.esriascii.EsriAsciiGriddedElevation;
import com.revolsys.elevation.gridded.usgsdem.UsgsGriddedElevation;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Triangle;
import com.revolsys.geometry.model.editor.GeometryEditor;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.io.IoFactory;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.properties.ObjectWithProperties;
import com.revolsys.spring.resource.Resource;

public interface GriddedElevationModel extends ObjectWithProperties, BoundingBoxProxy {
  String GEOMETRY_FACTORY = "geometryFactory";

  int NULL_COLOUR = WebColors.colorToRGB(0, 0, 0, 0);

  static int getGridCellX(final double minX, final int gridCellSize, final double x) {
    final double deltaX = x - minX;
    final double cellDiv = deltaX / gridCellSize;
    final int gridX = (int)Math.floor(cellDiv);
    return gridX;
  }

  static int getGridCellY(final double minY, final int gridCellSize, final double y) {
    final double deltaY = y - minY;
    final double cellDiv = deltaY / gridCellSize;
    final int gridY = (int)Math.floor(cellDiv);
    return gridY;
  }

  static GriddedElevationModel newGriddedElevationModel(final Object source) {
    final Map<String, Object> properties = Collections.emptyMap();
    return newGriddedElevationModel(source, properties);
  }

  static GriddedElevationModel newGriddedElevationModel(final Object source,
    final Map<String, ? extends Object> properties) {
    final GriddedElevationModelReadFactory factory = IoFactory
      .factory(GriddedElevationModelReadFactory.class, source);
    if (factory == null) {
      return null;
    } else {
      final Resource resource = Resource.getResource(source);
      final GriddedElevationModel dem = factory.newGriddedElevationModel(resource, properties);
      return dem;
    }
  }

  public static void serviceInit() {
    IoFactoryRegistry.addFactory(new CompactBinaryGriddedElevation());
    IoFactoryRegistry.addFactory(new EsriAsciiGriddedElevation());
    IoFactoryRegistry.addFactory(new UsgsGriddedElevation());
  }

  default void cancelChanges() {
  }

  void clear();

  default double getAspectRatio() {
    final int width = getGridWidth();
    final int height = getGridHeight();
    if (width > 0 && height > 0) {
      return (double)width / height;
    } else {
      return 0;
    }
  }

  @Override
  BoundingBox getBoundingBox();

  default int getColour(final int gridX, final int gridY) {
    throw new UnsupportedOperationException();
  }

  default double getElevation(final double x, final double y) {
    final int gridX = getGridCellX(x);
    final int gridY = getGridCellY(y);
    return getElevation(gridX, gridY);
  }

  double getElevation(final int x, final int y);

  default double getElevation(Point point) {
    point = convertGeometry(point);
    final double x = point.getX();
    final double y = point.getY();
    return getElevation(x, y);
  }

  double getElevationFast(int gridX, int gridY);

  @Override
  default GeometryFactory getGeometryFactory() {
    final BoundingBox boundingBox = getBoundingBox();
    return boundingBox.getGeometryFactory();
  }

  int getGridCellSize();

  default int getGridCellX(final double x) {
    final double minX = getMinX();
    final int gridCellSize = getGridCellSize();
    return getGridCellX(minX, gridCellSize, x);
  }

  default int getGridCellY(final double y) {
    final double minY = getMinY();
    final int gridCellSize = getGridCellSize();
    return getGridCellY(minY, gridCellSize, y);
  }

  int getGridHeight();

  int getGridWidth();

  default double getMaxX() {
    final BoundingBox boundingBox = getBoundingBox();
    return boundingBox.getMaxX();
  }

  default double getMaxY() {
    final BoundingBox boundingBox = getBoundingBox();
    return boundingBox.getMaxY();
  }

  default double getMaxZ() {
    final BoundingBox boundingBox = getBoundingBox();
    return boundingBox.getMaxZ();
  }

  default double getMinX() {
    final BoundingBox boundingBox = getBoundingBox();
    return boundingBox.getMinX();
  }

  default double getMinY() {
    final BoundingBox boundingBox = getBoundingBox();
    return boundingBox.getMinY();
  }

  default double getMinZ() {
    final BoundingBox boundingBox = getBoundingBox();
    return boundingBox.getMinZ();
  }

  Resource getResource();

  default double getScaleXY() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final double scaleXy = geometryFactory.getScaleXY();
    return scaleXy;
  }

  default double getX(final int i) {
    final double minX = getMinX();
    final int gridCellSize = getGridCellSize();
    return minX + i * gridCellSize;
  }

  default double getY(final int i) {
    final double maxY = getMaxY();
    final int gridCellSize = getGridCellSize();
    return maxY - i * gridCellSize;
  }

  boolean isEmpty();

  default boolean isNull(final double x, final double y) {
    final int i = getGridCellX(x);
    final int j = getGridCellY(y);
    return isNull(i, j);
  }

  default boolean isNull(final int x, final int y) {
    final double elevation = getElevation(x, y);
    return Double.isNaN(elevation);
  }

  default GriddedElevationModel newElevationModel(final BoundingBox boundingBox,
    final int gridCellSize) {
    final GeometryFactory geometryFactory = boundingBox.getGeometryFactory();
    final int minX = (int)boundingBox.getMinX();
    final int minY = (int)boundingBox.getMinY();
    final double width = boundingBox.getWidth();
    final double height = boundingBox.getHeight();

    final int modelWidth = (int)Math.ceil(width / gridCellSize);
    final int modelHeight = (int)Math.ceil(height / gridCellSize);
    final GriddedElevationModel elevationModel = newElevationModel(geometryFactory, minX, minY,
      modelWidth, modelHeight, gridCellSize);
    final int maxX = minX + modelWidth * gridCellSize;
    final int maxY = minY + modelHeight * gridCellSize;
    for (double y = minY; y < maxY; y += gridCellSize) {
      for (double x = minX; x < maxX; x += gridCellSize) {
        setElevation(elevationModel, x, y);
      }
    }
    return elevationModel;
  }

  default GriddedElevationModel newElevationModel(final double x, final double y, final int width,
    final int height) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final int gridCellSize = getGridCellSize();
    return newElevationModel(geometryFactory, x, y, width, height, gridCellSize);
  }

  GriddedElevationModel newElevationModel(GeometryFactory geometryFactory, double x, double y,
    int width, int height, int gridCellSize);

  default GriddedElevationModel resample(final int newGridCellSize) {
    final int tileX = (int)getMinX();
    final int tileY = (int)getMinY();
    final int gridCellSize = getGridCellSize();
    final double cellRatio = (double)gridCellSize / newGridCellSize;
    final int step = (int)Math.round(1 / cellRatio);
    final int gridWidth = getGridWidth();
    final int gridHeight = getGridHeight();

    final int newGridWidth = (int)Math.round(gridWidth * cellRatio);
    final int newGridHeight = (int)Math.round(gridHeight * cellRatio);

    final GeometryFactory geometryFactory = getGeometryFactory();
    final GriddedElevationModel newDem = new IntArrayScaleGriddedElevationModel(geometryFactory,
      tileX, tileY, newGridWidth, newGridHeight, newGridCellSize);

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
            final double elevation = getElevation(gridX, gridY);
            if (Double.isFinite(elevation)) {
              count++;
              sum += elevation;
            }
          }
        }
        if (count > 0) {
          final double elevation = geometryFactory.makeZPrecise(sum / count);
          newDem.setElevation(newGridX, newGridY, elevation);
        }
        newGridX++;
      }
      newGridY++;
    }
    return newDem;
  }

  void setBoundingBox(BoundingBox boundingBox);

  default void setElevation(final double x, final double y, final double elevation) {
    final int gridX = getGridCellX(x);
    final int gridY = getGridCellY(y);
    setElevation(gridX, gridY, elevation);
  }

  default void setElevation(final GriddedElevationModel elevationModel, final double x,
    final double y) {
    final int gridX = getGridCellX(x);
    final int gridY = getGridCellY(y);
    final double elevation = elevationModel.getElevation(x, y);
    setElevation(gridX, gridY, elevation);
  }

  void setElevation(final int gridX, final int gridY, final double elevation);

  default void setElevation(final int gridX, final int gridY,
    final GriddedElevationModel elevationModel, final double x, final double y) {
    final double elevation = elevationModel.getElevation(x, y);
    if (Double.isFinite(elevation)) {
      setElevation(gridX, gridY, elevation);
    }
  }

  default void setElevationNull(final int gridX, final int gridY) {
    setElevation(gridX, gridY, Double.NaN);
  }

  default void setElevations(final GriddedElevationModel elevationModel) {
    final double minX1 = elevationModel.getMinX();
    final double minY1 = elevationModel.getMinY();

    int startX = getGridCellX(minX1);
    if (startX < 0) {
      startX = 0;
    }
    int startY = getGridCellY(minY1);
    if (startY < 0) {
      startY = 0;
    }

    final int gridCellSize = getGridCellSize();
    final int minX = (int)getMinX() + startX * gridCellSize;
    final int minY = (int)getMinY() + startY * gridCellSize;

    double y = minY;
    final int gridWidth = getGridWidth();
    final int gridHeight = getGridHeight();
    for (int gridY = startY; gridY < gridHeight; gridY++) {
      double x = minX;
      for (int gridX = startX; gridX < gridWidth; gridX++) {
        setElevation(gridX, gridY, elevationModel, x, y);
        x += gridCellSize;
      }
      y += gridCellSize;
    }
  }

  default void setElevationsForTriangle(final double x1, final double y1, final double z1,
    final double x2, final double y2, final double z2, final double x3, final double y3,
    final double z3) {
    final double scaleXy = getScaleXY();
    double minX = x1;
    double maxX = x1;
    if (x2 < minX) {
      minX = x2;
    } else if (x2 > maxX) {
      maxX = x2;
    }
    if (x2 < minX) {
      minX = x2;
    } else if (x2 > maxX) {
      maxX = x2;
    }
    if (x3 < minX) {
      minX = x3;
    } else if (x3 > maxX) {
      maxX = x3;
    }

    double minY = y1;
    double maxY = y1;
    if (y2 < minY) {
      minY = y2;
    } else if (y2 > maxY) {
      maxY = y2;
    }
    if (y3 < minY) {
      minY = y3;
    } else if (y3 > maxY) {
      maxY = y3;
    }
    final int gridCellSize = getGridCellSize();
    final double gridMinX = getMinX();
    final double gridMaxX = getMaxX();
    final double startX;
    if (minX < gridMinX) {
      startX = gridMinX;
    } else {
      startX = Math.ceil(minX / gridCellSize) * gridCellSize;
    }
    if (maxX > gridMaxX) {
      maxX = gridMaxX;
    }
    final double gridMinY = getMinY();
    final double gridMaxY = getMaxY();
    final double startY;
    if (minY < gridMinY) {
      startY = gridMinY;
    } else {
      startY = Math.ceil(minY / gridCellSize) * gridCellSize;
    }
    if (maxY > gridMaxY) {
      maxY = gridMaxY;
    }
    for (double y = startY; y < maxY; y += gridCellSize) {
      for (double x = startX; x < maxX; x += gridCellSize) {
        if (Triangle.containsPoint(scaleXy, x1, y1, x2, y2, x3, y3, x, y)) {
          final double elevation = Triangle.getElevation(x1, y1, z1, x2, y2, z2, x3, y3, z3, x, y);
          if (Double.isFinite(elevation)) {
            setElevation(x, y, elevation);
          }
        }
      }
    }
  }

  default void setElevationsNull(final GriddedElevationModel elevationModel) {
    final double minX1 = elevationModel.getMinX();
    final double minY1 = elevationModel.getMinY();

    int startX = getGridCellX(minX1);
    if (startX < 0) {
      startX = 0;
    }
    int startY = getGridCellY(minY1);
    if (startY < 0) {
      startY = 0;
    }

    final int gridCellSize = getGridCellSize();
    final int minX = (int)getMinX() + startX * gridCellSize;
    final int minY = (int)getMinY() + startY * gridCellSize;

    double y = minY;
    final int gridWidth = getGridWidth();
    final int gridHeight = getGridHeight();
    for (int gridY = startY; gridY < gridHeight; gridY++) {
      double x = minX;
      for (int gridX = startX; gridX < gridWidth; gridX++) {
        final GriddedElevationModel elevationModel1 = elevationModel;
        final double elevation = elevationModel1.getElevation(x, y);
        if (Double.isFinite(elevation)) {
          setElevationNull(gridX, gridY);
        }
        x += gridCellSize;
      }
      y += gridCellSize;
    }
  }

  default void setElevationsNullFast(final Iterable<? extends Point> points) {
    for (final Point point : points) {
      final double x = point.getX();
      final double y = point.getY();
      final int gridX = getGridCellX(x);
      final int gridY = getGridCellY(y);
      setElevationNull(gridX, gridY);
    }
  }

  @SuppressWarnings("unchecked")
  default <G extends Geometry> G setGeometryElevations(final G geometry) {
    final GeometryEditor editor = geometry.newGeometryEditor(3);
    for (final Vertex vertex : geometry.vertices()) {
      final double x = vertex.getX();
      final double y = vertex.getY();
      final double elevation = getElevation(x, y);
      if (Double.isFinite(elevation)) {
        final int[] vertexId = vertex.getVertexId();
        editor.setZ(elevation, vertexId);
      }
    }
    return (G)editor.newGeometry();
  }

  void setResource(Resource resource);

  void updateZBoundingBox();

  default boolean writeGriddedElevationModel() {
    return writeGriddedElevationModel(MapEx.EMPTY);
  }

  default boolean writeGriddedElevationModel(final Map<String, ? extends Object> properties) {
    final Resource resource = getResource();
    if (resource == null) {
      return false;
    } else {
      writeGriddedElevationModel(resource, properties);
      return true;
    }
  }

  default void writeGriddedElevationModel(final Object target) {
    final Map<String, ? extends Object> properties = Collections.emptyMap();
    writeGriddedElevationModel(target, properties);
  }

  default void writeGriddedElevationModel(final Object target,
    final Map<String, ? extends Object> properties) {
    try (
      GriddedElevationModelWriter writer = GriddedElevationModelWriter
        .newGriddedElevationModelWriter(target, properties)) {
      if (writer == null) {
        throw new IllegalArgumentException("No elevation model writer exists for " + target);
      } else {
        writer.write(this);
      }
    }
  }
}
