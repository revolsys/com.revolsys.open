package com.revolsys.swing.map.layer.raster.filter;

import java.awt.image.BufferedImage;
import java.util.List;

import com.jhlabs.image.WholeImageFilter;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.impl.LineStringDouble;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.swing.map.overlay.MappedLocation;

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

  public static LineString targetPointsToPixels(final BoundingBox boundingBox,
    final LineString points, final int imageWidth, final int imageHeight) {
    final int numPoints = points.getVertexCount();
    final double[] coordinates = new double[numPoints * 2];
    int j = 0;
    for (int i = 0; i < numPoints; i++) {
      final Point modelPoint = points.getPoint(i);
      final Point imagePoint = toImagePoint(boundingBox, modelPoint,
        imageWidth, imageHeight);
      coordinates[j++] = imagePoint.getX();
      coordinates[j++] = imagePoint.getY();
    }
    return new LineStringDouble(2, coordinates);
  }

  public static Point targetPointToPixel(final BoundingBox boundingBox,
    final Point point, final int imageWidth, final int imageHeight) {
    return toImagePoint(boundingBox, point, imageWidth, imageHeight);
  }

  public static Point toImagePoint(final BoundingBox boundingBox,
    Point modelPoint, final int imageWidth, final int imageHeight) {
    modelPoint = modelPoint.convert(boundingBox.getGeometryFactory(), 2);
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
    return new PointDouble(imageX, imageY);
  }

  public static Point toModelPoint(final BoundingBox boundingBox,
    final double imageX, final double imageY, final int imageWidth,
    final int imageHeight) {
    final double xPercent = imageX / imageWidth;
    final double yPercent = imageY / imageHeight;

    final double modelWidth = boundingBox.getWidth();
    final double modelHeight = boundingBox.getHeight();

    final double modelX = boundingBox.getMinX() + modelWidth * xPercent;
    final double modelY = boundingBox.getMinY() + modelHeight * yPercent;
    final GeometryFactory geometryFactory = boundingBox.getGeometryFactory();
    final Point imagePoint = geometryFactory.point(modelX, modelY);
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

  public com.revolsys.jts.geom.GeometryFactory getGeometryFactory() {
    return boundingBox.getGeometryFactory();
  }

  public int getImageHeight() {
    return this.imageHeight;
  }

  public int getImageWidth() {
    return this.imageWidth;
  }

  public abstract Point sourcePixelToTargetPixel(final double sourceX,
    final double sourceY);

  public Point sourcePixelToTargetPixel(final Point sourcePixel) {
    final double x = sourcePixel.getX();
    final double y = sourcePixel.getY();
    return sourcePixelToTargetPixel(x, y);
  }

  public Point sourcePixelToTargetPoint(final BoundingBox boundingBox,
    final Point sourcePixel) {
    final Point targetImagePoint = sourcePixelToTargetPixel(sourcePixel);
    final double targetPixelX = targetImagePoint.getX();
    final double targetPixelY = targetImagePoint.getY();
    final Point targetPoint = toModelPoint(boundingBox, targetPixelX,
      targetPixelY, this.imageWidth, this.imageHeight);
    return targetPoint;
  }

  public Point sourcePixelToTargetPoint(final double x, final double y) {
    final PointDouble sourcePixel = new PointDouble(x, y);
    return sourcePixelToTargetPoint(sourcePixel);
  }

  public Point sourcePixelToTargetPoint(final MappedLocation tiePoint) {
    final Point sourcePixel = tiePoint.getSourcePixel();
    return sourcePixelToTargetPoint(sourcePixel);
  }

  public Point sourcePixelToTargetPoint(final Point sourcePixel) {
    final Point targetImagePoint = sourcePixelToTargetPixel(sourcePixel);
    final double targetPixelX = targetImagePoint.getX();
    final double targetPixelY = targetImagePoint.getY();
    return targetPixelToPoint(targetPixelX, targetPixelY);
  }

  public Point targetPixelToPoint(final double targetPixelX,
    final double targetPixelY) {
    final Point targetPoint = toModelPoint(this.boundingBox, targetPixelX,
      targetPixelY, this.imageWidth, this.imageHeight);
    return getGeometryFactory().point(targetPoint);
  }

  public abstract Point targetPixelToSourcePixel(final double targetX,
    final double targetY);

  public Point targetPixelToSourcePixel(final Point point) {
    final double x = point.getX();
    final double y = point.getY();
    return targetPixelToSourcePixel(x, y);
  }

  public Point targetPointToPixel(final Point targetPoint) {
    return toImagePoint(this.boundingBox, targetPoint, this.imageWidth,
      this.imageHeight);
  }

  public Point targetPointToSourcePixel(final Point targetPoint) {
    final Point targetPixel = targetPointToPixel(targetPoint);
    final Point sourcePixel = targetPixelToSourcePixel(targetPixel);
    return sourcePixel;
  }

}
