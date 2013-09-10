package com.revolsys.swing.map.layer.raster.filter;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.util.List;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.swing.map.overlay.MappedLocation;
import com.revolsys.util.ExceptionUtil;

/**
 * Affine warp that maintains the same image size and bounding box.
 */
public class WarpAffineFilter extends WarpFilter {
  private static final long serialVersionUID = 5344579737675153596L;

  private static double[] calculateLSM(final BoundingBox boundingBox,
    final int imageWidth, final int imageHeight,
    final List<MappedLocation> mappings) {

    final GeneralMatrix A = getAMatrix(mappings);

    final GeneralMatrix X = getXMatrix(boundingBox, imageWidth, imageHeight,
      mappings);

    final GeneralMatrix P = getWeights(mappings.size());

    final GeneralMatrix AT = A.clone();
    AT.transpose();

    final GeneralMatrix ATP = new GeneralMatrix(AT.getNumRow(), P.getNumCol());
    final GeneralMatrix ATPA = new GeneralMatrix(AT.getNumRow(), A.getNumCol());
    final GeneralMatrix ATPX = new GeneralMatrix(AT.getNumRow(), 1);
    final GeneralMatrix x = new GeneralMatrix(A.getNumCol(), 1);
    ATP.mul(AT, P);
    ATPA.mul(ATP, A);
    ATPX.mul(ATP, X);
    ATPA.invert();
    x.mul(ATPA, ATPX);
    ATPA.invert();

    x.transpose();

    return x.getElements()[0];
  }

  public static AffineTransform getAffineTransform(
    final BoundingBox boundingBox, final int imageWidth, final int imageHeight,
    final List<MappedLocation> mappings) {

    final double[] affineTransformMatrix = calculateLSM(boundingBox,
      imageWidth, imageHeight, mappings);
    final double translateX = affineTransformMatrix[2];
    final double translateY = affineTransformMatrix[5];
    final double scaleX = affineTransformMatrix[0];
    final double scaleY = affineTransformMatrix[4];
    final double shearX = affineTransformMatrix[1];
    final double shearY = affineTransformMatrix[3];
    try {
      return new AffineTransform(scaleX, shearY, shearX, scaleY, translateX,
        translateY).createInverse();
    } catch (final NoninvertibleTransformException e) {
      ExceptionUtil.throwUncheckedException(e);
      return null;
    }
  }

  public static GeneralMatrix getAMatrix(final List<MappedLocation> mappings) {
    final GeneralMatrix A = new GeneralMatrix(2 * mappings.size(), 6);

    final int numRow = mappings.size() * 2;

    for (int j = 0; j < numRow / 2; ++j) {
      final MappedLocation mappedLocation = mappings.get(j);
      final Coordinates sourcePoint = mappedLocation.getSourcePixel();
      A.setRowValues(j, sourcePoint.getX(), sourcePoint.getY(), 1.0D, 0.0D,
        0.0D, 0.0D);
    }

    for (int j = numRow / 2; j < numRow; ++j) {
      final MappedLocation mappedLocation = mappings.get(j - (numRow / 2));
      final Coordinates sourcePoint = mappedLocation.getSourcePixel();
      A.setRowValues(j, 0.0D, 0.0D, 0.0D, sourcePoint.getX(),
        sourcePoint.getY(), 1.0D);
    }
    return A;
  }

  public static GeneralMatrix getWeights(final int size) {
    final GeneralMatrix P = new GeneralMatrix(size * 2, size * 2);

    for (int j = 0; j < size; ++j) {
      P.setElement(j, j, 1.0D);
    }
    return P;
  }

  private static GeneralMatrix getXMatrix(final BoundingBox boundingBox,
    final int imageWidth, final int imageHeight,
    final List<MappedLocation> mappings) {
    // TODO Auto-generated method stub

    final GeneralMatrix X = new GeneralMatrix(2 * mappings.size(), 1);

    final int numRow = X.getNumRow();

    for (int j = 0; j < numRow / 2; ++j) {
      final MappedLocation mappedLocation = mappings.get(j);
      final Coordinates targetPixel = mappedLocation.getTargetPixel(
        boundingBox, imageWidth, imageHeight);
      final double x = targetPixel.getX();
      X.setElement(j, 0, x);
    }

    for (int j = numRow / 2; j < numRow; ++j) {
      final MappedLocation mappedLocation = mappings.get(j - (numRow / 2));
      final Coordinates targetPixel = mappedLocation.getTargetPixel(
        boundingBox, imageWidth, imageHeight);
      final double y = targetPixel.getY();
      X.setElement(j, 0, y);
    }
    return X;
  }

  public static double transformX(final double x, final double y,
    final double translate, final double scale, final double shear) {
    return translate + scale * x + shear * y;
  }

