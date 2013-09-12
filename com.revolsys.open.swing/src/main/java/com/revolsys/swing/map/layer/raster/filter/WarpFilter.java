package com.revolsys.swing.map.layer.raster.filter;

import java.awt.image.BufferedImage;
import java.util.List;

import com.jhlabs.image.WholeImageFilter;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.swing.map.overlay.MappedLocation;
import com.vividsolutions.jts.geom.Point;

public abstract class WarpFilter extends WholeImageFilter {
  private static final long serialVersionUID = 1L;

  public static WarpFilter createWarpFilter(final BoundingBox boundingBox,
    final List<MappedLocation> mappings, int degree, final int imageWidth,
    final int imageHeight) {
    final int pointCount = mappings.size();
    if (pointCount > 0) {
      int minNumPoints = (degree + 1) * (degree + 2) / 2;
      while (minNumPoints > pointCount) {
        degree--;
        minNumPoints = (degree + 1) * (degree + 2) / 2;
      }

      if (degree > 0) {
        if (degree == 1) {
          return new WarpAffineFilter(boundingBox, imageWidth, imageHeight,
            mappings);
        } else if (degree == 2) {
          // float[] coeffs = PolyWarpSolver.getCoeffs(sourceCoords, 0,
          // targetCoords, 0,
          // numCoords, 1, 1, 1, 1, degree);
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
      }
    }

    return new WarpAffineFilter(boundingBox, imageWidth, imageHeight);
  }

  public static CoordinatesList targetPointsToPixels(
    final BoundingBox boundingBox, final CoordinatesList points,
    final int imageWidth, final int imageHeight) {
    final int numPoints = points.size();
    final CoordinatesList dstCoords = new DoubleCoordinatesList(numPoints, 2);
    for (int i = 0; i < numPoints; i++) {
      final Coordinates modelPoint = points.get(i);
      final Coordinates imagePoint = toImagePoint(boundingBox, modelPoint,
        imageWidth, imageHeight);
      dstCoords.setX(i, imagePoint.getX());
      dstCoords.setY(i, imagePoint.getY());
    }
    return dstCoords;
  }

  public static Coordinates targetPointToPixel(final BoundingBox boundingBox,
    final Coordinates point, final int imageWidth, final int imageHeight) {
    return toImagePoint(boundingBox, point, imageWidth, imageHeight);
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

  private final BoundingBox boundingBox;

  private final int imageHeight;

  private final int imageWidth;

  public WarpFilter(final BoundingBox boundingBox, final int imageWidth,
    final int imageHeight) {
    this.boundingBox = boundingBox;
    this.imageWidth = imageWidth;
    this.imageHeight = imageHeight;
  }

  public BufferedImage filter(final BufferedImage source) {
    final int width = source.getWidth();
    final int height = source.getHeight();
    final BufferedImage target = new BufferedImage(width, height,
      BufferedImage.TYPE_INT_ARGB);
    return super.filter(source, target);
  }

  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  public GeometryFactory getGeometryFactory() {
    return boundingBox.getGeometryFactory();
  }

  public int getImageHeight() {
    return this.imageHeight;
  }

  public int getImageWidth() {
    return this.imageWidth;
  }

  public Coordinates sourcePixelToTargetPixel(final Coordinates sourcePixel) {
    final double x = sourcePixel.getX();
    final double y = sourcePixel.getY();
    return sourcePixelToTargetPixel(x, y);
  }

  public abstract Coordinates sourcePixelToTargetPixel(final double sourceX,
    final double sourceY);

  public Coordinates sourcePixelToTargetPoint(final BoundingBox boundingBox,
    final Coordinates sourcePixel) {
    final Coordinates targetImagePoint = sourcePixelToTargetPixel(sourcePixel);
    final double targetPixelX = targetImagePoint.getX();
    final double targetPixelY = targetImagePoint.getY();
    final Coordinates targetPoint = toModelPoint(boundingBox, targetPixelX,
      targetPixelY, this.imageWidth, this.imageHeight);
    return targetPoint;
  }

  public Point sourcePixelToTargetPoint(final Coordinates sourcePixel) {
    final Coordinates targetImagePoint = sourcePixelToTargetPixel(sourcePixel);
    final double targetPixelX = targetImagePoint.getX();
    final double targetPixelY = targetImagePoint.getY();
    return targetPixelToPoint(targetPixelX, targetPixelY);
  }

  public Point sourcePixelToTargetPoint(final double x, final double y) {
    final DoubleCoordinates sourcePixel = new DoubleCoordinates(x, y);
    return sourcePixelToTargetPoint(sourcePixel);
  }

  public Point sourcePixelToTargetPoint(final MappedLocation tiePoint) {
    final Coordinates sourcePixel = tiePoint.getSourcePixel();
    return sourcePixelToTargetPoint(sourcePixel);
  }

  public Point targetPixelToPoint(final double targetPixelX,
    final double targetPixelY) {
    final Coordinates targetPoint = toModelPoint(this.boundingBox,
      targetPixelX, targetPixelY, this.imageWidth, this.imageHeight);
    return getGeometryFactory().createPoint(targetPoint);
  }

  public Coordinates targetPixelToSourcePixel(final Coordinates point) {
    final double x = point.getX();
    final double y = point.getY();
    return targetPixelToSourcePixel(x, y);
  }

  public abstract Coordinates targetPixelToSourcePixel(final double targetX,
    final double targetY);

  public Coordinates targetPointToPixel(final Coordinates targetPoint) {
    return toImagePoint(this.boundingBox, targetPoint, this.imageWidth,
      this.imageHeight);
  }

  public Coordinates targetPointToSourcePixel(final Coordinates targetPoint) {
    final Coordinates targetPixel = targetPointToPixel(targetPoint);
    final Coordinates sourcePixel = targetPixelToSourcePixel(targetPixel);
    return sourcePixel;
  }

}
