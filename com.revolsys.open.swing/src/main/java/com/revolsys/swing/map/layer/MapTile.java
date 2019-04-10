package com.revolsys.swing.map.layer;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.raster.BufferedGeoreferencedImage;
import com.revolsys.raster.GeoreferencedImage;

public abstract class MapTile implements GeometryFactoryProxy {
  private final BoundingBox boundingBox;

  private final int height;

  private final double resolution;

  private final int width;

  private GeoreferencedImage image;

  private final Map<CoordinateSystem, GeoreferencedImage> projectedImages = new HashMap<>();

  private final GeometryFactory geometryFactory;

  public MapTile(final BoundingBox boundingBox, final int width, final int height,
    final double resolution) {
    this.boundingBox = boundingBox;
    this.geometryFactory = boundingBox.getGeometryFactory();
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

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.boundingBox.getGeometryFactory();
  }

  public int getHeight() {
    return this.height;
  }

  public GeoreferencedImage getImage() {
    return this.image;
  }

  public GeoreferencedImage getImage(final GeometryFactory geometryFactory,
    final double resolution) {
    if (resolution > 500 && this.geometryFactory.isProjectionRequired(geometryFactory)) {
      final CoordinateSystem coordinateSystem = geometryFactory.getHorizontalCoordinateSystem();
      return this.projectedImages.get(coordinateSystem);
    } else {
      return getImage();
    }
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

  public GeoreferencedImage loadImage(final GeometryFactory geometryFactory,
    final double resolution) {
    synchronized (this.projectedImages) {
      if (this.image == null) {
        this.image = loadImage();
      }
    }
    if (this.image != null && this.geometryFactory.isProjectionRequired(geometryFactory)) {
      final CoordinateSystem coordinateSystem = geometryFactory.getHorizontalCoordinateSystem();
      synchronized (this.projectedImages) {
        GeoreferencedImage projectedImage = this.projectedImages.get(coordinateSystem);
        if (projectedImage == null) {
          if (resolution > 500) {
            projectedImage = this.image.getImage(geometryFactory, getResolution());
          } else {
            projectedImage = this.image;
          }
          this.projectedImages.put(coordinateSystem, projectedImage);
        }
        return projectedImage;
      }
    } else {
      return this.image;
    }
  }

}
