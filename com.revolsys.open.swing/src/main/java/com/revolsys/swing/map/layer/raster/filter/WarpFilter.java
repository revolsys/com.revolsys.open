package com.revolsys.swing.map.layer.raster.filter;

import java.awt.image.BufferedImage;

import com.jhlabs.image.WholeImageFilter;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.sun.media.jai.util.PolyWarpSolver;

public abstract class WarpFilter extends WholeImageFilter {
  private static final long serialVersionUID = 1L;

  public static WarpFilter createFilter() {
    return new WarpAffineFilter();
  }

  public static WarpFilter createWarpFilter(final BoundingBox boundingBox,
    final CoordinatesList sourcePoints, final CoordinatesList destPoints,
    final int numCoords, int degree, final int imageWidth, final int imageHeight) {

    final int minPoints = Math.min(sourcePoints.size(), destPoints.size());
    if (minPoints == 0) {
      return new WarpAffineFilter();
    } else {
      int minNumPoints = (degree + 1) * (degree + 2) / 2;
      while (minNumPoints > minPoints) {
        degree--;
        minNumPoints = (degree + 1) * (degree + 2) / 2;
      }

      final float[] sourceCoords = CoordinatesUtil.toFloatArray(sourcePoints, 2);
      final float[] destCoords = toDestinationImagePoints(boundingBox,
        destPoints, imageWidth, imageHeight);

      final float[] coeffs;
      if (degree > 0) {
        coeffs = PolyWarpSolver.getCoeffs(sourceCoords, 0, destCoords, 0,
          numCoords, 1, 1, 1, 1, degree);
        if (degree < 2) {
          return new WarpAffineFilter(coeffs);
        } else if (degree == 2) {
          // return new WarpQuadratic(xCoeffs, yCoeffs, preScaleX, preScaleY,
          // postScaleX, postScaleY);
        } else if (degree == 3) {
          // return new WarpCubic(xCoeffs, yCoeffs, preScaleX, preScaleY,
          // postScaleX,
          // postScaleY);
        } else {
          // return new WarpGeneralPolynomial(xCoeffs, yCoeffs, preScaleX,
          // preScaleY,
          // postScaleX, postScaleY);
        }
      } else {
        final float translateX = sourceCoords[0] - destCoords[0];
        final float translateY = destCoords[1] - sourceCoords[1];
        return new WarpAffineFilter(translateX, translateY, 1, 1, 0, 0);
      }
    }

    return null;
  }

  public static float[] toDestinationImagePoints(final BoundingBox boundingBox,
    final CoordinatesList points, final int imageWidth, final int imageHeight) {
    final int numPoints = points.size();
    final float[] dstCoords = new float[numPoints * 2];
    for (int i = 0; i < numPoints; i++) {
      final Coordinates modelPoint = points.get(i);
      final Coordinates imagePoint = toImagePoint(boundingBox, modelPoint,
        imageWidth, imageHeight);
      dstCoords[i * 2] = (float)imagePoint.getX();
      dstCoords[i * 2 + 1] = (float)imagePoint.getY();
    }
    return dstCoords;
  }

  public static Coordinates toImagePoint(final BoundingBox boundingBox,
    final Coordinates modelPoint, final int imageWidth, final int imageHeight) {
    final double modelX = modelPoint.getX();
    final double modelY = modelPoint.getY();
    final double modelDeltaX = modelX - boundingBox.getMinX();
    final double modelDeltaY = modelY - boundingBox.getMinY();

    final double modelWidth = boundingBox.getWidth();
    final double modelHeight = boundingBox.getHeight();

    final double xRatio = modelDeltaX / modelWidth;
    final double yRatio = modelDeltaY / modelHeight;

    final double imageX = imageWidth * xRatio;
    final double imageY = imageHeight * yRatio;
    return new DoubleCoordinates(imageX, imageY);
  }

  public static Coordinates toModelPoint(final BoundingBox boundingBox,
    final double imageX, final double imageY, final int imageWidth,
    final int imageHeight) {
    final double xPercent = imageX / imageWidth;
    final double yPercent = imageY / imageHeight;

    final double modelWidth = boundingBox.getWidth();
    final double modelHeight = boundingBox.getHeight();

    final double modelX = boundingBox.getMinX() + modelWidth * xPercent;
    final double modelY = boundingBox.getMinY() + modelHeight * yPercent;
    final DoubleCoordinates imagePoint = new DoubleCoordinates(modelX, modelY);
    return imagePoint;
  }

  public BufferedImage filter(final BufferedImage source) {
    final int width = source.getWidth();
    final int height = source.getHeight();
    final BufferedImage dest = new BufferedImage(width, height,
      BufferedImage.TYPE_INT_ARGB);
    return super.filter(source, dest);
  }

  public Coordinates toDestPoint(final Coordinates point) {
    final double x = point.getX();
    final double y = point.getY();
    return toDestPoint(x, y);
  }

  public abstract Coordinates toDestPoint(final double sourceX,
    final double sourceY);

  public Coordinates toModelPoint(final BoundingBox boundingBox,
    final Coordinates sourceImagePoint, final int imageWidth,
    final int imageHeight) {
    final Coordinates destImagePoint = toDestPoint(sourceImagePoint);

    final double destImageX = destImagePoint.getX();
    final double destImageY = destImagePoint.getY();
    final Coordinates modelPoint = toModelPoint(boundingBox, destImageX,
      destImageY, imageWidth, imageHeight);
    return modelPoint;
  }

  public Coordinates toSourceImagePoint(final BoundingBox boundingBox,
    final Coordinates modelPoint, final int imageWidth, final int imageHeight) {
    final Coordinates imagePoint = toImagePoint(boundingBox, modelPoint,
      imageWidth, imageHeight);

    final Coordinates sourcePoint = toSourcePoint(imagePoint);
    return sourcePoint;
  }

  public Coordinates toSourcePoint(final Coordinates point) {
    final double x = point.getX();
    final double y = point.getY();
    return toSourcePoint(x, y);
  }

  public abstract Coordinates toSourcePoint(final double destX,
    final double destY);

}
