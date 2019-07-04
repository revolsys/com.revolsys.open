package com.revolsys.raster;
// Mesh and Warp Canvas

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import org.jeometry.coordinatesystem.operation.CoordinatesOperation;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.geometry.model.Point;

public class ImageProjector {

  private static int[] gauss(final double[][] matrix) {
    final int[] indeces = {
      0, 1, 2
    };
    final double[] scalingFactor = new double[3];
    for (int i = 0; i < 3; i++) {
      scalingFactor[i] = maxAbs(matrix[i]);
    }

    int j = 2;
    for (int k = 0; k < 2; k++) {
      --j;
      double maxR = 0;
      for (int i = k; i < 3; i++) {
        final int indexI = indeces[i];
        final double r = Math.abs(matrix[indexI][k] / scalingFactor[indexI]);
        if (r > maxR) {
          maxR = r;
          j = i;
        }
      }
      final int temp = indeces[j];
      indeces[j] = indeces[k];
      indeces[k] = temp;
      final int indexK = indeces[k];
      final double[] rowK = matrix[indexK];
      for (int i = k + 1; i < 3; i++) {
        final int indexI = indeces[i];
        final double[] rowI = matrix[indexI];
        final double xmult = rowI[k] / rowK[k];
        rowI[k] = xmult;
        for (j = k + 1; j < 3; j++) {
          rowI[j] = rowI[j] - xmult * rowK[j];
        }
      }
    }
    return indeces;
  }

  private static double maxAbs(final double[] values) {
    double max = 0;
    for (final double value : values) {
      final double absValue = Math.abs(value);
      if (absValue > max) {
        max = absValue;
      }
    }
    return max;
  }

  private static double[] solve(final double[][] matrix, final int[] indices, final double[] b) {
    final double[] x = new double[3];
    for (int k = 0; k < 2; k++) {
      final int indexK = indices[k];
      for (int i = k + 1; i < 3; i++) {
        final int indexI = indices[i];
        b[indexI] -= matrix[indexI][k] * b[indexK];
      }
    }
    final int indexLast = indices[2];
    x[2] = b[indexLast] / matrix[indexLast][2];

    for (int i = 1; i >= 0; --i) {
      final int index = indices[i];
      double sum = b[index];
      for (int j = i + 1; j < 3; j++) {
        sum = sum - matrix[index][j] * x[j];
      }
      x[i] = sum / matrix[index][i];
    }
    return x;
  }

  private final GeoreferencedImage sourceImage;

  private final BufferedImage sourceBufferdImage;

  private BufferedGeoreferencedImage targetImage;

  private final ImageProjectorTriangle sourceTriangle = new ImageProjectorTriangle();

  private final ImageProjectorTriangle targetTriangle = new ImageProjectorTriangle();

  private final CoordinatesOperationPoint point = new CoordinatesOperationPoint();

  private CoordinatesOperation operation;

  private Graphics2D g2;

  private GeometryFactory targetGeometryFactory;

  private final AffineTransform transform = new AffineTransform();

  private final int step = 50;

  public ImageProjector(final GeoreferencedImage sourceImage,
    final GeometryFactoryProxy targetGeometryFactory) {
    this.sourceImage = sourceImage;
    this.sourceBufferdImage = sourceImage.getBufferedImage();
    setTargetGeometryFactory(targetGeometryFactory);
  }

  public void drawImage(final Graphics2D graphics) {
    this.g2 = graphics;

    graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
      RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    final Shape clip = graphics.getClip();

    final int imageWidth = this.sourceImage.getImageWidth();
    final int imageHeight = this.sourceImage.getImageHeight();

    final int step = this.step;
    for (int imageY = 0; imageY < imageHeight; imageY += step) {
      final int imageY2 = Math.min(imageHeight - 1, imageY + step);
      for (int imageX = 0; imageX < imageWidth; imageX += step) {
        final int imageX2 = Math.min(imageWidth - 1, imageX + step);
        drawTriangle(imageX, imageY, imageX2, imageY, imageX, imageY2);
        drawTriangle(imageX, imageY2, imageX2, imageY, imageX2, imageY2);
      }
    }
    graphics.setClip(clip);
  }

