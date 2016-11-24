package com.revolsys.elevation.gridded;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.util.Collections;
import java.util.Map;

import com.revolsys.awt.WebColors;
import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.gridded.compactbinary.CompactBinaryGriddedElevation;
import com.revolsys.elevation.gridded.esriascii.EsriAsciiGriddedElevation;
import com.revolsys.elevation.gridded.usgsdem.UsgsGriddedElevation;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.editor.GeometryEditor;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.io.IoFactory;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.properties.ObjectWithProperties;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Debug;

public interface GriddedElevationModel extends ObjectWithProperties, GeometryFactoryProxy {
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

  public static void ioFactoryInit() {
    IoFactoryRegistry.addFactory(new CompactBinaryGriddedElevation());
    IoFactoryRegistry.addFactory(new EsriAsciiGriddedElevation());
    IoFactoryRegistry.addFactory(new UsgsGriddedElevation());
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

  default int getColour(final int gridX, final int gridY) {
    throw new UnsupportedOperationException();
  }

  default double getElevation(final double x, final double y) {
    final int i = getGridCellX(x);
    final int j = getGridCellY(y);
    return getElevation(i, j);
  }

  double getElevation(final int x, final int y);

  default double getElevation(Point point) {
    point = convertGeometry(point);
    final double x = point.getX();
    final double y = point.getY();
    return getElevation(x, y);
  }

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

  default BufferedImage newBufferedImage() {
    getBoundingBox();
    final ColorModel colorModel = ColorModel.getRGBdefault();
    final DataBuffer imageBuffer = new GriddedElevationModelDataBuffer(this);
    final int width = getGridWidth();
    final int height = getGridHeight();

    final SampleModel sampleModel = new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT, width,
      height,
      new int[] { //
        0x00ff0000, // Red
        0x0000ff00, // Green
        0x000000ff, // Blue
        0xff000000 // Alpha
      });

    final WritableRaster raster = new IntegerRaster(sampleModel, imageBuffer);
    return new BufferedImage(colorModel, raster, false, null);
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
    if (!elevationModel.isNull(x, y)) {
      final double elevation = elevationModel.getElevation(x, y);
      setElevation(gridX, gridY, elevation);
    }
  }

  default void setElevationNull(final int gridX, final int gridY) {
    setElevation(gridX, gridY, Double.NaN);
  }

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
      final double elevation = getElevation(x, y);
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
      } else {
        writer.write(this);
      }
    }
  }
}
