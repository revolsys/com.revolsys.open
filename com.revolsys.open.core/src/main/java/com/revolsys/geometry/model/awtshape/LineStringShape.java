package com.revolsys.geometry.model.awtshape;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

import com.revolsys.geometry.model.LineString;

public class LineStringShape extends AbstractGeometryShape<LineString> {

  public LineStringShape(final LineString line) {
    super(line);
  }

  @Override
  public PathIterator getPathIterator(final AffineTransform at) {
    if (at == null) {
      return new LineStringPathIterator(this.geometry);
    } else {
      return new LineStringPathIteratorTransform(this.geometry, at);
    }
  }

}
