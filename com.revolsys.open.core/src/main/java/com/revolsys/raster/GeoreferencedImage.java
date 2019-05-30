package com.revolsys.raster;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.beans.PropertyChangeListener;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.geometry.model.Point;
import com.revolsys.io.IoFactory;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.math.matrix.Matrix;
import com.revolsys.spring.resource.Resource;

public interface GeoreferencedImage
  extends BoundingBoxProxy, MapSerializer, PropertyChangeListener {
  static double[] calculateLSM(final BoundingBox boundingBox, final int imageWidth,
    final int imageHeight, final List<MappedLocation> mappings) {
    final Matrix A = getAMatrix(mappings, imageHeight);

    final Matrix X = getXMatrix(boundingBox, imageWidth, imageHeight, mappings);

    final Matrix P = getWeights(mappings.size());

    final Matrix AT = A.transpose();

    final Matrix ATP = new Matrix(AT.getRowCount(), P.getColumnCount());
    final Matrix ATPA = new Matrix(AT.getRowCount(), A.getColumnCount());
    final Matrix ATPX = new Matrix(AT.getRowCount(), 1);
    final Matrix x = new Matrix(A.getColumnCount(), 1);
    ATP.times(AT, P);
    ATPA.times(ATP, A);
    ATPX.times(ATP, X);
    ATPA.invert();
    x.times(ATPA, ATPX);
    ATPA.invert();

    return x.transpose().getRow(0);
  }

  static Matrix getAMatrix(final List<MappedLocation> mappings, final int imageHeight) {
    final int mappingCount = mappings.size();
    final int rowCount = mappingCount * 2;
    final Matrix aMatrix = new Matrix(rowCount, 6);

    for (int j = 0; j < mappingCount; ++j) {
      final MappedLocation mappedLocation = mappings.get(j);
      final Point sourcePoint = mappedLocation.getSourcePixel();
      final double x = sourcePoint.getX();
      final double y = imageHeight - sourcePoint.getY();
      aMatrix.setRow(j, x, y, 1.0D, 0.0D, 0.0D, 0.0D);
    }

    for (int j = mappingCount; j < rowCount; ++j) {
      final MappedLocation mappedLocation = mappings.get(j - mappingCount);
      final Point sourcePoint = mappedLocation.getSourcePixel();
      final double x = sourcePoint.getX();
      final double y = imageHeight - sourcePoint.getY();
      aMatrix.setRow(j, 0.0D, 0.0D, 0.0D, x, y, 1.0D);
    }
    return aMatrix;
  }

  public static Matrix getWeights(final int size) {
    final int matrixSize = size * 2;
    final Matrix P = new Matrix(matrixSize, matrixSize);

    for (int j = 0; j < matrixSize; ++j) {
      P.set(j, j, 1.0D);
    }
    return P;
  }

  static Matrix getXMatrix(final BoundingBox boundingBox, final int imageWidth,
    final int imageHeight, final List<MappedLocation> mappings) {
    final int mappingCount = mappings.size();
    final int rowCount = mappingCount * 2;
    final Matrix xMatrix = new Matrix(rowCount, 1);

    for (int j = 0; j < mappingCount; ++j) {
      final MappedLocation mappedLocation = mappings.get(j);
      final Point targetPixel = mappedLocation.getTargetPixel(boundingBox, imageWidth, imageHeight);
      final double x = targetPixel.getX();
      xMatrix.set(j, 0, x);
    }

    for (int j = mappingCount; j < rowCount; ++j) {
      final MappedLocation mappedLocation = mappings.get(j - mappingCount);
      final Point targetPixel = mappedLocation.getTargetPixel(boundingBox, imageWidth, imageHeight);
      final double y = imageHeight - targetPixel.getY();
      xMatrix.set(j, 0, y);
    }
    return xMatrix;
  }

  static boolean isReadable(final Path path) {
    return IoFactory.isAvailable(GeoreferencedImageReadFactory.class, path);
  }

  static GeoreferencedImage newGeoreferencedImage(final Object source) {
    final Resource resource = Resource.getResource(source);
    final GeoreferencedImageReadFactory factory = IoFactory
      .factory(GeoreferencedImageReadFactory.class, resource);
    if (factory == null) {
      return null;
    } else {
      final GeoreferencedImage reader = factory.readGeoreferencedImage(resource);
      return reader;
    }
  }

  void addTiePointsForBoundingBox();

  default void cancelChanges() {
  }

  default void copyModelPoint(final CoordinatesOperationPoint point, final double imageX,
    final double imageY) {
    final BoundingBox boundingBox = getBoundingBox();
    final double resolutionX = getResolutionX();
    final double resolutionY = getResolutionY();
    final double x = boundingBox.getMinX() + imageX * resolutionX;
    final double y = boundingBox.getMaxY() - imageY * resolutionY;
    point.setPoint(x, y);
  }

  void deleteTiePoint(MappedLocation tiePoint);

  default void drawImage(final Graphics2D graphics, final BoundingBox viewBoundingBox,
    final int viewWidth, final int viewHeight, final boolean useTransform) {
    if (viewBoundingBox.bboxIntersects(this) && viewWidth > 0 && viewHeight > 0) {
      if (isSameCoordinateSystem(viewBoundingBox)) {
        final RenderedImage renderedImage = getRenderedImage();
        drawRenderedImage(renderedImage, graphics, viewBoundingBox, viewWidth, viewHeight,
          useTransform);
      } else {
        final GeoreferencedImage image = imageToCs(viewBoundingBox);
        image.drawImage(graphics, viewBoundingBox, viewWidth, viewHeight, useTransform);
      }
    }
  }

  default void drawRenderedImage(final RenderedImage renderedImage, BoundingBox imageBoundingBox,
    final Graphics2D graphics, final BoundingBox viewBoundingBox, final int viewWidth,
    final boolean useTransform) {
    if (renderedImage != null) {
      final int imageWidth = renderedImage.getWidth();
      final int imageHeight = renderedImage.getHeight();
      if (imageWidth > 0 && imageHeight > 0) {

        final GeometryFactory viewGeometryFactory = viewBoundingBox.getGeometryFactory();
        imageBoundingBox = imageBoundingBox.bboxToCs(viewGeometryFactory);
        try {
          final double scaleFactor = viewWidth / viewBoundingBox.getWidth();

          final double imageMinX = imageBoundingBox.getMinX();
          final double viewMinX = viewBoundingBox.getMinX();
          final double screenX = (imageMinX - viewMinX) * scaleFactor;

          final double imageMaxY = imageBoundingBox.getMaxY();
          final double viewMaxY = viewBoundingBox.getMaxY();
          final double screenY = -(imageMaxY - viewMaxY) * scaleFactor;

          final double imageModelWidth = imageBoundingBox.getWidth();
          final int imageScreenWidth = (int)Math.ceil(imageModelWidth * scaleFactor);

          final double imageModelHeight = imageBoundingBox.getHeight();
          final int imageScreenHeight = (int)Math.ceil(imageModelHeight * scaleFactor);

          if (imageScreenWidth > 0 && imageScreenHeight > 0) {
            if (imageScreenWidth > 0 && imageScreenHeight > 0) {
              final double scaleX = (double)imageScreenWidth / imageWidth;
              final double scaleY = (double)imageScreenHeight / imageHeight;
              final AffineTransform imageTransform = new AffineTransform(scaleX, 0, 0, scaleY,
                screenX, screenY);
              if (useTransform) {
                final AffineTransform geoTransform = getAffineTransformation(imageBoundingBox);
                imageTransform.concatenate(geoTransform);
              }
              graphics.drawRenderedImage(renderedImage, imageTransform);
            }
          }
        } catch (final Throwable e) {
          e.printStackTrace();
        }
      }
    }
  }

  default void drawRenderedImage(final RenderedImage renderedImage, final Graphics2D graphics,
    final BoundingBox viewBoundingBox, final int viewWidth, final int viewHeight,
    final boolean useTransform) {
    final BoundingBox imageBoundingBox = getBoundingBox();
    drawRenderedImage(renderedImage, imageBoundingBox, graphics, viewBoundingBox, viewWidth,
      useTransform);
  }

  default AffineTransform getAffineTransformation(final BoundingBox boundingBox) {
    final List<MappedLocation> mappings = new ArrayList<>(getTiePoints());
    final int count = mappings.size();
    final int imageWidth = getImageWidth();
    final int imageHeight = getImageHeight();
    if (count == 1) {
      final MappedLocation tiePoint = mappings.get(0);
      final Point sourcePixel = tiePoint.getSourcePixel();
      final Point targetPixel = tiePoint.getTargetPixel(boundingBox, imageWidth, imageHeight);
      final double translateX = targetPixel.getX() - sourcePixel.getX();
      final double translateY = sourcePixel.getY() - targetPixel.getY();
      return new AffineTransform(1, 0, 0, 1, translateX, translateY);
    } else if (count < 3) {
      return new AffineTransform();
    }
    final double[] affineTransformMatrix = calculateLSM(boundingBox, imageWidth, imageHeight,
      mappings);
    final double translateX = affineTransformMatrix[2];
    final double translateY = affineTransformMatrix[5];
    final double scaleX = affineTransformMatrix[0];
    final double scaleY = affineTransformMatrix[4];
    final double shearX = affineTransformMatrix[1];
    final double shearY = affineTransformMatrix[3];
    return new AffineTransform(scaleX, shearY, shearX, scaleY, translateX, translateY);
  }

  default BufferedImage getBufferedImage() {
    final RenderedImage renderedImage = getRenderedImage();
    if (renderedImage == null) {
      return null;
    } else if (renderedImage instanceof BufferedImage) {
      return (BufferedImage)renderedImage;

    } else {
      final int width = getImageWidth();
      final int height = getImageHeight();
      final BufferedImage bufferedImage = new BufferedImage(width, height,
        BufferedImage.TYPE_INT_ARGB);
      final Graphics2D g2 = bufferedImage.createGraphics();
      g2.drawRenderedImage(renderedImage, null);
      g2.dispose();
      return bufferedImage;
    }
  }

  int[] getDpi();

  default GeoreferencedImage getImage(final GeometryFactoryProxy geometryFactory,
    final double resolution) {
    final int imageSrid = getHorizontalCoordinateSystemId();
    if (imageSrid > 0 && imageSrid != geometryFactory.getHorizontalCoordinateSystemId()) {
      final BoundingBox boundingBox = getBoundingBox();
      final ProjectionImageFilter filter = new ProjectionImageFilter(boundingBox, geometryFactory,
        resolution);

      final BufferedImage newImage = filter.filter(getBufferedImage());

      final BoundingBox destBoundingBox = filter.getDestBoundingBox();
      return new BufferedGeoreferencedImage(destBoundingBox, newImage);
    }
    return this;
  }

  default double getImageAspectRatio() {
    final int imageWidth = getImageWidth();
    final int imageHeight = getImageHeight();
    if (imageWidth > 0 && imageHeight > 0) {
      return (double)imageWidth / imageHeight;
    } else {
      return 0;
    }
  }

  int getImageHeight();

  Resource getImageResource();

  int getImageWidth();

  List<Dimension> getOverviewSizes();

  RenderedImage getRenderedImage();

  double getResolutionX();

  double getResolutionY();

  List<MappedLocation> getTiePoints();

  String getWorldFileExtension();

  default boolean hasBoundingBox() {
    return !getBoundingBox().isEmpty();
  }

  default boolean hasGeometryFactory() {
    return getGeometryFactory().getHorizontalCoordinateSystemId() > 0;
  }

  default GeoreferencedImage imageToCs(final GeometryFactoryProxy geometryFactory) {
    if (isSameCoordinateSystem(geometryFactory)) {
      return this;
    } else {
      return new ImageProjector(this, geometryFactory).newImage();
    }
  }

  boolean isHasChanages();

  default boolean isHasTransform() {
    final int count = getTiePoints().size();
    if (count > 2 || count == 1) {
      return true;
    } else {
      return false;
    }
  }

  boolean saveChanges();

  void setBoundingBox(final BoundingBox boundingBox);

  default void setBoundingBox(final double minX, final double maxY, final double pixelWidth,
    final double pixelHeight) {
    final GeometryFactory geometryFactory = getGeometryFactory();

    final int imageWidth = getImageWidth();
    final double maxX = minX + pixelWidth * imageWidth;

    final int imageHeight = getImageHeight();
    final double minY = maxY + pixelHeight * imageHeight;
    final BoundingBox boundingBox = geometryFactory.newBoundingBox(minX, maxY, maxX, minY);
    setBoundingBox(boundingBox);
  }

  void setDpi(final int... dpi);

  void setRenderedImage(final RenderedImage image);

  void setTiePoints(final List<MappedLocation> tiePoints);

  default void toImagePoint(final CoordinatesOperationPoint point) {
    final double x = point.getX();
    final double y = point.getY();
    final double resolutionX = getResolutionX();
    final double resolutionY = getResolutionY();
    final BoundingBox boundingBox = getBoundingBox();
    final double imageX = (x - boundingBox.getMinX()) / resolutionX;
    final double imageY = (boundingBox.getMaxY() - y) / resolutionY;
    point.setPoint(imageX, imageY);
  }

  default void writeImage(final Object target) {
    writeImage(target, MapEx.EMPTY);
  }

  default void writeImage(final Object target, final MapEx properties) {
    try (
      GeoreferencedImageWriter writer = GeoreferencedImageWriter.newGeoreferencedImageWriter(target,
        properties)) {
      if (writer == null) {
        throw new IllegalArgumentException("No image writer exists for " + target);
      } else {
        writer.write(this);
      }
    }

  }
}