  public static double transformY(final double x, final double y,
    final double translate, final double scale, final double shear) {
    return translate + shear * x + scale * y;
  }

  private boolean hasInverse = true;

  private double inverseScaleX = 1;

  private double inverseScaleY = 1;

  private double inverseShearX;

  private double inverseShearY;

  private double inverseTranslateX;

  private double inverseTranslateY;

  private double scaleX = 1;

  private double scaleY = 1;

  private double shearX = 0;

  private double shearY = 0;

  private double translateX = 0;

  private double translateY = 0;

  public WarpAffineFilter(final BoundingBox boundingBox, final int imageWidth,
    final int imageHeight) {
    super(boundingBox, imageWidth, imageHeight);
  }

  public WarpAffineFilter(final BoundingBox boundingBox, final int imageWidth,
    final int imageHeight, final AffineTransform transform) {
    this(boundingBox, imageWidth, imageHeight, transform.getTranslateX(),
      transform.getTranslateY(), transform.getScaleX(), transform.getScaleY(),
      transform.getShearX(), transform.getShearY());
  }

  public WarpAffineFilter(final BoundingBox boundingBox, final int imageWidth,
    final int imageHeight, final double translateX, final double translateY,
    final double scaleX, final double scaleY, final double shearX,
    final double shearY) {
    super(boundingBox, imageWidth, imageHeight);
    this.translateX = translateX;
    this.translateY = translateY;

    this.scaleX = scaleX;
    this.scaleY = scaleY;
    this.shearX = shearX;
    this.shearY = shearY;

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

  public WarpAffineFilter(final BoundingBox boundingBox, final int imageWidth,
    final int imageHeight, final double[] affineTransformMatrix) {
    this(boundingBox, imageWidth, imageHeight, affineTransformMatrix[2],
      affineTransformMatrix[5], affineTransformMatrix[0],
      affineTransformMatrix[4], affineTransformMatrix[1],
      affineTransformMatrix[3]);
  }

  public WarpAffineFilter(final BoundingBox boundingBox, final int imageWidth,
    final int imageHeight, final float... coeffs) {
    this(boundingBox, imageWidth, imageHeight, coeffs[2], coeffs[5], coeffs[0],
      coeffs[4], coeffs[1], coeffs[3]);
  }

  public WarpAffineFilter(final BoundingBox boundingBox, final int imageWidth,
    final int imageHeight, final List<MappedLocation> mappings) {
    this(boundingBox, imageWidth, imageHeight, getAffineTransform(boundingBox,
      imageWidth, imageHeight, mappings));
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
    final int[] sourcePixels, final Rectangle transformedSpace) {
    final int targetWidth = transformedSpace.width;
    final int targetHeight = transformedSpace.height;
    final int[] targetPixels = new int[targetWidth * targetHeight];
    for (int targetX = 0; targetX < targetWidth; targetX++) {
      for (int targetY = 0; targetY < targetHeight; targetY++) {

        final Coordinates sourcePixel = targetPixelToSourcePixel(targetX,
          targetHeight - targetY);

        final int souceX = (int)sourcePixel.getX();
        final int sourceY = targetHeight - (int)sourcePixel.getY();
        if (souceX > -1 && souceX < targetWidth) {
          if (sourceY > -1 && sourceY < targetHeight) {

            final int rgb = sourcePixels[sourceY * targetWidth + souceX];
            if (rgb != -1) {

              targetPixels[targetY * targetWidth + targetX] = rgb;
            }
          }
        }
      }
    }
    return targetPixels;
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
  public Coordinates sourcePixelToTargetPixel(final double sourceX,
    final double sourceY) {
    if (this.hasInverse) {
      final double destX = transformX(sourceX, sourceY, inverseTranslateX,
        inverseScaleX, inverseShearX);
      final double destY = transformY(sourceX, sourceY, inverseTranslateY,
        inverseScaleY, inverseShearY);
      return new DoubleCoordinates(destX, destY);
    } else {
      return null;
    }
  }

  @Override
  public Coordinates targetPixelToSourcePixel(final double destX,
    final double destY) {
    final double sourceX = transformX(destX, destY, translateX, scaleX, shearX);
    final double sourceY = transformY(destX, destY, translateY, scaleY, shearY);
    return new DoubleCoordinates(sourceX, sourceY);
  }

  @Override
  public String toString() {
    return "Affine[translate(" + translateX + "," + translateY + "),scale("
      + scaleX + "," + scaleY + "),shear(" + shearX + "," + shearY + ")]"
      + "\nInv-Affine[translate(" + inverseTranslateX + "," + inverseTranslateY
      + "),scale(" + inverseScaleX + "," + inverseScaleY + "),shear("
      + inverseShearX + "," + inverseShearY + ")]";
  }
}
