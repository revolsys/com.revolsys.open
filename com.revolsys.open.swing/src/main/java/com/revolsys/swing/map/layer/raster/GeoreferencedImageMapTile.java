package com.revolsys.swing.map.layer.raster;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.raster.BufferedGeoreferencedImage;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.swing.map.layer.tile.AbstractMapTile;

public abstract class GeoreferencedImageMapTile extends AbstractMapTile<GeoreferencedImage> {

  private final Map<CoordinateSystem, GeoreferencedImage> projectedImages = new HashMap<>();

  public GeoreferencedImageMapTile(final BoundingBox boundingBox, final int width, final int height,
    final double resolution) {
    super(boundingBox, width, height, resolution);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof GeoreferencedImageMapTile) {
      return super.equals(obj);
    }
    return false;
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

  protected abstract BufferedImage loadBuffferedImage();

  @Override
  protected GeoreferencedImage loadData() {
    final BufferedImage bufferedImage = loadBuffferedImage();
    if (bufferedImage == null) {
      return null;
    } else {
      final BoundingBox boundingBox = getBoundingBox();
      return new BufferedGeoreferencedImage(boundingBox, bufferedImage);
    }
  }

  protected GeoreferencedImage loadData(final CoordinateSystem coordinateSystem) {
    synchronized (this.projectedImages) {
      GeoreferencedImage projectedImage = this.projectedImages.get(coordinateSystem);
      if (projectedImage == null) {
        final GeometryFactory geometryFactory = getGeometryFactory();
        GeoreferencedImage image = getData();
        if (image == null) {
          image = loadData();
          this.projectedImages.put(geometryFactory.getCoordinateSystem(), image);
        }
        if (image != null) {
          projectedImage = image.getImage(coordinateSystem, getResolution());
          this.projectedImages.put(coordinateSystem, projectedImage);
        }
      }
      return projectedImage;
    }
  }

  @Override
  public GeoreferencedImage loadData(final GeometryFactory geometryFactory) {
    final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
    return loadData(coordinateSystem);
  }

}
