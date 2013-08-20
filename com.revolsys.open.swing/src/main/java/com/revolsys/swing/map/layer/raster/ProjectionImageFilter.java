package com.revolsys.swing.map.layer.raster;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.jhlabs.image.WholeImageFilter;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.projection.CoordinatesOperation;
import com.revolsys.gis.cs.projection.ProjectionFactory;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;

public class ProjectionImageFilter extends WholeImageFilter {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private final BoundingBox sourceBoundingBox;

  private final BoundingBox destBoundingBox;

  private final double destPixelSize;

  private final int destWidth;

  private final int destHeight;

  public ProjectionImageFilter(final BoundingBox imageBoundingBox,
    final CoordinateSystem destCoordinateSystem, final double resolution) {
    this(imageBoundingBox, GeometryFactory.getFactory(destCoordinateSystem),
      resolution);
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
    final BufferedImage dest = new BufferedImage(this.destWidth,
      this.destHeight, BufferedImage.TYPE_INT_ARGB);
    return super.filter(source, dest);
  }

  @Override
  protected int[] filterPixels(final int imageWidth, final int imageHeight,
    final int[] inPixels, final Rectangle transformedSpace) {
    final int[] outPixels = new int[transformedSpace.width
      * transformedSpace.height];

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

    final CoordinatesOperation operation = ProjectionFactory.getCoordinatesOperation(
      destGeometryFactory, sourceGeometryFactory);
    if (operation == null) {
      return inPixels;
    }
    final Coordinates source = new DoubleCoordinates(2);
    final Coordinates dest = new DoubleCoordinates(2);
    for (int i = 0; i < newImageWidth; i++) {
      final double newImageX = newMinX + i * this.destPixelSize;
      dest.setX(newImageX);
      for (int j = 0; j < newImageHeight; j++) {
        final double newImageY = newMaxY - j * this.destPixelSize;
        dest.setY(newImageY);
        operation.perform(dest, source);
        final double imageX = source.getX();
        final double imageY = source.getY();
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
