package com.revolsys.geometry.model.awtshape;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Location;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.vertex.Vertex;

public class PolygonShape extends AbstractGeometryShape<Polygon> {

  public PolygonShape(final Polygon polygon) {
    super(polygon);
  }

  @Override
  public boolean contains(final double x, final double y) {
    final Location location = this.geometry.locate(x, y);
    return location != Location.EXTERIOR;
  }

  @Override
  public boolean contains(final double x, final double y, final double w, final double h) {
    return false;
  }

  @Override
  public boolean contains(final Point2D point) {
    final double x = point.getX();
    final double y = point.getY();
    return contains(x, y);
  }

  @Override
  public boolean contains(final Rectangle2D rectangle) {
    final double x = rectangle.getX();
    final double y = rectangle.getY();
    final double width = rectangle.getWidth();
    final double height = rectangle.getHeight();
    return contains(x, y, width, height);
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
  public PathIterator getPathIterator(final AffineTransform transform) {
    final Vertex vertex = this.geometry.vertices();
    if (transform == null) {
      return new VertexPathIterator(vertex);
    } else {
      return new VertexPathIteratorTransform(vertex, transform);
    }
  }

  @Override
  public PathIterator getPathIterator(final AffineTransform transform, final double flatness) {
    return getPathIterator(transform);
  }

}
