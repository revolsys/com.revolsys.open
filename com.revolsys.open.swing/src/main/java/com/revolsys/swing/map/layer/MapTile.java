package com.revolsys.swing.map.layer;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.swing.map.layer.raster.GeoReferencedImage;

public abstract class MapTile {
  private final double resolution;

  private final Map<CoordinateSystem, GeoReferencedImage> projectedImages = new HashMap<CoordinateSystem, GeoReferencedImage>();

  private final BoundingBox boundingBox;

  private final int width;

  private final int height;

  public MapTile(final BoundingBox boundingBox, final int width,
    final int height, final double resolution) {
    this.boundingBox = boundingBox;
    this.width = width;
    this.height = height;
    this.resolution = resolution;
  }

  public BoundingBox getBoundingBox() {
    return boundingBox;
  }

  public GeometryFactory getGeometryFactory() {
    return boundingBox.getGeometryFactory();
  }

  public int getHeight() {
    return height;
  }

  public GeoReferencedImage getImage() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return getImage(geometryFactory);
  }

  public GeoReferencedImage getImage(final CoordinateSystem coordinateSystem) {
    final GeoReferencedImage projectedImage = projectedImages.get(coordinateSystem);
    return projectedImage;
  }

  public GeoReferencedImage getImage(final GeometryFactory geometryFactory) {
    final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();

    return getImage(coordinateSystem);
  }

  public double getResolution() {
    return resolution;
  }

  public int getWidth() {
    return width;
  }

  protected abstract BufferedImage loadBuffferedImage();

  protected GeoReferencedImage loadImage() {
    final BufferedImage bufferedImage = loadBuffferedImage();
    final BoundingBox boundingBox = getBoundingBox();
    return new GeoReferencedImage(boundingBox, bufferedImage);
  }

  public GeoReferencedImage loadImage(final CoordinateSystem coordinateSystem) {
    synchronized (projectedImages) {
      GeoReferencedImage projectedImage = projectedImages.get(coordinateSystem);
      if (projectedImage == null) {
        final GeometryFactory geometryFactory = getGeometryFactory();
        GeoReferencedImage image = getImage();
        if (image == null) {
          image = loadImage();
          projectedImages.put(geometryFactory.getCoordinateSystem(), image);
        }
        projectedImage = image.getImage(coordinateSystem, resolution);
        projectedImages.put(coordinateSystem, projectedImage);
      }
      return projectedImage;
    }
  }

  public GeoReferencedImage loadImage(final GeometryFactory geometryFactory) {
    final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
    return loadImage(coordinateSystem);
  }

}
