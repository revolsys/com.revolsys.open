package com.revolsys.elevation.gridded;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Map;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.geometry.model.editor.GeometryEditor;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.io.IoFactory;
import com.revolsys.properties.ObjectWithProperties;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Debug;

public interface GriddedElevationModel extends ObjectWithProperties, GeometryFactoryProxy {
  String GEOMETRY_FACTORY = "geometryFactory";

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
      final Resource resource = factory.getZipResource(source);
      final GriddedElevationModel dem = factory.newGriddedElevationModel(resource, properties);
      return dem;
    }
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

  BoundingBox getBoundingBox();

  BufferedImage getBufferedImage();

  default double getElevationDouble(final double x, final double y) {
    final int i = getGridCellX(x);
    final int j = getGridCellY(y);
    return getElevationDouble(i, j);
  }

  default double getElevationDouble(final int x, final int y) {
    return getElevationFloat(x, y);
  }

  default float getElevationFloat(final double x, final double y) {
    final int i = getGridCellX(x);
    final int j = getGridCellY(y);
    return getElevationFloat(i, j);
  }

  default float getElevationFloat(final int x, final int y) {
    final short elevation = getElevationShort(x, y);
    if (elevation == Short.MIN_VALUE) {
      return Float.NaN;
    } else {
      return elevation;
    }
  }

  default int getElevationInteger(final double x, final double y) {
    final int i = getGridCellX(x);
    final int j = getGridCellY(y);
    return getElevationInteger(i, j);
  }

  default int getElevationInteger(final int x, final int y) {
    return getElevationShort(x, y);
  }

  default long getElevationLong(final double x, final double y) {
    final int i = getGridCellX(x);
    final int j = getGridCellY(y);
    return getElevationLong(i, j);
  }

  default long getElevationLong(final int x, final int y) {
    return getElevationShort(x, y);
  }

  default short getElevationShort(final double x, final double y) {
    final int i = getGridCellX(x);
    final int j = getGridCellY(y);
    return getElevationShort(i, j);
  }

  short getElevationShort(int x, int y);

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

  GeoreferencedImage getImage();

  default double getMaxX() {
    final BoundingBox boundingBox = getBoundingBox();
    return boundingBox.getMaxX();
  }

  default double getMaxY() {
    final BoundingBox boundingBox = getBoundingBox();
    return boundingBox.getMaxY();
  }

  default double getMinX() {
    final BoundingBox boundingBox = getBoundingBox();
    return boundingBox.getMinX();
  }

  default double getMinY() {
    final BoundingBox boundingBox = getBoundingBox();
    return boundingBox.getMinY();
  }

  Resource getResource();

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

  boolean isNull(int x, int y);

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

  void setBoundingBox(BoundingBox boundingBox);

  default void setElevation(final double x, final double y, final double elevation) {
    final int gridX = getGridCellX(x);
    final int gridY = getGridCellY(y);
    setElevation(gridX, gridY, elevation);
  }

  default void setElevation(final double x, final double y, final float elevation) {
    final int gridX = getGridCellX(x);
    final int gridY = getGridCellY(y);
    setElevation(gridX, gridY, elevation);
  }

  default void setElevation(final double x, final double y, final int elevation) {
    final int gridX = getGridCellX(x);
    final int gridY = getGridCellY(y);
    setElevation(gridX, gridY, elevation);
  }

  default void setElevation(final double x, final double y, final long elevation) {
    final int gridX = getGridCellX(x);
    final int gridY = getGridCellY(y);
    setElevation(gridX, gridY, elevation);
  }

  void setElevation(GriddedElevationModel elevationModel, double x, double y);

  default void setElevation(final int gridX, final int gridY, final double elevation) {
    setElevation(gridX, gridY, (float)elevation);
  }

  default void setElevation(final int gridX, final int gridY, final float elevation) {
    setElevation(gridX, gridY, (short)elevation);
  }

  void setElevation(int gridX, int gridY, GriddedElevationModel elevationModel, double x, double y);

  default void setElevation(final int gridX, final int gridY, final int elevation) {
    setElevation(gridX, gridY, (short)elevation);
  }

  default void setElevation(final int gridX, final int gridY, final long elevation) {
    setElevation(gridX, gridY, (short)elevation);
  }

  void setElevation(int x, int y, short elevation);

  void setElevationNull(int x, int y);

  default void setElevations(final GriddedElevationModel elevationModel) {
    final int gridCellSize = getGridCellSize();
    final int minX = (int)getMinX();
    final int minY = (int)getMinY();

    double y = minY;
    final int width = getGridWidth();
    final int height = getGridHeight();
    for (int gridY = 0; gridY < height; gridY++) {
      double x = minX;
      for (int gridX = 0; gridX < width; gridX++) {
        setElevation(gridX, gridY, elevationModel, x, y);
        x += gridCellSize;
      }
      y += gridCellSize;
    }
  }

  @SuppressWarnings("unchecked")
  default <G extends Geometry> G setGeometryElevations(final G geometry) {
    final GeometryEditor editor = geometry.newGeometryEditor();
    editor.setAxisCount(3);
    for (final Vertex vertex : geometry.vertices()) {
      final double x = vertex.getX();
      final double y = vertex.getY();
      final double elevation = getElevationDouble(x, y);
      if (Double.isNaN(elevation)) {
        Debug.noOp();
      } else {
        final int[] vertexId = vertex.getVertexId();
        editor.setZ(elevation, vertexId);
      }
    }
    return (G)editor.newGeometry();
  }

  void setResource(Resource resource);

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
      }
      writer.write(this);
    }
  }
}
