package com.revolsys.swing.map.layer.record.renderer.shape;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;

public abstract class AbstractGeometryShape<G extends Geometry> implements Shape {

  protected G geometry;

  public AbstractGeometryShape() {
  }

  public AbstractGeometryShape(final G geometry) {
    setGeometry(geometry);
  }

  public void clearGeometry() {
    this.geometry = null;
  }

  @Override
  public boolean contains(final double x, final double y) {
    return false;
  }

  @Override
  public boolean contains(final double x, final double y, final double w, final double h) {
    return false;
  }

  @Override
  public boolean contains(final Point2D point) {
    return false;
  }

  @Override
  public boolean contains(final Rectangle2D r) {
    return false;
  }

  @Override
  public Rectangle getBounds() {
    final Rectangle2D bounds2d = getBounds2D();
    if (bounds2d == null) {
      return null;
    } else {
      return bounds2d.getBounds();
    }
  }

  @Override
  public Rectangle2D getBounds2D() {
    final BoundingBox boundingBox = this.geometry.getBoundingBox();
    if (boundingBox.isEmpty()) {
      return null;
    } else {
      final double x = boundingBox.getMinX();
      final double y = boundingBox.getMinY();
      final double width = boundingBox.getWidth();
      final double height = boundingBox.getHeight();
      return new Rectangle2D.Double(x, y, width, height);
    }
  }

  @Override
  public PathIterator getPathIterator(final AffineTransform at, final double flatness) {
    final PathIterator pathIterator = getPathIterator(at);
    return new FlatteningPathIterator(pathIterator, flatness);
  }

  @Override
  public boolean intersects(final double x, final double y, final double width,
    final double height) {
    return this.geometry.bboxIntersects(x, y, x + width, y + height);
  }

  @Override
  public boolean intersects(final Rectangle2D r) {
    final double x = r.getX();
    final double y = r.getY();
    final double width = r.getWidth();
    final double height = r.getHeight();
    return this.geometry.bboxIntersects(x, y, x + width, y + height);
  }

  public void setGeometry(final G geometry) {
    this.geometry = geometry;
  }
}
