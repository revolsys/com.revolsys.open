package com.revolsys.swing.map.layer;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.raster.BufferedGeoReferencedImage;
import com.revolsys.raster.GeoReferencedImage;

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

  public GeoReferencedImage getImage() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return getImage(geometryFactory);
  }

  public GeoReferencedImage getImage(final CoordinateSystem coordinateSystem) {
    final GeoReferencedImage projectedImage = this.projectedImages.get(coordinateSystem);
    return projectedImage;
  }

  public GeoReferencedImage getImage(final GeometryFactory geometryFactory) {
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

  protected GeoReferencedImage loadImage() {
    final BufferedImage bufferedImage = loadBuffferedImage();
    if (bufferedImage == null) {
      return null;
    } else {
      final BoundingBox boundingBox = getBoundingBox();
      return new BufferedGeoReferencedImage(boundingBox, bufferedImage);
    }
  }

  public GeoReferencedImage loadImage(final CoordinateSystem coordinateSystem) {
    synchronized (this.projectedImages) {
      GeoReferencedImage projectedImage = this.projectedImages.get(coordinateSystem);
      if (projectedImage == null) {
        final GeometryFactory geometryFactory = getGeometryFactory();
        GeoReferencedImage image = getImage();
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

  public GeoReferencedImage loadImage(final GeometryFactory geometryFactory) {
    final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
    return loadImage(coordinateSystem);
  }

}