  private void drawTriangle(final int x1, final int y1, final int x2, final int y2, final int x3,
    final int y3) {
    final ImageProjectorTriangle sourceTriangle = this.sourceTriangle;
    sourceTriangle.setCorners(x1, y1, x2, y2, x3, y3);
    final GeoreferencedImage sourceImage = this.sourceImage;
    final ImageProjectorTriangle targetTriangle = this.targetTriangle;
    for (int i = 0; i < 3; i++) {
      final double imageX = sourceTriangle.getX(i);
      final double imageY = sourceTriangle.getY(i);
      final CoordinatesOperationPoint point = this.point;
      sourceImage.copyModelPoint(point, imageX, imageY);
      this.operation.perform(point);
      this.targetImage.toImagePoint(point);
      final double targetImageX = point.x;
      final double targetImageY = point.y;
      targetTriangle.setPoint(i, targetImageX, targetImageY);
    }

    final double[][] a = new double[3][3];
    for (int i = 0; i < 3; ++i) {
      a[i][0] = sourceTriangle.getX(i);
      a[i][1] = sourceTriangle.getY(i);
      a[i][2] = 1.0;
    }

    final int l[] = gauss(a);

    final double[] bx = targetTriangle.xCoordinates.clone();
    final double[] x = solve(a, l, bx);

    final double[] by = targetTriangle.yCoordinates.clone();
    final double[] y = solve(a, l, by);

    this.transform.setTransform(x[0], y[0], x[1], y[1], x[2], y[2]);
    final Graphics2D g2 = this.g2;
    g2.setClip(targetTriangle);
    g2.drawImage(this.sourceBufferdImage, this.transform, null);
  }

  private double getResolution(final double originalDistance, final double distance1,
    final double distance2) {
    final double targetPixelSize1 = distance1 / originalDistance;
    final double targetPixelSize2 = distance2 / originalDistance;
    double targetPixelSize;
    if (targetPixelSize1 < targetPixelSize2) {
      targetPixelSize = targetPixelSize1;
    } else {
      targetPixelSize = targetPixelSize2;
    }
    return targetPixelSize;
  }

  public GeoreferencedImage newImage() {
    final BoundingBox sourceBoundingBox = this.sourceImage.getBoundingBox();

    final BoundingBox targetBoundingBox = sourceBoundingBox.bboxToCs(this.targetGeometryFactory);
    final Point p1 = sourceBoundingBox.getCornerPoint(0).convertPoint2d(this.targetGeometryFactory);
    final Point p2 = sourceBoundingBox.getCornerPoint(1).convertPoint2d(this.targetGeometryFactory);
    final Point p3 = sourceBoundingBox.getCornerPoint(2).convertPoint2d(this.targetGeometryFactory);
    final Point p4 = sourceBoundingBox.getCornerPoint(3).convertPoint2d(this.targetGeometryFactory);

    final double sourceResolutionX = this.sourceImage.getResolutionX();
    final double sourceWidth = sourceBoundingBox.getWidth() / sourceResolutionX;
    final double width1 = p1.distancePoint(p2);
    final double width2 = p3.distancePoint(p4);
    final double targetResolutionX = getResolution(sourceWidth, width1, width2);

    final double sourceResolutionY = this.sourceImage.getResolutionY();
    final double sourceHeight = sourceBoundingBox.getHeight() / sourceResolutionY;
    final double height1 = p1.distancePoint(p4);
    final double height2 = p2.distancePoint(p3);
    final double targetResolutionY = getResolution(sourceHeight, height1, height2);

    final double width = targetBoundingBox.getWidth();
    final double height = targetBoundingBox.getHeight();
    final int targetImageWidth = (int)(width / targetResolutionX);

    final int targetImageHeight = (int)(height / targetResolutionY);
    this.targetImage = BufferedGeoreferencedImage.newImage(targetBoundingBox, targetImageWidth,
      targetImageHeight);
    final Graphics2D graphics = this.targetImage.getBufferedImage().createGraphics();
    try {
      drawImage(graphics);
    } finally {
      graphics.dispose();
    }
    return this.targetImage;
  }

  public void setTargetGeometryFactory(final GeometryFactoryProxy targetGeometryFactory) {
    if (targetGeometryFactory == null) {
      this.targetGeometryFactory = GeometryFactory.DEFAULT_2D;
    } else {
      this.targetGeometryFactory = targetGeometryFactory.getGeometryFactory();
    }

    this.operation = this.sourceImage.getCoordinatesOperation(targetGeometryFactory);
  }

}
