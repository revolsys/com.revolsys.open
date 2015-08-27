package com.revolsys.swing.map.layer;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.raster.BufferedGeoreferencedImage;
import com.revolsys.raster.GeoreferencedImage;

public abstract class MapTile {
  private final BoundingBox boundingBox;

  private final int height;

  private final Map<CoordinateSystem, GeoreferencedImage> projectedImages = new HashMap<CoordinateSystem, GeoreferencedImage>();

  private final double resolution;

  private final int width;

  public MapTile(final BoundingBox boundingBox, final int width, final int height,
    final double resolution) {
    this.boundingBox = boundingBox;
    this.width = width;
    this.height = height;
    this.resolution = resolution;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof MapTile) {
      final MapTile tile = (MapTile)obj;
      return tile.getBoundingBox().equals(this.boundingBox);
    }
    return false;
  }

  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  public GeometryFactory getGeometryFactory() {
    return this.boundingBox.getGeometryFactory();
  }

  public int getHeight() {
    return this.height;
  }

  public GeoreferencedImage getImage() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return getImage(geometryFactory);
  }

  public GeoreferencedImage getImage(final CoordinateSystem coordinateSystem) {
    final GeoreferencedImage projectedImage = this.projectedImages.get(coordinateSystem);
    return projectedImage;
  }

  public GeoreferencedImage getImage(final GeometryFactory geometryFactory) {
    final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();

    return getImage(coordinateSystem);
  }

  public double getResolution() {
    return this.resolution;
  }

  public int getWidth() {
    return this.width;
  }

  protected abstract BufferedImage loadBuffferedImage();

  protected GeoreferencedImage loadImage() {
    final BufferedImage bufferedImage = loadBuffferedImage();
    if (bufferedImage == null) {
      return null;
    } else {
      final BoundingBox boundingBox = getBoundingBox();
      return new BufferedGeoreferencedImage(boundingBox, bufferedImage);
    }
  }

  public GeoreferencedImage loadImage(final CoordinateSystem coordinateSystem) {
    synchronized (this.projectedImages) {
      GeoreferencedImage projectedImage = this.projectedImages.get(coordinateSystem);
      if (projectedImage == null) {
        final GeometryFactory geometryFactory = getGeometryFactory();
        GeoreferencedImage image = getImage();
        if (image == null) {
          image = loadImage();
          this.projectedImages.put(geometryFactory.getCoordinateSystem(), image);
        }
        if (image != null) {
          projectedImage = image.getImage(coordinateSystem, this.resolution);
          this.projectedImages.put(coordinateSystem, projectedImage);
        }
      }
      return projectedImage;
    }
  }

  public GeoreferencedImage loadImage(final GeometryFactory geometryFactory) {
    final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
    return loadImage(coordinateSystem);
  }

}
