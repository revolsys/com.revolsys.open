package com.revolsys.elevation.gridded;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Map;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.cs.esri.EsriCoordinateSystems;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.IoFactory;
import com.revolsys.properties.ObjectWithProperties;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.spring.resource.Resource;

public interface GriddedElevationModel extends ObjectWithProperties {
  String GEOMETRY_FACTORY = "geometryFactory";

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
    final int width = getWidth();
    final int height = getHeight();
    if (width > 0 && height > 0) {
      return (double)width / height;
    } else {
      return 0;
    }
  }

  BoundingBox getBoundingBox();

  BufferedImage getBufferedImage();

  int getCellSize();

  default int getCellX(final double x) {
    final double minX = getMinX();
    final int cellSize = getCellSize();
    final double deltaX = x - minX;
    final double cellDiv = deltaX / cellSize;
    final int cellX = (int)Math.floor(cellDiv);
    return cellX;
  }

  default int getCellY(final double y) {
    final double maxY = getMaxY();
    final int cellSize = getCellSize();
    final int cellY = (int)Math.floor((maxY - y) / cellSize);
    return cellY;
  }

  default double getElevationDouble(final double x, final double y) {
    final int i = getCellX(x);
    final int j = getCellY(y);
    return getElevationDouble(i, j);
  }

  default double getElevationDouble(final int x, final int y) {
    return getElevationFloat(x, y);
  }

  default float getElevationFloat(final double x, final double y) {
    final int i = getCellX(x);
    final int j = getCellY(y);
    return getElevationFloat(i, j);
  }

  default float getElevationFloat(final int x, final int y) {
    return getElevationShort(x, y);
  }

  default int getElevationInteger(final double x, final double y) {
    final int i = getCellX(x);
    final int j = getCellY(y);
    return getElevationInteger(i, j);
  }

  default int getElevationInteger(final int x, final int y) {
    return getElevationShort(x, y);
  }

  default long getElevationLong(final double x, final double y) {
    final int i = getCellX(x);
    final int j = getCellY(y);
    return getElevationLong(i, j);
  }

  default long getElevationLong(final int x, final int y) {
    return getElevationShort(x, y);
  }

  default short getElevationShort(final double x, final double y) {
    final int i = getCellX(x);
    final int j = getCellY(y);
    return getElevationShort(i, j);
  }

  short getElevationShort(int x, int y);

  default GeometryFactory getGeometryFactory() {
    final BoundingBox boundingBox = getBoundingBox();
    return boundingBox.getGeometryFactory();
  }

  int getHeight();

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

  int getWidth();

  default double getX(final int i) {
    final double minX = getMinX();
    final int cellSize = getCellSize();
    return minX + i * cellSize;
  }

  default double getY(final int i) {
    final double maxY = getMaxY();
    final int cellSize = getCellSize();
    return maxY - i * cellSize;
  }

  boolean isEmpty();

  default boolean isNull(final double x, final double y) {
    final int i = getCellX(x);
    final int j = getCellY(y);
    return isNull(i, j);
  }

  boolean isNull(int x, int y);

  default GriddedElevationModel newElevationModel(final BoundingBox boundingBox,
    final int cellSize) {
    final GeometryFactory geometryFactory = boundingBox.getGeometryFactory();
    final int minX = (int)boundingBox.getMinX();
    final int minY = (int)boundingBox.getMinY();
    final double width = boundingBox.getWidth();
    final double height = boundingBox.getHeight();

    final int modelWidth = (int)Math.ceil(width / cellSize);
    final int modelHeight = (int)Math.ceil(height / cellSize);
    final GriddedElevationModel elevationModel = newElevationModel(geometryFactory, minX, minY,
      modelWidth, modelHeight, cellSize);
    final int maxX = minX + modelWidth * cellSize;
    final int maxY = minY + modelHeight * cellSize;
    for (double y = minY; y < maxY; y += cellSize) {
      for (double x = minX; x < maxX; x += cellSize) {
        setElevation(elevationModel, x, y);
      }
    }
    return elevationModel;
  }

  default GriddedElevationModel newElevationModel(final double x, final double y, final int width,
    final int height) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final int cellSize = getCellSize();
    return newElevationModel(geometryFactory, x, y, width, height, cellSize);
  }

  GriddedElevationModel newElevationModel(GeometryFactory geometryFactory, double x, double y,
    int width, int height, int cellSize);

  void setBoundingBox(BoundingBox boundingBox);

  default void setElevation(final double x, final double y, final double elevation) {
    final int cellX = getCellX(x);
    final int cellY = getCellX(x);
    setElevation(cellX, cellY, elevation);
  }

  default void setElevation(final double x, final double y, final float elevation) {
    final int cellX = getCellX(x);
    final int cellY = getCellX(x);
    setElevation(cellX, cellY, elevation);
  }

  default void setElevation(final double x, final double y, final int elevation) {
    final int cellX = getCellX(x);
    final int cellY = getCellX(x);
    setElevation(cellX, cellY, elevation);
  }

  default void setElevation(final double x, final double y, final long elevation) {
    final int cellX = getCellX(x);
    final int cellY = getCellX(x);
    setElevation(cellX, cellY, elevation);
  }

  void setElevation(GriddedElevationModel elevationModel, double x, double y);

  default void setElevation(final int cellX, final int cellY, final double elevation) {
    setElevation(cellX, cellY, (float)elevation);
  }

  default void setElevation(final int cellX, final int cellY, final float elevation) {
    setElevation(cellX, cellY, (short)elevation);
  }

  void setElevation(int cellX, int cellY, GriddedElevationModel elevationModel, double x, double y);

  default void setElevation(final int cellX, final int cellY, final int elevation) {
    setElevation(cellX, cellY, (short)elevation);
  }

  default void setElevation(final int cellX, final int cellY, final long elevation) {
    setElevation(cellX, cellY, (short)elevation);
  }

  void setElevation(int x, int y, short elevation);

  void setElevationNull(int x, int y);

  default void setElevations(final GriddedElevationModel elevationModel) {
    final int cellSize = getCellSize();
    final int minX = (int)getMinX();
    final int minY = (int)getMinY();

    double y = minY;
    final int width = getWidth();
    final int height = getHeight();
    for (int cellY = height - 1; cellY >= 0; cellY--) {
      double x = minX;
      for (int cellX = 0; cellX < width; cellX++) {
        setElevation(cellX, cellY, elevationModel, x, y);
        x += cellSize;
      }
      y += cellSize;
    }
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
      writer.write(this);
    }
    final GeometryFactory geometryFactory = getGeometryFactory();
    EsriCoordinateSystems.writePrjFile(target, geometryFactory);
  }
}
