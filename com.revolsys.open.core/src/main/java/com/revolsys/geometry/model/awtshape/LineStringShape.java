package com.revolsys.geometry.model.awtshape;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;

public class LineStringShape implements Shape {

  private LineString line;

  public LineStringShape(final LineString line) {
    setLine(line);
  }

  @Override
  public boolean contains(final double x, final double y) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean contains(final double x, final double y, final double w, final double h) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean contains(final Point2D point) {
    final double x = point.getX();
    final double y = point.getY();
    return contains(x, y);
  }

  @Override
  public boolean contains(final Rectangle2D r) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Rectangle getBounds() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Rectangle2D getBounds2D() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PathIterator getPathIterator(final AffineTransform at) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PathIterator getPathIterator(final AffineTransform at, final double flatness) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean intersects(final double x, final double y, final double w, final double h) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean intersects(final Rectangle2D r) {
    // TODO Auto-generated method stub
    return false;
  }

  public void setLine(final LineString line) {
    if (line == null) {
      this.line = GeometryFactory.DEFAULT_2D.lineString();
    } else {
      this.line = line;
    }
  }
}
