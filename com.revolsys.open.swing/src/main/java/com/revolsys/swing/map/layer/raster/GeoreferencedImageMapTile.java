package com.revolsys.swing.map.layer.raster;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import org.jeometry.coordinatesystem.model.CoordinateSystem;

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

  public GeoreferencedImage getImage(final GeometryFactory geometryFactory,
    final double resolution) {
    if (resolution > 500) {
      final CoordinateSystem coordinateSystem = geometryFactory.getHorizontalCoordinateSystem();
      return this.projectedImages.get(coordinateSystem);
    } else {
      return getData();
    }
  }

  protected abstract BufferedImage loadBuffferedImage();

  @Override
  public GeoreferencedImage loadData(final GeometryFactory geometryFactory,
    final double resolution) {
    final CoordinateSystem coordinateSystem = geometryFactory.getHorizontalCoordinateSystem();
    synchronized (this.projectedImages) {
      GeoreferencedImage projectedImage = this.projectedImages.get(coordinateSystem);
      if (projectedImage == null) {
        final GeometryFactory geometryFactoryThis = getGeometryFactory();
        GeoreferencedImage image = getData();
        if (image == null) {
          image = loadData();
          this.projectedImages.put(geometryFactoryThis.getHorizontalCoordinateSystem(), image);
        }
        if (image != null) {
          if (resolution > 500) {
            projectedImage = image.getImage(geometryFactory, getResolution());
          } else {
            projectedImage = image;
          }
          this.projectedImages.put(coordinateSystem, projectedImage);
        }
      }
      return projectedImage;
    }
  }

  @Override
  protected GeoreferencedImage loadDataDo() {
    final BufferedImage bufferedImage = loadBuffferedImage();
    if (bufferedImage == null) {
      return null;
    } else {
      final BoundingBox boundingBox = getBoundingBox();
      final BufferedGeoreferencedImage image = new BufferedGeoreferencedImage(boundingBox,
        bufferedImage);
      image.addTiePointsForBoundingBox();
      return image;
    }
  }

}
