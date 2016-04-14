package com.revolsys.gis.elevation.gridded;

import java.util.Collections;
import java.util.Map;

import com.revolsys.geometry.cs.esri.EsriCoordinateSystems;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.IoFactory;
import com.revolsys.properties.ObjectWithProperties;
import com.revolsys.spring.resource.Resource;

public interface GriddedElevationModel extends ObjectWithProperties {
  String GEOMETRY_FACTORY = "geometryFactory";

  static GriddedElevationModel newGriddedElevationModel(final Object source) {
    final Map<String, Object> properties = Collections.emptyMap();
    return newGriddedElevationModel(source, properties);
  }

  static GriddedElevationModel newGriddedElevationModel(final Object source,
    final Map<String, ? extends Object> properties) {
    final GriddedElevationModelFactory factory = IoFactory
      .factory(GriddedElevationModelFactory.class, source);
    if (factory == null) {
      return null;
    } else {
      final Resource resource = Resource.getResource(source);
      final GriddedElevationModel dem = factory.newGriddedElevationModel(resource, properties);
      return dem;
    }
  }

  void clear();

  BoundingBox getBoundingBox();

  int getCellSize();

  default int getCellX(final double x) {
    final double minX = getMinX();
    final int cellSize = getCellSize();
    double deltaX = x - minX;
    double cellDiv = deltaX / cellSize;
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

  default boolean isNull(final double x, final double y) {
    final int i = getCellX(x);
    final int j = getCellY(y);
    return isNull(i, j);
  }

  boolean isNull(int x, int y);

  void setBoundingBox(BoundingBox boundingBox);

  default void setElevation(final int x, final int y, final double elevation) {
    setElevation(x, y, (short)elevation);
  }

  default void setElevation(final int x, final int y, final float elevation) {
    setElevation(x, y, (short)elevation);
  }

  default void setElevation(final int x, final int y, final int elevation) {
    setElevation(x, y, (short)elevation);
  }

  default void setElevation(final int x, final int y, final long elevation) {
    setElevation(x, y, (short)elevation);
  }

  void setElevation(int x, int y, short elevation);

  void setElevationNull(int x, int y);

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
