package com.revolsys.swing.map.layer.raster.filter;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;

/**
 * Affine warp that maintains the same image size and bounding box.
 */
public class WarpAffineFilter extends WarpFilter {
  private static final long serialVersionUID = 5344579737675153596L;

  private boolean hasInverse = true;

  private double inverseScaleX = 1;

  private double inverseScaleY = 1;

  private double inverseShearX;

  private double inverseShearY;

  private double inverseTranslateX;

  private double inverseTranslateY;

  private double scaleX = 1;

  private double scaleY = 1;

  private double shearX;

  private double shearY;

  private double translateX;

  private double translateY;

  public WarpAffineFilter() {
  }

  public WarpAffineFilter(final double translateX, final double translateY,
    final double scaleX, final double scaleY, final double shearX,
    final double shearY) {
    this.translateX = translateX;
    this.scaleX = scaleX;
    this.shearX = shearX;
    this.translateY = translateY;
    this.shearY = shearY;
    this.scaleY = scaleY;

    try {
      final AffineTransform transform = new AffineTransform(scaleX, shearY,
        shearX, scaleY, translateX, translateY);
      final AffineTransform invTransform = transform.createInverse();
      this.inverseTranslateX = invTransform.getTranslateX();
      this.inverseScaleX = invTransform.getScaleX();
      this.inverseShearX = invTransform.getShearX();

      this.inverseTranslateY = invTransform.getTranslateY();
      this.inverseShearY = invTransform.getShearY();
      this.inverseScaleY = invTransform.getScaleY();
    } catch (final java.awt.geom.NoninvertibleTransformException e) {
      this.hasInverse = false;
    }
  }

  public WarpAffineFilter(final float... coeffs) {
    this(coeffs[2], coeffs[5], coeffs[0], coeffs[4], coeffs[1], coeffs[3]);
  }

  @Override
  public BufferedImage filter(final BufferedImage source) {
    if (isIdentityTransform()) {
      return source;
    } else {
      return super.filter(source);
    }
  }

  @Override
  protected int[] filterPixels(final int imageWidth, final int imageHeight,
    final int[] inPixels, final Rectangle transformedSpace) {
    final int destWidth = transformedSpace.width;
    final int destHeight = transformedSpace.height;
    final int[] outPixels = new int[destWidth * destHeight];
    for (int i = 0; i < destWidth; i++) {
      for (int j = 0; j < destHeight; j++) {
        final Coordinates source = toSourcePoint(i, j);
        final int imageI = (int)source.getX();
        final int imageJ = (int)source.getY();
        if (imageI > -1 && imageI < imageWidth) {
          if (imageJ > -1 && imageJ < imageHeight) {
            final int rgb = inPixels[imageJ * imageWidth + imageI];
            if (rgb != -1) {
              outPixels[j * imageWidth + i] = rgb;
            }
          }
        }
      }
    }
    return outPixels;
  }

  public boolean isIdentityTransform() {
    if (this.translateX == 0) {
      if (this.translateY == 0) {
        if (this.scaleX == 1) {
          if (this.scaleY == 0) {
            if (this.shearX == 0) {
              if (this.shearY == 0) {
                return true;
              }
            }
          }
        }
      }
    }
    return false;
  }

  @Override
  public Coordinates toDestPoint(final double sourceX, final double sourceY) {
    if (this.hasInverse) {
      final double destX = this.inverseTranslateX + this.inverseScaleX
        * sourceX + this.inverseShearX * sourceY;
      final double destY = this.inverseTranslateY + this.inverseShearY
        * sourceX + this.inverseScaleY * sourceY;
      return new DoubleCoordinates(destX, destY);
    } else {
      return null;
    }
  }

  @Override
  public Coordinates toSourcePoint(final double destX, final double destY) {
    final double sourceX = this.translateX + this.scaleX * destX + this.shearX
      * destY;
    final double sourceY = this.translateY + this.shearY * destX + this.scaleY
      * destY;
    return new DoubleCoordinates(sourceX, sourceY);
  }
}
