package com.revolsys.elevation.gridded;

import java.awt.image.BufferedImage;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.raster.AbstractGeoreferencedImage;

public class GriddedElevationModelImage extends AbstractGeoreferencedImage {
  public GriddedElevationModelImage(final GriddedElevationModel elevationModel) {
    final BoundingBox boundingBox = elevationModel.getBoundingBox();
    setBoundingBox(boundingBox);

    final int width = elevationModel.getWidth();
    setImageWidth(width);

    final int height = elevationModel.getHeight();
    setImageHeight(height);

    final BufferedImage image = elevationModel.getBufferedImage();
    setRenderedImage(image);
  }
}
