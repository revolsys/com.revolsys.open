package com.revolsys.raster;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.projection.CoordinatesOperation;
import com.revolsys.geometry.cs.projection.ProjectionFactory;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;

public class ProjectionImageFilter extends WholeImageFilter {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final BoundingBox destBoundingBox;

  private final int destHeight;

  private final double destPixelSize;

  private final int destWidth;

  private final BoundingBox sourceBoundingBox;

  public ProjectionImageFilter(final BoundingBox imageBoundingBox,
    final CoordinateSystem destCoordinateSystem, final double resolution) {
    this(imageBoundingBox, destCoordinateSystem.getGeometryFactory(), resolution);
  }

  public ProjectionImageFilter(final BoundingBox sourceBoundingBox,
    final GeometryFactory destGeometryFactory, final double destPixelSize) {
    this.sourceBoundingBox = sourceBoundingBox;
    this.destBoundingBox = sourceBoundingBox.convert(destGeometryFactory);
    this.destPixelSize = destPixelSize;
    final double width = this.destBoundingBox.getWidth();
    this.destWidth = (int)(width / destPixelSize);

    final double height = this.destBoundingBox.getHeight();
    this.destHeight = (int)(height / destPixelSize);
  }

  public BufferedImage filter(final BufferedImage source) {
    if (this.destWidth < 1 || this.destHeight < 1) {
      return source;
    } else {
      final BufferedImage dest = new BufferedImage(this.destWidth, this.destHeight,
        BufferedImage.TYPE_INT_ARGB);
      return super.filter(source, dest);
    }
  }

  @Override
  protected int[] filterPixels(final int imageWidth, final int imageHeight, final int[] inPixels,
    final Rectangle transformedSpace) {
    final int[] outPixels = new int[transformedSpace.width * transformedSpace.height];

    final double minX = this.sourceBoundingBox.getMinX();
    final double minY = this.sourceBoundingBox.getMinY();
    final double width = this.sourceBoundingBox.getWidth();
    final double height = this.sourceBoundingBox.getHeight();
    final double pixelWidth = width / imageWidth;
    final double pixelHeight = height / imageHeight;

    final double newMinX = this.destBoundingBox.getMinX();
    final double newMaxY = this.destBoundingBox.getMaxY();

    final int newImageWidth = transformedSpace.width;
    final int newImageHeight = transformedSpace.height;
    final GeometryFactory sourceGeometryFactory = this.sourceBoundingBox.getGeometryFactory();
    final GeometryFactory destGeometryFactory = this.destBoundingBox.getGeometryFactory();

    final CoordinatesOperation operation = ProjectionFactory
      .getCoordinatesOperation(destGeometryFactory, sourceGeometryFactory);
    if (operation == null) {
      return inPixels;
    }
    final double[] source = new double[2];
    final double[] dest = new double[2];
    for (int i = 0; i < newImageWidth; i++) {
      final double newImageX = newMinX + i * this.destPixelSize;
      dest[0] = newImageX;
      for (int j = 0; j < newImageHeight; j++) {
        final double newImageY = newMaxY - j * this.destPixelSize;
        dest[1] = newImageY;
        operation.perform(2, dest, 2, source);
        final double imageX = source[0];
        final double imageY = source[1];
        final int imageI = (int)((imageX - minX) / pixelWidth);
        final int imageJ = imageHeight - (int)((imageY - minY) / pixelHeight);
        if (imageI > -1 && imageI < imageWidth) {
          if (imageJ > -1 && imageJ < imageHeight) {
            final int rgb = inPixels[imageJ * imageWidth + imageI];
            if (rgb != -1) {
              outPixels[j * newImageWidth + i] = rgb;
              // // TODO better interpolation
            }
          }
        }
      }
    }
    return outPixels;
  }

  public BoundingBox getDestBoundingBox() {
    return this.destBoundingBox;
  }

  public int getDestHeight() {
    return this.destHeight;
  }

  public int getDestWidth() {
    return this.destWidth;
  }

  @Override
  protected void transformSpace(final Rectangle rect) {
    super.transformSpace(rect);
    rect.width = this.destWidth;
    rect.height = this.destHeight;
  }

}
