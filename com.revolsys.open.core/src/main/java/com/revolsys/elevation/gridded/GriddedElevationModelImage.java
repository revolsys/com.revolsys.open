package com.revolsys.elevation.gridded;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;

import com.revolsys.elevation.gridded.rasterizer.GriddedElevationModelRasterizer;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.raster.AbstractGeoreferencedImage;

public class GriddedElevationModelImage extends AbstractGeoreferencedImage {
  private DataBuffer imageBuffer;

  private GriddedElevationModelRasterizer rasterizer;

  private GriddedElevationModel elevationModel;

  public GriddedElevationModelImage(final GriddedElevationModelRasterizer rasterizer) {
    this.rasterizer = rasterizer;
    final GriddedElevationModel elevationModel = rasterizer.getElevationModel();
    setElevationModel(elevationModel);

  }

  @Override
  public BufferedImage getBufferedImage() {
    synchronized (this) {
      if (this.imageBuffer == null) {
        redraw();
      }
    }
    return super.getBufferedImage();
  }

  public GriddedElevationModel getElevationModel() {
    return this.elevationModel;
  }

  public boolean hasImage() {
    return this.imageBuffer != null;
  }

  public void redraw() {
    synchronized (this) {
      if (this.imageBuffer == null) {
        final int width = this.elevationModel.getGridWidth();
        final int height = this.elevationModel.getGridHeight();
        final ColorModel colorModel = ColorModel.getRGBdefault();
        final DataBuffer imageBuffer = new TempFileMappedIntDataBuffer(width, height);

        final SampleModel sampleModel = new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT, width,
          height,
          new int[] { //
            0x00ff0000, // Red
            0x0000ff00, // Green
            0x000000ff, // Blue
            0xff000000 // Alpha
          });

        final WritableRaster raster = new IntegerRaster(sampleModel, imageBuffer);
        final BufferedImage image = new BufferedImage(colorModel, raster, false, null);

        setRenderedImage(image);
        this.rasterizer.rasterize(imageBuffer);
        this.imageBuffer = imageBuffer;
      } else {
        this.rasterizer.rasterize(this.imageBuffer);
      }
    }
  }

  public void setElevationModel(final GriddedElevationModel elevationModel) {
    this.elevationModel = elevationModel;
    this.rasterizer.setElevationModel(elevationModel);
    final BoundingBox boundingBox = elevationModel.getBoundingBox();
    setBoundingBox(boundingBox);

    final int width = elevationModel.getGridWidth();
    setImageWidth(width);

    final int height = elevationModel.getGridHeight();
    setImageHeight(height);
  }

  public void setRasterizer(final GriddedElevationModelRasterizer rasterizer) {
    this.rasterizer = rasterizer;
    redraw();
  }
}
