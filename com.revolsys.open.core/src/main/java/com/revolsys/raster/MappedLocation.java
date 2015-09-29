package com.revolsys.raster;

import java.awt.geom.AffineTransform;
import java.util.LinkedHashMap;
import java.util.Map;

import com.revolsys.beans.AbstractPropertyChangeObject;
import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.io.map.MapSerializer;

public class MappedLocation extends AbstractPropertyChangeObject
  implements GeometryFactoryProxy, MapSerializer {
  public static Point targetPointToPixel(final BoundingBox boundingBox, final Point point,
    final int imageWidth, final int imageHeight) {
    return toImagePoint(boundingBox, point, imageWidth, imageHeight);
  }

  public static Point toImagePoint(final BoundingBox boundingBox, Point modelPoint,
    final int imageWidth, final int imageHeight) {
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

  public static double[] toModelCoordinates(final GeoreferencedImage image,
    final BoundingBox boundingBox, final boolean useTransform, final double... coordinates) {
    double[] targetCoordinates;
    if (useTransform) {
      targetCoordinates = new double[10];
      final AffineTransform transform = image.getAffineTransformation(boundingBox);
      transform.transform(coordinates, 0, targetCoordinates, 0, coordinates.length / 2);
    } else {
      targetCoordinates = coordinates.clone();
    }
    final int imageWidth = image.getImageWidth();
    final int imageHeight = image.getImageHeight();
    for (int vertexIndex = 0; vertexIndex < coordinates.length / 2; vertexIndex++) {
      final int vertexOffset = vertexIndex * 2;
      final double xPercent = targetCoordinates[vertexOffset] / imageWidth;
      final double yPercent = (imageHeight - targetCoordinates[vertexOffset + 1]) / imageHeight;

      final double modelWidth = boundingBox.getWidth();
      final double modelHeight = boundingBox.getHeight();

      final double modelX = boundingBox.getMinX() + modelWidth * xPercent;
      final double modelY = boundingBox.getMinY() + modelHeight * yPercent;
      targetCoordinates[vertexOffset] = modelX;
      targetCoordinates[vertexOffset + 1] = modelY;
    }
    return targetCoordinates;
  }

  private GeometryFactory geometryFactory = GeometryFactory.floating(0, 2);

  private Point sourcePixel;

  private Point targetPoint;

  public MappedLocation(final Map<String, Object> map) {
    final double sourceX = Maps.getDouble(map, "sourceX", 0.0);
    final double sourceY = Maps.getDouble(map, "sourceY", 0.0);
    this.sourcePixel = new PointDouble(sourceX, sourceY);
    this.targetPoint = this.geometryFactory.geometry((String)map.get("target"));
  }

  public MappedLocation(final Point sourcePixel, final Point targetPoint) {
    this.sourcePixel = sourcePixel;
    this.targetPoint = targetPoint;
    this.geometryFactory = targetPoint.getGeometryFactory().convertAxisCount(2);
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  public Point getSourcePixel() {
    return this.sourcePixel;
  }

  public Point getSourcePoint(final GeoreferencedImage image, final BoundingBox boundingBox,
    final boolean useTransform) {
    final Point sourcePixel = getSourcePixel();
    final double[] sourcePoint = toModelCoordinates(image, boundingBox, useTransform,
      sourcePixel.getX(), image.getImageHeight() - sourcePixel.getY());
    final GeometryFactory geometryFactory = boundingBox.getGeometryFactory();
    return geometryFactory.point(sourcePoint[0], sourcePoint[1]);
  }

  // public Point getSourcePoint(final WarpFilter filter,
  // final WmsBoundingBox boundingBox) {
  // if (filter == null) {
  // return null;
  // } else {
  // final Point sourcePixel = getSourcePixel();
  // final Point sourcePoint = filter.sourcePixelToTargetPoint(boundingBox,
  // sourcePixel);
  // final GeometryFactory geometryFactory = filter.getGeometryFactory();
  // return geometryFactory.point(sourcePoint);
  // }
  // }

  public LineString getSourceToTargetLine(final GeoreferencedImage image,
    final BoundingBox boundingBox, final boolean useTransform) {

    final Point sourcePixel = getSourcePixel();
    final double[] sourcePoint = toModelCoordinates(image, boundingBox, useTransform,
      sourcePixel.getX(), image.getImageHeight() - sourcePixel.getY());
    final GeometryFactory geometryFactory = boundingBox.getGeometryFactory();
    final double sourceX = sourcePoint[0];
    final double sourceY = sourcePoint[1];

    final Point targetPoint = getTargetPoint().convert(geometryFactory);
    final double targetX = targetPoint.getX();
    final double targetY = targetPoint.getY();
    return geometryFactory.lineString(2, sourceX, sourceY, targetX, targetY);
  }

  public Point getTargetPixel(final BoundingBox boundingBox, final int imageWidth,
    final int imageHeight) {
    final GeometryFactory geometryFactory = boundingBox.getGeometryFactory();
    final Point targetPointCoordinates = (Point)this.targetPoint.convert(geometryFactory, 2);
    return targetPointToPixel(boundingBox, targetPointCoordinates, imageWidth, imageHeight);
  }

  public Point getTargetPoint() {
    return this.targetPoint;
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory.convertAxisCount(2);
    this.targetPoint = this.targetPoint.convert(this.geometryFactory);
  }

  public void setSourcePixel(final Point sourcePixel) {
    final Object oldValue = this.sourcePixel;
    this.sourcePixel = sourcePixel;
    firePropertyChange("sourcePixel", oldValue, sourcePixel);
  }

  public void setTargetPoint(final Point targetPoint) {
    final Object oldValue = this.targetPoint;
    this.targetPoint = targetPoint;
    firePropertyChange("targetPoint", oldValue, targetPoint);
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("sourceX", this.sourcePixel.getX());
    map.put("sourceY", this.sourcePixel.getY());
    map.put("target", this.targetPoint.toWkt());
    return map;
  }

  @Override
  public String toString() {
    return this.sourcePixel + "->" + this.targetPoint;
  }
}
