package com.revolsys.swing.map.layer.raster.filter;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;

import com.jhlabs.image.WholeImageFilter;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Point;

/**
 * Polynomial warp that maintains the same image size and bounding box.
 */
public class WarpPolynomialFilter extends WholeImageFilter {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private final BoundingBox boundingBox;

  private int degree;

  public WarpPolynomialFilter(final BoundingBox boundingBox,
    final CoordinatesList imagePoints, final List<Point> destinationPoints) {
    this.boundingBox = boundingBox;
  }

  public BufferedImage filter(final BufferedImage source) {
    final int width = source.getWidth();
    final int height = source.getHeight();
    final BufferedImage dest = new BufferedImage(width, height,
      BufferedImage.TYPE_INT_ARGB);
    return super.filter(source, dest);
  }

  @Override
  protected int[] filterPixels(final int imageWidth, final int imageHeight,
    final int[] inPixels, final Rectangle transformedSpace) {
    final int[] outPixels = new int[transformedSpace.width
      * transformedSpace.height];

    final double minX = this.boundingBox.getMinX();
    final double minY = this.boundingBox.getMaxY();
    final double maxY = this.boundingBox.getMaxY();
    final double pixelWidth = this.boundingBox.getWidth() / imageWidth;
    final double pixelHeight = this.boundingBox.getHeight() / imageHeight;

    final Coordinates dest = new DoubleCoordinates(2);
    for (int i = 0; i < imageWidth; i++) {
      final double destX = minX + i * pixelWidth;
      dest.setX(destX);
      for (int j = 0; j < imageHeight; j++) {
        final double destY = maxY - j * pixelHeight;
        dest.setY(destY);
        final Coordinates source = toSourcePoint(destX, destY);

        final double imageX = source.getX();
        final double imageY = source.getY();
        final int imageI = (int)((imageX - minX) / pixelWidth);
        final int imageJ = imageHeight - (int)((imageY - minY) / pixelHeight);
        if (imageI > -1 && imageI < imageWidth) {
          if (imageJ > -1 && imageJ < imageHeight) {
            final int rgb = inPixels[imageJ * imageWidth + imageI];
            if (rgb != -1) {
              outPixels[j * imageWidth + i] = rgb;
              // // TODO better interpolation
            }
          }
        }
      }
    }
    return outPixels;
  }

  public Coordinates toSourcePoint(final double destX, final double destY) {
    final double sourceX = 0.0;
    final double sourceY = 0.0;
    int count = 0;

    for (int nx = 0; nx <= this.degree; nx++) {
      for (int ny = 0; ny <= nx; ny++) {
        final double t = Math.pow(destX, nx - ny) * Math.pow(destY, ny);
        // sx += xCoeffs[c] * t;
        // sy += yCoeffs[c] * t;
        count++;
      }
    }

    return new DoubleCoordinates(sourceX, sourceY);
  }
}
