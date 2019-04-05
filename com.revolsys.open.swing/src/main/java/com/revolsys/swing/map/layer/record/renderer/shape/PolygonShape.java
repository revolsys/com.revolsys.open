package com.revolsys.swing.map.layer.record.renderer.shape;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.revolsys.geometry.model.Location;
import com.revolsys.geometry.model.Polygon;

public class PolygonShape extends AbstractGeometryShape<Polygon> {

  public PolygonShape() {
  }

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
  public PathIterator getPathIterator(final AffineTransform transform) {
    if (transform == null) {
      return new PolygonPathIterator(this.geometry);
    } else {
      return new PolygonPathIteratorTransform(this.geometry, transform);
    }
  }
}
