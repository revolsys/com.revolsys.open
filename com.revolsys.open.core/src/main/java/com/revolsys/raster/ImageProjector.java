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

  private static void gauss(final double[][] matrix, final int[] l) {
    /****************************************************
        a is a n x n matrix and l is an int array of length n
        l is used as an index array that will determine the order of
        elimination of coefficients
        All array indexes are assumed to start at 0
    ******************************************************/
    final double[] scalingFactor = new double[3]; // scaling factor
    int i = 0;
    int j = 0;
    for (i = 0; i < 3; ++i) {
      l[i] = i;
      double scaleMax = 0;
      for (j = 0; j < 3; ++j) {
        scaleMax = Math.max(scaleMax, Math.abs(matrix[i][j]));
      }
      scalingFactor[i] = scaleMax;
    }

    i = 2;
    for (int k = 0; k < 2; ++k) {
      --j;
      double rmax = 0;
      for (i = k; i < 3; ++i) {
        final double r = Math.abs(matrix[l[i]][k] / scalingFactor[l[i]]);
        if (r > rmax) {
          rmax = r;
          j = i;
        }
      }
      final int temp = l[j];
      l[j] = l[k];
      l[k] = temp;
      for (i = k + 1; i < 3; ++i) {
        final double xmult = matrix[l[i]][k] / matrix[l[k]][k];
        matrix[l[i]][k] = xmult;
        for (j = k + 1; j < 3; ++j) {
          matrix[l[i]][j] = matrix[l[i]][j] - xmult * matrix[l[k]][j];
        }
      }
    }
  }

  private static void solve(final double[][] a, final int[] l, final double[] b, final double[] x) {
    /*********************************************************
       a and l have previously been passed to Gauss() b is the product of
       a and x. x is the 1x3 matrix of coefficients to solve for
    *************************************************************/
    for (int k = 0; k < 2; ++k) {
      for (int i = k + 1; i < 3; ++i) {
        b[l[i]] -= a[l[i]][k] * b[l[k]];
      }
    }
    x[2] = b[l[2]] / a[l[2]][2];

    for (int i = 1; i >= 0; --i) {
      double sum = b[l[i]];
      for (int j = i + 1; j < 3; ++j) {
        sum = sum - a[l[i]][j] * x[j];
      }
      x[i] = sum / a[l[i]][i];
    }
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

  public ImageProjector(final GeoreferencedImage sourceImage,
    final GeometryFactoryProxy targetGeometryFactory) {
    this.sourceImage = sourceImage;
    this.sourceBufferdImage = sourceImage.getBufferedImage();
    setTargetGeometryFactory(targetGeometryFactory);
  }

  public void drawImage(final Graphics2D graphics) {
    this.g2 = graphics;

    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
      RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    final Shape clip = graphics.getClip();

    final int imageWidth = this.sourceImage.getImageWidth();
    final int imageHeight = this.sourceImage.getImageHeight();

    final int step = 50;
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

    final int l[] = new int[3];
    gauss(a, l);

    final double[] bx = new double[3];
    final double[] by = new double[3];
    for (int i = 0; i < 3; ++i) {
      bx[i] = targetTriangle.getX(i);
      by[i] = targetTriangle.getY(i);
    }

    final double[] x = new double[3];
    solve(a, l, bx, x);

    final double[] y = new double[3];
    solve(a, l, by, y);

    final AffineTransform af = new AffineTransform(x[0], y[0], x[1], y[1], x[2], y[2]);

    final Graphics2D g2 = this.g2;
    g2.setClip(targetTriangle);
    g2.drawImage(this.sourceBufferdImage, af, null);
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
    final double width1 = p1.distance(p2);
    final double width2 = p3.distance(p4);
    final double targetResolutionX = getResolution(sourceWidth, width1, width2);

    final double sourceResolutionY = this.sourceImage.getResolutionY();
    final double sourceHeight = sourceBoundingBox.getHeight() / sourceResolutionY;
    final double height1 = p1.distance(p4);
    final double height2 = p2.distance(p3);
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
