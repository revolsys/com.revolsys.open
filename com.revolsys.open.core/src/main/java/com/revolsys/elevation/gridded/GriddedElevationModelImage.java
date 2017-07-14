package com.revolsys.elevation.gridded;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.raster.AbstractGeoreferencedImage;

public class GriddedElevationModelImage extends AbstractGeoreferencedImage {
  private final TempFileMappedIntDataBuffer imageBuffer;

  private final int width;

  private final int height;

  public GriddedElevationModelImage(final GriddedElevationModel elevationModel) {
    final BoundingBox boundingBox = elevationModel.getBoundingBox();
    setBoundingBox(boundingBox);

    this.width = elevationModel.getGridWidth();
    setImageWidth(this.width);

    this.height = elevationModel.getGridHeight();
    setImageHeight(this.height);

    final ColorModel colorModel = ColorModel.getRGBdefault();
    this.imageBuffer = new TempFileMappedIntDataBuffer(this.width, this.height);

    final SampleModel sampleModel = new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT,
      this.width, this.height, new int[] { //
        0x00ff0000, // Red
        0x0000ff00, // Green
        0x000000ff, // Blue
        0xff000000 // Alpha
      });

    final WritableRaster raster = new IntegerRaster(sampleModel, this.imageBuffer);
    final BufferedImage image = new BufferedImage(colorModel, raster, false, null);

    setRenderedImage(image);
  }

  public void refresh(final GriddedElevationModel elevationModel) {
    int index = 0;
    for (int y = this.height - 1; y >= 0; y--) {
      for (int x = 0; x < this.width; x++) {
        final int hillShade = elevationModel.getColour(x, y);
        this.imageBuffer.setElem(index, hillShade);
        index++;
      }
    }
  }

  public void refresh(final HillShadeConfiguration hillShadeConfiguration) {
    int index = 0;
    for (int y = this.height - 1; y >= 0; y--) {
      for (int x = 0; x < this.width; x++) {
        final int hillShade = hillShadeConfiguration.getHillShade(index);
        this.imageBuffer.setElem(index, hillShade);
        index++;
      }
    }
  }
}
